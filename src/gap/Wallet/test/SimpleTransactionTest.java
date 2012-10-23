package gap.Wallet.test;

import gap.Wallet.WalletException;
import gap.Wallet.WalletStorage;
import gap.Wallet.WalletStorageClient;

import java.io.IOException;
import java.util.Date;


public class SimpleTransactionTest {

	private static void show(String message) {
		System.out.println(new Date() + " " + message);
	}

	public static void main(String[] args) throws NumberFormatException, WalletException, IOException {

		if (args.length != 2) {
			System.out.println("test filename rows");
			return;
		}

		show("creating ...");
			WalletStorage.createStorage(args[0], Integer.parseInt(args[1]));
		show("done");
		
		WalletStorageClient client = new WalletStorageClient(args[0]);
		
		client.set("key #1".getBytes(), "value #1".getBytes());
		client.set("key #2".getBytes(), "value #2".getBytes());
		
		show("count = " + client.count());
		
		client.start();
		client.remove("key #2".getBytes());
		show("count = " + client.count());
		client.set("key #3".getBytes(), "value #3".getBytes());
		show("count = " + client.count());
		client.commit();
		show("count = " + client.count());
		
		client.start();		
		client.set("key #4".getBytes(), "value #4".getBytes());
		client.set("key #5".getBytes(), "value #5".getBytes());
		client.set("key #6".getBytes(), "value #6".getBytes());
		show("count = " + client.count());
		client.rollback();
		show("count = " + client.count());
		
		client.close();

	}

}
