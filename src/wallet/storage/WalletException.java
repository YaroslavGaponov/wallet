package wallet.storage;

public class WalletException extends Exception {
	private String error;
	
	public WalletException(String error) {
		super(error);
		this.error = error;
	}
	
	public String getError() {
		return error;
	}

}
