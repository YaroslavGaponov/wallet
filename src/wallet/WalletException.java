package wallet;

public class WalletException extends Exception {
	private String error;
	
	public WalletException(String error) {
		this.error = error;
	}

}
