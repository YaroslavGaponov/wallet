package com.gap.Wallet;
import java.io.IOException;


interface IStorage {
	public final long EOF	= -1;
	
	public final int recordHeaderSize = 24;
	
	public final int keySize_index	= 0;
	public final int valueSize_index = 1;
	public final int offsetNext_index = 2;


	public byte[] getKey(long offset) throws IOException;
	public byte[] getValue(long offset) throws IOException;
	public long getNextOffset(long offset) throws IOException;
	
	public void setNextOffset(long offset, long nextOffset) throws IOException;
	
	public long save(byte[] key, byte[] value, long nextOffset) throws IOException;	
}
