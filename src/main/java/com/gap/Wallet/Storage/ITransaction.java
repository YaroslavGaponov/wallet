package com.gap.wallet.storage;

interface ITransaction {	
	public void start() throws WalletException;
	public void commit() throws WalletException;
	public void rollback() throws WalletException;
}
