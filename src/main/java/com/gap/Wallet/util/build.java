package com.gap.wallet.util;

import java.util.Date;

import com.gap.wallet.copyright;
import com.gap.wallet.storage.StorageDriver;

public class build {

	private static void show(String message) {
		System.out.println(new Date() + " " + message);
	}
	
	public static void main(String[] args) {
		System.out.println(copyright.info);
		System.out.println("build tool");
		
		if (args.length != 2) {
			System.out.println("help: build [database] [records]");
			return;
		}

		String filename = args[0];
		long records = Integer.parseInt(args[1]);
		
		show("creating ...");
			StorageDriver.createStorage(filename, records);
		show("done");
		
	}

}
