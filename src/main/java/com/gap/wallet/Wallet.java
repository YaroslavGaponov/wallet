package com.gap.wallet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import com.gap.wallet.core.WalletSocketServer;
import com.gap.wallet.deamon.Daemonizer;


public class Wallet {

	public static void main(String[] args) throws InvalidPropertiesFormatException, IOException {
		System.out.println(Copyright.info);
		System.out.println("wallet - demon");
		
		if (System.getenv("WALLET") == null) {
			halt("set WALLET environment variable to solution folder");
		}
		
	    Properties config = new Properties();
	    config.loadFromXML(new FileInputStream(System.getenv("WALLET") + "/etc/wallet.xml"));
	    
	    if (args.length > 0) {	    
		    switch (args[0]) {
			    case "start":
			    	start();
			    	break;
			    	
			    case "stop":		    	
			    	stop
			    	(
			    		Integer.parseInt(config.getProperty("shutdown"))
			    	);
			    	break;
			    	
		    	default:
		    		help();
		    }
	    }
	    else {
	    	run
	    	(
    			Integer.parseInt(config.getProperty("port")), 
    			config.getProperty("path"), 
    			Integer.parseInt(config.getProperty("shutdown"))
	    	);
	    }	    
	}

	private static void halt(String err) {
		System.out.println(err);
		System.exit(-1);	
	}

	private static void start() throws IOException {
		Runtime.getRuntime().exec("java -jar " + System.getenv("WALLET") + "/bin/wallet.jar");
	}

	private static void run(int port,String path,int shutdown) throws IOException {	
		Daemonizer.start(new WalletSocketServer(port, path), shutdown); 
	}
	
	private static void stop(int shutdown) {
		Daemonizer.stop(shutdown);
	}

	
	private static void help() {
		System.out.println("help: wallet [start|stop]");
		System.exit(-1);
	}
		
}
