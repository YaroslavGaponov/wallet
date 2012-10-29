package com.gap.Wallet.Server;

import java.nio.ByteBuffer;

public class IOBuffers {
	public final ByteBuffer out;
	public final ByteBuffer in;
	
	public IOBuffers(int insize, int outsize) {
		out = ByteBuffer.allocate(insize);
		out.clear();
		in = ByteBuffer.allocate(outsize);
		in.clear();
	}
}
