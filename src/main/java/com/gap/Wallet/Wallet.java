package com.gap.Wallet;

import java.io.IOException;

import com.gap.Wallet.Deamon.Daemon;

public class Wallet {

	public static void main(String[] args) throws IOException {
		System.out.println("wallet - demon");
		
		if (args.length != 2) {
			halt("wallet [port] [path]");
		}
							
		int port = 0;
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			halt("error: " + e.toString());
		}
		
		System.out.println("wallet demon is starting on " + port);			
		Thread daemon = new Thread(new Daemon(port, args[1]));
		daemon.start();
				
	}

	private static void halt(String message) {
		System.out.println(message);	
		System.exit(-1);
	}

}
