package gap.Wallet.util;

import gap.Wallet.Storage.StorageDriver;
import gap.Wallet.Storage.WalletException;

import java.io.IOException;
import java.util.Date;


public class rebuild {
	
	private static void show(String message) {
		System.out.println(new Date() + " " + message);
	}
	
	public static void main(String[] args) throws WalletException, IOException {
		System.out.println("wallet - rebuild tool");
		
		if (args.length != 1) {
			System.out.println("rebuild {database}");
			return;
		}
		
		show("rebuilding ...");		
			StorageDriver.rebuild(args[0]);
		show("done");
	}

}
