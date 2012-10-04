package wallet.test;

import wallet.storage.WalletStorage;

public class search {

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("search filename key");
			return;
		}
		
		WalletStorage ws = null;
		try {
			ws = new WalletStorage(args[0]);
						
			System.out.println("total records is " + ws.count());
			
			long start = System.currentTimeMillis();
			byte[] value = ws.get(args[1]);
			long stop = System.currentTimeMillis();
			
			if (value == null) {
				System.out.println("key " + args[1] + "is not found");				
			} else {			
				System.out.println(args[1] + " : " + new String(value));
			}
			
			System.out.println("took : " + (stop - start) + " ms");
			
		} finally {
			ws.close();
		}
		
	}

}
