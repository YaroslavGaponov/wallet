package com.gap.Wallet.test;

import java.io.IOException;
import java.util.Date;

import com.gap.Wallet.WalletException;
import com.gap.Wallet.WalletStorage;
import com.gap.Wallet.WalletStorageManager;

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
		
		WalletStorageManager storage = new WalletStorageManager(args[0]);
		
		storage.set("key #1".getBytes(), "value #1".getBytes());
		storage.set("key #2".getBytes(), "value #2".getBytes());
		
		show("count = " + storage.count());
		
		storage.start();
		storage.remove("key #2".getBytes());
		show("count = " + storage.count());
		storage.set("key #3".getBytes(), "value #3".getBytes());
		show("count = " + storage.count());
		storage.commit();
		show("count = " + storage.count());
		
		storage.start();		
		storage.set("key #4".getBytes(), "value #4".getBytes());
		storage.set("key #5".getBytes(), "value #5".getBytes());
		storage.set("key #6".getBytes(), "value #6".getBytes());
		show("count = " + storage.count());
		storage.rollback();
		show("count = " + storage.count());
		
		storage.close();

	}

}
