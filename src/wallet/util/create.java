package wallet.util;

import wallet.storage.WalletException;
import wallet.storage.WalletStorage;

public class create {

	public static void main(String[] args) throws NumberFormatException, WalletException {
		if (args.length != 2) {
			System.out.println("create filename buckettablesize");
			return;
		}
		
		System.out.println("create a new database " + args[0] + " ...");
		WalletStorage.create(args[0], Integer.parseInt(args[1]));
		System.out.println("done successful");
	}

}
