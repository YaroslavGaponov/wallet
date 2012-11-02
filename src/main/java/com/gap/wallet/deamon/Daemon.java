package com.gap.wallet.deamon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import com.gap.wallet.core.WalletSocketServer;

public class Daemon {
	private final WalletSocketServer server;
	private final int shutdown;

	public Daemon(int port, String path, int shutdown) throws IOException {
		this.server =  new WalletSocketServer(port, path);
		this.shutdown = shutdown;
	}

	public void start() {
		try {
			this.server.start();
			startShutdownServer();
		} catch (IOException e) {
		}
	}

	public void stop() {
		this.server.stop();
	}
		
	
	private void startShutdownServer() {
		ServerSocket  server = null;
		try {
			server = new ServerSocket(shutdown);
		    Socket clientSocket  = server.accept();
		    clientSocket.close();
		    stop();
		} catch (IOException e) {
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
				}
			}
		}				
		
	}
		
	public static void stopDaemon(int shutdown) {
		Socket clientSocket;
		try {
			clientSocket = new Socket(InetAddress.getLocalHost(), shutdown);
			clientSocket.close();
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
			
	}
	
}
