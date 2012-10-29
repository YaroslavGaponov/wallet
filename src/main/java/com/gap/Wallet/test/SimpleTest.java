package com.gap.Wallet.test;

import com.gap.Wallet.Storage.StorageDriver;
import com.gap.Wallet.Storage.StorageSession;
import com.gap.Wallet.Storage.WalletException;

import java.io.IOException;
import java.util.Date;


public class SimpleTest {

	private static void show(String message) {
		System.out.println(new Date() + " " + message);
	}
	
	public static void main(String[] args) throws WalletException, NumberFormatException, IOException {
				
		if (args.length != 2) {
			System.out.println("test filename rows");
			return;
		}

		
		show("creating ...");
			StorageDriver.createStorage(args[0], Integer.parseInt(args[1]));
		show("done");
				
		String key = "key #%s"; String value = "value #%s";		
		
		show("filling ...");
			StorageSession  w = null;
			try {			
				w = new StorageSession(args[0]);
				for (long i=0; i<Integer.parseInt(args[1]); i++) {
					w.set(String.format(key, i).getBytes(), String.format(value, i).getBytes());
				}
				show("count = " + w.count());
			} finally {
				if (w != null) {
					w.close();
				}
			}		
		show("done");
			
		
		show("searching ...");		
			w = new StorageSession(args[0]);
			long ss = Integer.parseInt(args[1]) - 1;
			while (ss != 0) {
				byte[] bytesKey  = String.format(key, ss).getBytes();
				long start = System.nanoTime();
				byte[] bytesValue = w.get(bytesKey);				
				long stop = System.nanoTime();
				show(new String(bytesKey) + " : " + (bytesValue != null ? new String(bytesValue) : "error") + " -> " + (stop - start)/1000 + " µs");
				ss >>=1;			
			}
			w.close();
		show("done");
		

		show("removing ...");		
			w = new StorageSession(args[0]);
			long rr = Integer.parseInt(args[1]) - 1;
			while (rr != 0) {
				byte[] bytesKey  = String.format(key, rr).getBytes();
				long start = System.nanoTime();
				boolean result = w.remove(bytesKey);				
				long stop = System.nanoTime();
				show(new String(bytesKey) + " : " + (result ? "removed" : "error") + " -> " + (stop - start)/1000 + " µs");
				rr >>=1;			
			}
			w.close();
		show("done");
		
		show("inserting ...");		
			w = new StorageSession(args[0]);
			show("count = " + w.count());
			long ii = Integer.parseInt(args[1]) - 1;
			while (ii != 0) {
				byte[] bytesKey  = String.format(key, ii).getBytes();
				byte[] bytesValue = String.format(value, ii).getBytes();
				long start = System.nanoTime();
				boolean result = w.set(bytesKey, bytesValue);				
				long stop = System.nanoTime();
				show(new String(bytesKey) + " : " + (result ? "inserted" : "error") + " -> " + (stop - start)/1000 + " µs");
				ii >>=1;			
			}
			show("count = " + w.count());
			w.close();
		show("done");
		
	}

}
