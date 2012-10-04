package wallet.test;

import wallet.storage.Callback;
import wallet.storage.WalletException;
import wallet.storage.WalletStorage;

public class dump {
	
	public static void main(String[] args) throws WalletException {
		if (args.length != 1) {
			System.out.println("dump filename");
			return;
		}
		
		WalletStorage ws = null;
		try {
			ws = new WalletStorage(args[0]);
			ws.iterator(new Callback() {
				public void run(Object... o) {
					System.out.println(o[0] + " : " + new String((byte[])o[1]));
				}				
			});
		} finally {
			ws.close();
		}

	}

}
