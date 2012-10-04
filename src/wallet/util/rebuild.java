package wallet.util;

import wallet.storage.Callback;
import wallet.storage.WalletException;
import wallet.storage.WalletStorage;

public class rebuild {

	public static void main(String[] args) throws NumberFormatException, WalletException {
		if (args.length != 2) {
			System.out.println("rebuild filename buckettablesize");
			return;
		}
		
		System.out.println("start rebuiling ...");
		WalletStorage.rebuild(args[0], Integer.parseInt(args[1]), new Callback() {
			public void run(Object ... o)  {
				System.out.print("processed " + o[2] + " %\r");
			}			
		});		

	}

}
