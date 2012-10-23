package com.gap.Wallet;


interface IStorage {
	public final long EOF	= -1;
	
	public final int recordHeaderSize = 24;
	
	public final int keySize_index	= 0;
	public final int valueSize_index = 1;
	public final int offsetNext_index = 2;


	public byte[] getKey(long offset) throws WalletException;
	public byte[] getValue(long offset) throws WalletException;
	public long getNextOffset(long offset) throws WalletException;	
	public void setNextOffset(long offset, long nextOffset) throws WalletException;
	
	public long save(byte[] key, byte[] value, long nextOffset) throws WalletException;	
}
