package com.gap.wallet.netserver;

import java.util.HashMap;
import java.util.Map;

public class Frame {
	private Command command;
	private Map<String, String> params = new HashMap<String, String>();

	public Frame(Command command) {
		this.command = command;
	}
	
	public Command getCommand() {
		return command;
	}

	public Frame addParam(String name, String value) {
		params.put(name, value);
		return this;
	}

	public String getParam(String name) {
		return params.get(name);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(command.toString() + '\n');
		for (String key : params.keySet()) {
			sb.append(key + ':' + params.get(key) + '\n');
		}
		sb.append('\0');
		return sb.toString();
	}

	public static Frame createFrame(Command cmd) {
		return new Frame(cmd);
	}	
	
	public static Frame parse(String data) {
		String[] lines = data.split("\n");		
		Command cmd = Command.valueOf(lines[0]);
		Frame frame = new Frame(cmd);
		for (int i = 1; i < lines.length; i++) {
			String kv[] = lines[i].split(":");
			if (kv.length == 2) {
				frame.params.put(kv[0], kv[1]);
			} 
		}
		return frame;
	}

}
