package gap.Wallet.deamon;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import gap.Wallet.Server.Frame;
import gap.Wallet.Server.SocketServer;
import gap.Wallet.Server.Frame.Command;
import gap.Wallet.Storage.StorageSession;
import gap.Wallet.Storage.WalletException;

public class WalletSocketServer extends SocketServer implements Runnable {
	
	private final static int DEFAULT_PORT = 3678;
	private final Map<String, StorageSession> sessions = new HashMap<String, StorageSession>();

	private String path;

	public WalletSocketServer(String path) throws IOException {
		this(DEFAULT_PORT, path);
	}
	
	public WalletSocketServer(int port, String path) throws IOException {
		super(port);
		this.path = new File(path).getAbsolutePath() + '/';
	}

	public String handler(String request) {		
		Frame requestFrame = Frame.parse(request);

		Frame responseFrame = null;
		try {
			switch (requestFrame.getCommand()) {
				case CONNECT:
					responseFrame = connect(requestFrame);
					break;
				case SET:
					responseFrame = set(requestFrame);
					break;
				case GET:
					responseFrame = get(requestFrame);
					break;
				case REMOVE:
					responseFrame = remove(requestFrame);
					break;
				case START:
					responseFrame = start(requestFrame);
					break;
				case COMMIT:
					responseFrame = commit(requestFrame);
					break;
				case ROLLBACK:
					responseFrame = rollback(requestFrame);
					break;
				case EXISTS:
					responseFrame = exists(requestFrame);
					break;
				case COUNT:
					responseFrame = count(requestFrame);
					break;
				case DISCONNECT:
					responseFrame = disconnect(requestFrame);
					break;
			default:
				break;
			}		
			
			// return frameID back
			String frameId = requestFrame.getParam("frameID"); 
			responseFrame.addParam("frameID", frameId);
						
			return responseFrame.toString();
		} catch (WalletException ex) {
			responseFrame = new Frame(Command.ERROR);
			responseFrame.addParam("result", ex.getMessage());
			return responseFrame.toString(); 
		}
	}
	
		
	private Frame connect(Frame request) {
		String database = request.getParam("database");
		String session = UUID.randomUUID().toString();
		sessions.put(session, new StorageSession(path + database));
		
		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", session.toString());
		return response;
	}

	private Frame set(Frame request) {
		String session = request.getParam("session");
		String key = request.getParam("key");
		String value = request.getParam("value");
		Boolean res = sessions.get(session).set(key.getBytes(), value.getBytes());
				
		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", res.toString());
		return response;
	}

	private Frame exists(Frame request) {
		String session = request.getParam("session");
		String key = request.getParam("key");
		Boolean res = sessions.get(session).exists(key.getBytes());
		
		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", res.toString());
		return response;
	}

	private Frame count(Frame request) {
		String session = request.getParam("session");
		Long count = sessions.get(session).count();

		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", count.toString());
		return response;		
	}

	private Frame get(Frame request) {
		String session = request.getParam("session");
		String key = request.getParam("key");
		byte[] value = sessions.get(session).get(key.getBytes());
		
		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", new String(value));
		return response;		
	}

	private Frame remove(Frame request) {
		String session = request.getParam("session");
		String key = request.getParam("key");
		Boolean res = sessions.get(session).remove(key.getBytes());
		
		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", res.toString());
		return response;
	}

	private Frame start(Frame request) {
		String session = request.getParam("session");
		sessions.get(session).start();
		
		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", ((Boolean)(true)).toString());
		return response;

	}

	private Frame commit(Frame request) {
		String session = request.getParam("session");
		sessions.get(session).commit();
		
		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", ((Boolean)(true)).toString());
		return response;
	}

	private Frame rollback(Frame request) {
		String session = request.getParam("session");
		sessions.get(session).rollback();
		
		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", ((Boolean)(true)).toString());
		return response;
	}

	private Frame disconnect(Frame request) {
		String session = request.getParam("session");
		sessions.get(session).close();
		
		Frame response = new Frame(Command.ANSWER);
		response.addParam("result", ((Boolean)(true)).toString());
		return response;
	}

	public void run() {
		try {
			start();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
