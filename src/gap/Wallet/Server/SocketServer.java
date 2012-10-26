package gap.Wallet.Server;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;

public abstract class SocketServer {

	private final static int DEFAULT_BUFFER_SIZE = 16384;
	private final ByteBuffer buffer;

	private final Selector selector;
	private final ServerSocketChannel ssc;

	public SocketServer(int port) throws IOException {
		this(port, DEFAULT_BUFFER_SIZE);
	}

	public SocketServer(int port, int buffersize) throws IOException {

		buffer = ByteBuffer.allocate(buffersize);

		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(port));
		selector = Selector.open();
		ssc.register(selector, SelectionKey.OP_ACCEPT);
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
							
								Attachment attachment = (Attachment) key.attachment();
								if (attachment == null) {
									attachment = new Attachment();
									key.attach(attachment);
								}
								
								SocketChannel sc  = (SocketChannel) key.channel();
								sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
								
								buffer.clear();
								if (attachment.in.length() > 0) {
									buffer.put(attachment.in.toString().getBytes());
								}
								sc.read(buffer);
								buffer.flip();
		
								 Charset charset = Charset.defaultCharset();  
							     CharsetDecoder decoder = charset.newDecoder();  
							     CharBuffer charBuffer = decoder.decode(buffer); 
								
								attachment.in.setLength(0);
								for (int i=0; i<charBuffer.limit(); i++) {
									attachment.in.append( charBuffer.get(i));
									if ( charBuffer.get(i) == '\0') {
										attachment.out.append(handler(attachment.in.toString()));
										attachment.in.setLength(0);
									}							
								}
							
							} catch (IOException ex) {
								key.cancel();
								key.channel().close();
							}
							
							
						} else if (key.isWritable()) {	
							
							try {							
								
								Attachment attachment = (Attachment) key.attachment();
								if (attachment != null && attachment.out.length() > 0) {	
									SocketChannel sc  = (SocketChannel) key.channel();															
									sc.write(ByteBuffer.wrap(attachment.out.toString().getBytes()));
									attachment.out.setLength(0);
								}
								
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

	public abstract String handler(String request);

}
