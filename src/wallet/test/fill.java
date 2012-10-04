package wallet.test;

import wallet.storage.WalletException;
import wallet.storage.WalletStorage;

public class fill {

	public static void main(String[] args) throws WalletException {
		if (args.length != 2) {
			System.out.println("fill filename count");
			return;
		}
		
		WalletStorage ws = null;
		try {
			ws = new WalletStorage(args[0]);
						
			for (int i=0; i<Integer.parseInt(args[1]); i++) {
				String key = java.util.UUID.randomUUID().toString();				
				ws.put(key, ("Hello world #" + key).getBytes());
			}
			
			System.out.println("total records is " + ws.count());
			
		} finally {
			ws.close();
		}

	}

}
