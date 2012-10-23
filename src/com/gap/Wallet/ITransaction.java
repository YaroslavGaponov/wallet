package com.gap.Wallet;

import java.io.IOException;

interface ITransaction {	
	public void start() throws WalletException;
	public void commit() throws WalletException, IOException;
	public void rollback() throws WalletException;
}
