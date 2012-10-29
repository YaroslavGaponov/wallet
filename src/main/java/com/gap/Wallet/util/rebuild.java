package com.gap.wallet.util;

import com.gap.wallet.copyright;
import com.gap.wallet.storage.StorageDriver;
import com.gap.wallet.storage.WalletException;

import java.io.IOException;
import java.util.Date;


public class rebuild {
	
	private static void show(String message) {
		System.out.println(new Date() + " " + message);
	}
	
	public static void main(String[] args) throws WalletException, IOException {
		System.out.println(copyright.info);
		System.out.println("rebuild tool");
		
		if (args.length != 1) {
			System.out.println("help: rebuild [database]");
			return;
		}
		
		show("rebuilding ...");		
			StorageDriver.rebuild(args[0]);
		show("done");
	}

}
