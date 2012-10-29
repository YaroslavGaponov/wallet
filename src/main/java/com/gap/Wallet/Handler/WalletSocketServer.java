package com.gap.Wallet.Handler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gap.Wallet.NetServer.Frame;
import com.gap.Wallet.NetServer.SocketServer;
import com.gap.Wallet.NetServer.Command;
import com.gap.Wallet.Storage.StorageSession;
import com.gap.Wallet.Storage.WalletException;

public class WalletSocketServer extends SocketServer {
	
	private final static int DEFAULT_PORT = 12345;
	private final Map<String, StorageSession> sessions = new HashMap<String, StorageSession>();

	private final String path;

	public WalletSocketServer(String path) throws IOException {
		this(DEFAULT_PORT, path);
	}
	
	public WalletSocketServer(int port, String path) throws IOException {
		super(port);
		this.path = new File(path).getAbsolutePath() + '/';
	}

	public byte[] handler(byte[] request) {		
		Frame requestFrame = Frame.parse(new String(request));
		
		assert(request != null);
				
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
				throw new WalletException("Command is not supported.");
			}		
			
			// return id back
			String id = requestFrame.getParam("id");
			if (id != null) {
				responseFrame.addParam("id", id);
			}
			
			return responseFrame.toString().getBytes();
		} catch (WalletException ex) {
			return
				Frame
					.createFrame(Command.ERROR)
						.addParam("result", ex.getMessage())
						.addParam("id",  requestFrame.getParam("id") != null ? requestFrame.getParam("id") : null)
							.toString()
								.getBytes();
		}
	}
	
	private Frame connect(Frame request) {
		String database = request.getParam("database");
		assert(database != null);
		
		String session = UUID.randomUUID().toString();
		sessions.put(session, new StorageSession(path + database));
				
		
		return 
			Frame
				.createFrame(Command.ANSWER)
					.addParam("result", session.toString());		
	}

	private Frame set(Frame request) {
		String session = request.getParam("session");
		assert(session != null);
		
		String key = request.getParam("key");
		assert(key != null);
		
		String value = request.getParam("value");
		Boolean res = sessions.get(session).set(key.getBytes(), value.getBytes());
						
		return 
				Frame
					.createFrame(Command.ANSWER)
						.addParam("result", res.toString());		
		
	}

	private Frame exists(Frame request) {
		String session = request.getParam("session");
		assert(session != null);
		
		String key = request.getParam("key");
		assert(key != null);
		
		Boolean res = sessions.get(session).exists(key.getBytes());
		
		return 
				Frame
					.createFrame(Command.ANSWER)
						.addParam("result", res.toString());
	}

	private Frame count(Frame request) {
		String session = request.getParam("session");
		assert(session != null);
		
		Long count = sessions.get(session).count();

		return 
				Frame
					.createFrame(Command.ANSWER)
						.addParam("result", count.toString());		
		
	}

	private Frame get(Frame request) {
		String session = request.getParam("session");
		assert(session != null);
		
		String key = request.getParam("key");
		assert(key != null);
		
		byte[] value = sessions.get(session).get(key.getBytes());
				
		return 
				Frame
					.createFrame(Command.ANSWER)
						.addParam("result", new String(value));		
		
	}

	private Frame remove(Frame request) {
		String session = request.getParam("session");
		assert(session != null);
		
		String key = request.getParam("key");
		assert(key != null);
		
		Boolean res = sessions.get(session).remove(key.getBytes());
		
		return 
				Frame
					.createFrame(Command.ANSWER)
						.addParam("result", res.toString());	
				
	}

	private Frame start(Frame request) {
		String session = request.getParam("session");
		assert(session != null);
		
		sessions.get(session).start();
		
		return 
				Frame
					.createFrame(Command.ANSWER)
						.addParam("result", Boolean.TRUE.toString());	
	}

	private Frame commit(Frame request) {
		String session = request.getParam("session");
		assert(session != null);
		
		sessions.get(session).commit();
		
		return 
				Frame
					.createFrame(Command.ANSWER)
						.addParam("result", Boolean.TRUE.toString());	
	}

	private Frame rollback(Frame request) {
		String session = request.getParam("session");
		assert(session != null);
		
		sessions.get(session).rollback();
		
		return 
				Frame
					.createFrame(Command.ANSWER)
						.addParam("result", Boolean.TRUE.toString());	
	}

	private Frame disconnect(Frame request) {
		String session = request.getParam("session");
		assert(session != null);
		
		sessions.get(session).close();
		
		return 
				Frame
					.createFrame(Command.ANSWER)
						.addParam("result", Boolean.TRUE.toString());	
	}

}
