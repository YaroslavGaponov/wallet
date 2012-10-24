package gap.Wallet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import gap.Wallet.Server.Frame;
import gap.Wallet.Server.Frame.Command;
import gap.Wallet.Server.SocketServer;
import gap.Wallet.Storage.StorageSession;

public class demon {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("walletd [port] [file]");
			return;
		}
		
		
		int port  = Integer.parseInt(args[0]);
		String file = args[1];
		
		SocketServer socketServer = new SocketServer(port) {
			private final Map<String, StorageSession> sessions = new HashMap<String, StorageSession>();
						
			
			private StorageSession getStorageSession(String session) {
				return sessions.get(session);
			}
			
			@SuppressWarnings("incomplete-switch")
			public byte[] handler(byte[] request) {		
				
				String session;
				byte[] key, value;
				
				Frame frameReq = Frame.parse(request);				
				switch (frameReq.command) {
					case CONNECT: 							
						session = UUID.randomUUID().toString();
						sessions.put(session, new StorageSession(getParam("file")));												
						Frame frameResConnect = new Frame();
						frameResConnect.command = Command.CONNECTED;
						frameResConnect.params.put("session", session.toString());
						return frameResConnect.getBytes();
					
					case SET: 
						session = frameReq.params.get("session");
						key = frameReq.params.get("key").getBytes();
						value = frameReq.params.get("value").getBytes();
						if (getStorageSession(session).set(key, value)) {
							return new Frame(Command.SUCCESS).getBytes();
						} 
					
					case GET:
						session = frameReq.params.get("session");
						key = frameReq.params.get("key").getBytes();
						value = getStorageSession(session).get(key);
						Frame frameResGet = new Frame(Command.MESSAGE);
						frameResGet.params.put(frameReq.params.get("key"), new String(value));
						return frameResGet.getBytes();
						
						
					case REMOVE:
						session = frameReq.params.get("session");
						key = frameReq.params.get("key").getBytes();
						if (getStorageSession(session).remove(key)) {
							return new Frame(Command.SUCCESS).getBytes();
						} 
						
					case START:
						session = frameReq.params.get("session");
						getStorageSession(session).start();
						return new Frame(Command.SUCCESS).getBytes();
						
						
					case COMMIT:
						session = frameReq.params.get("session");
						getStorageSession(session).commit();
						return new Frame(Command.SUCCESS).getBytes();
						
					case ROLLBACK:
						session = frameReq.params.get("session");
						getStorageSession(session).rollback();
						return new Frame(Command.SUCCESS).getBytes();
						
					case DISCONNECT:
						session = frameReq.params.get("session");
						getStorageSession(session).close();
						return new Frame(Command.DISCONNECTED).getBytes();
						
				}				
				return new Frame(Command.ERROR).getBytes();
			}
		};
		socketServer.addParam("file", file);
		socketServer.start();
		
	}

}
