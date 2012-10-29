package com.gap.wallet.storage;

public interface IterationAction {
	public boolean fire(byte[] key, byte[] value);
}
