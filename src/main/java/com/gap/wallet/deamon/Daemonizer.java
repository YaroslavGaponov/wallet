package com.gap.wallet.deamon;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStream;

public class Daemonizer {
	
	public static void start(final Service service, int shutdown) {		
		// start service in separate thread
		Thread threadService = new Thread(new Runnable() {
			public void run() {
				service.start();
			}
		});
		threadService.start();
		
		// start simple socket server for wait shutdown signal
		ServerSocket serverSocket;
		Socket clientSocket;
		byte[] buf = new byte[8];
		String cmd;
		try {
			serverSocket = new ServerSocket(shutdown);
			do {
				clientSocket = serverSocket.accept();
				InputStream in = clientSocket.getInputStream();
				in.read(buf);
				in.close();
				cmd = new String(buf);
			} while (!cmd.equals("shutdown"));
			service.stop();
			clientSocket.close();
		} catch (IOException e) {
		}
	}
	
	public static void stop(int port) {
		Socket clientSocket;
		try {
			clientSocket = new Socket("localhost", port);
			OutputStream out = clientSocket.getOutputStream();
			out.write(new String("shutdown").getBytes());
			out.close();
			clientSocket.close();
		} catch (IOException e) {
		}
		
	}
	
}
