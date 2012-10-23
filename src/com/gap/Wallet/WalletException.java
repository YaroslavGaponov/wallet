package com.gap.Wallet;

public class WalletException extends RuntimeException {
	private static final long serialVersionUID = 1L;
		
	public WalletException() {
		super();
	}
	
	public WalletException(String s) {
		super(s);
	}
	
	public WalletException(String s, Throwable t) {
		super(s, t);
	}
	
	public WalletException(Throwable t) {
		super(t);
	}

}
