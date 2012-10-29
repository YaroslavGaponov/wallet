package com.gap.Wallet.Deamon;

import java.io.IOException;

import com.gap.Wallet.Handler.WalletSocketServer;

public class Daemon implements Runnable {
	private final WalletSocketServer server;

	public Daemon(int port, String path) throws IOException {
		this.server =  new WalletSocketServer(port, path);
	}

	public void run() {
		try {
			this.server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
