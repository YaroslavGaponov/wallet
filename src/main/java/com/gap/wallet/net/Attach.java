package com.gap.wallet.net;

import java.nio.ByteBuffer;

public class Attach {
	public final ByteBuffer out;
	public final ByteBuffer in;
	
	public Attach(int insize, int outsize) {
		assert(insize > 0);
		assert(outsize > 0);
		
		out = ByteBuffer.allocate(insize);
		out.clear();
		
		in = ByteBuffer.allocate(outsize);
		in.clear();
	}
}
