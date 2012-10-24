package gap.Wallet.Server;

import java.util.HashMap;
import java.util.Map;

public class Frame {
	public enum Command { CONNECT, CONNECTED, SET, GET, EXISTS, REMOVE, START, COMMIT, ROLLBACK, MESSAGE, SUCCESS, ERROR, DISCONNECT, DISCONNECTED };
	
	public Command command;
	public Map<String, String> params = new HashMap<String, String>();
	
	public Frame() {
		
	}
	
	public Frame(Command command) {
		this.command = command;
	}
	
	public byte[] getBytes() {
		StringBuilder s = new StringBuilder();
		s.append(command.toString() + "\n");
		for (String key : params.keySet()) {
			s.append(key + ":" + params.get(key) + "\n");
		}			
		return s.toString().getBytes();
	}
	
	public static Frame parse(byte[] raw) {
		String[] lines = new String(raw).split("\n");
		Frame frame = new Frame();
		frame.command = Command.valueOf(lines[0]);
		for (int i=1; i<lines.length; i++) {
			String kv[] = lines[i].split(":");
			if (kv.length == 2) {
				frame.params.put(kv[0], kv[1]);
			}
		}
		return frame;		
	}

}
