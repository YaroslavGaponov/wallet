package com.gap.Wallet.Storage;

interface IBucket {
	public long get(int index);
	public void set(int index, long offset);
}
