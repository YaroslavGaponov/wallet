package com.gap.Wallet;

public class WalletException extends Exception {
	private static final long serialVersionUID = -2383221501531994008L;
	private final String message;
	
	public WalletException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

}
