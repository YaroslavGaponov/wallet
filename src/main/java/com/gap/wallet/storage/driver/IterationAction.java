package com.gap.wallet.storage.driver;

public interface IterationAction {
	public boolean fire(byte[] key, byte[] value);
}
