package com.gap.wallet.storage;

interface IBucket {
	public long get(int index);
	public void set(int index, long offset);
}
