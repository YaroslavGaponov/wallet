package com.gap.Wallet.Server;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

public abstract class SocketServer {
	private final static int DEFAULT_BUFFER_SIZE = 16384;
	private final Selector selector;
	private final ServerSocketChannel ssc;
	private final int buffersize;

	public SocketServer(int port) throws IOException {
		this(port, DEFAULT_BUFFER_SIZE);
	}

	public SocketServer(int port, int buffersize) throws IOException {
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(port));
		selector = Selector.open();
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		this.buffersize = buffersize;
	}

	public void start() throws IOException {
		try {
			while (true) {
				int num = selector.select();

				if (num == 0) {
					continue;
				}

				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = (SelectionKey) it.next();

					if (key.isValid()) {
						if (key.isAcceptable()) {
							Socket s = ssc.accept().socket();
							SocketChannel sc = s.getChannel();
							sc.configureBlocking(false);
							sc.register(selector, SelectionKey.OP_READ);
	
						} else if (key.isReadable()) {
							
							try {
							
								IOBuffers attachment = (IOBuffers) key.attachment();
								if (attachment == null) {
									attachment = new IOBuffers(buffersize, buffersize);
									key.attach(attachment);
								}
								
								SocketChannel sc = (SocketChannel) key.channel();
								
								sc.read(attachment.in);
								attachment.in.flip();
				
								StringBuilder sb = new StringBuilder();
								while (attachment.in.remaining() > 0) {
									char ch = (char) attachment.in.get();
									
									sb.append(ch);
									if (ch == '\0') {
										byte[] response = handler(sb.toString().getBytes());
										attachment.out.put(response);
										sb.setLength(0);
										key.interestOps(SelectionKey.OP_WRITE);
									}	
								}
								
								attachment.in.clear();
								attachment.in.put(sb.toString().getBytes());				
							
							} catch (IOException ex) {
								key.cancel();
								key.channel().close();
							}
							
							
						} else if (key.isWritable()) {	
							try {							
								IOBuffers attachment = (IOBuffers) key.attachment();						
								SocketChannel sc  = (SocketChannel) key.channel();	
								attachment.out.flip();
								sc.write(attachment.out);
								attachment.out.clear();
								key.interestOps(SelectionKey.OP_READ);
								
							} catch (IOException ex) {
								key.cancel();
								key.channel().close();
							}
	
						}
					}
				}
				keys.clear();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		Set<SelectionKey> keys = selector.selectedKeys();
		Iterator<SelectionKey> it = keys.iterator();
		while (it.hasNext()) {
			SelectionKey key = (SelectionKey) it.next();
			if (key.channel().isOpen()) {
				try {
					key.channel().close();
				} catch (IOException e) {
				}
			}
		}

		if (ssc.isOpen()) {
			try {
				ssc.close();
			} catch (IOException e) {
			}
		}

	}

	public abstract byte[] handler(byte[] request);

}
