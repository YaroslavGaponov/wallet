package gap.Wallet.Server;

import java.util.HashMap;
import java.util.Map;

public class Frame {
	public enum Command {
		// client messages
		CONNECT, DISCONNECT, SET, GET, COUNT, EXISTS, REMOVE, START, COMMIT, ROLLBACK,

		// server message
		MESSAGE
	};

	private Command command;
	private Map<String, String> params = new HashMap<String, String>();

	private Frame() {

	}

	public Frame(Command command) {
		this.command = command;
	}

	public Command getCommand() {
		return command;
	}

	public void addParam(String name, String value) {
		params.put(name, value);
	}

	public String getParam(String name) {
		return params.get(name);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(command.toString() + "\n");
		for (String key : params.keySet()) {
			s.append(key + ":" + params.get(key) + "\n");
		}
		s.append('\0');
		return s.toString();
	}

	public static Frame parse(String data) {
		String[] lines = data.split("\n");
		Frame frame = new Frame();
		frame.command = Command.valueOf(lines[0]);
		for (int i = 1; i < lines.length; i++) {
			String kv[] = lines[i].split(":");
			if (kv.length == 2) {
				frame.params.put(kv[0], kv[1]);
			}
		}
		return frame;
	}

}
