package com.gap.Wallet.Storage;

interface ITransaction {	
	public void start() throws WalletException;
	public void commit() throws WalletException;
	public void rollback() throws WalletException;
}
