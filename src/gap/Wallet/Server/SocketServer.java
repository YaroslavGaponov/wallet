package gap.Wallet.Server;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
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

					if (key.isAcceptable()) {

						Socket s = ssc.accept().socket();
						SocketChannel sc = s.getChannel();
						sc.configureBlocking(false);
						sc.register(selector, SelectionKey.OP_READ);
						key.attach(null);

					} else if (key.isReadable()) {

						SocketChannel sc = null;
						try {
							sc = (SocketChannel) key.channel();
							boolean ok = ioworker(key, sc);

							if (!ok) {
								key.cancel();

								Socket s = null;
								try {
									s = sc.socket();
									s.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

						} catch (IOException e) {
							key.cancel();
							try {
								sc.close();
							} catch (IOException ex) {
								ex.printStackTrace();
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

	private boolean ioworker(SelectionKey key, SocketChannel sc)
			throws IOException {

		byte[] tail = (byte[]) key.attachment();

		buffer.clear();
		if (tail != null) {
			buffer.put(tail);
		}
		sc.read(buffer);
		buffer.flip();

		if (buffer.limit() == 0) {
			return false;
		}

		int j = 0;
		for (int i = 0; i < buffer.limit(); i++) {
			if (buffer.get(i) == '\0') {
				byte[] response = handler(Arrays.copyOfRange(buffer.array(), j, i));
				sc.write(ByteBuffer.wrap(response));
				j = i + 1;
			}
		}

		key.attach(Arrays.copyOfRange(buffer.array(), j, buffer.limit()));

		return true;
	}

}
