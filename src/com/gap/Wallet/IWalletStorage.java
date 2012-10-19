package com.gap.Wallet;

import java.io.IOException;


public interface IWalletStorage {
	public final long version = 1L;
	
	public byte[] get(byte[] key) throws IOException;
	public boolean set(byte[] key, byte[] value) throws IOException;
	public boolean remove(byte[] key) throws IOException;
	public long count();
	
	public long iterator(IterationAction iterationAction) throws IOException;
	
	public void close();
}
