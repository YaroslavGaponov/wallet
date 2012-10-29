package com.gap.Wallet.NetServer;

import java.nio.ByteBuffer;

public class Attach {
	public final ByteBuffer out;
	public final ByteBuffer in;
	
	public Attach(int insize, int outsize) {
		out = ByteBuffer.allocate(insize);
		out.clear();
		in = ByteBuffer.allocate(outsize);
		in.clear();
	}
}
