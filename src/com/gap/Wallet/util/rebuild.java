package com.gap.Wallet.util;

import java.io.IOException;
import java.util.Date;

import com.gap.Wallet.WalletException;
import com.gap.Wallet.WalletStorage;

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
			WalletStorage.rebuild(args[0]);
		show("done");
	}

}
