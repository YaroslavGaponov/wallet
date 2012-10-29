package com.gap.Wallet.util;

import java.util.Date;

import com.gap.Wallet.Storage.StorageDriver;

public class build {

	private static void show(String message) {
		System.out.println(new Date() + " " + message);
	}
	
	public static void main(String[] args) {
		System.out.println("wallet - build");
		if (args.length != 2) {
			System.out.println("build {database} {rows}");
			return;
		}

		String filename = args[0];
		long records = Integer.parseInt(args[1]);
		
		show("creating ...");
			StorageDriver.createStorage(filename, records);
		show("done");
		
	}

}
