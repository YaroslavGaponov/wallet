package com.gap.wallet.util;

import com.gap.wallet.Copyright;
import com.gap.wallet.exception.WalletException;
import com.gap.wallet.storage.driver.Driver;


import java.io.IOException;
import java.util.Date;


public class Rebuild {
	
	private static void show(String message) {
		System.out.println(new Date() + " " + message);
	}
	
	public static void main(String[] args) throws WalletException, IOException {
		System.out.println(Copyright.info);
		System.out.println("rebuild tool");
		
		if (args.length != 1) {
			System.out.println("help: rebuild [database]");
			return;
		}
		
		show("rebuilding ...");		
			Driver.rebuild(args[0]);
		show("done");
	}

}
