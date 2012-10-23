package com.gap.Wallet;

import java.io.IOException;


public interface IWalletStorage {
	public final long version = 1L;
	
	public boolean exists(byte[] key) throws IOException;
	public byte[] get(byte[] key) throws IOException;
	public boolean set(byte[] key, byte[] value) throws IOException;
	public boolean remove(byte[] key) throws IOException;
	public long count();
	
	public void close();
}
