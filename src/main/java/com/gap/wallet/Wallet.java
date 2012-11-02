package com.gap.wallet;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import com.gap.wallet.deamon.Daemon;

public class Wallet {

	public static void main(String[] args) throws IOException, URISyntaxException {
		System.out.println(Copyright.info);
		System.out.println("wallet - demon");

		String solutionPath = System.getenv("WALLET"); 
		
		if (solutionPath == null) {
			halt("Please set environment variable WALLET");
		}
		
		FileInputStream fileConfig = new FileInputStream(solutionPath + "/etc/wallet.xml");
	    Properties config = new Properties();
	    config.loadFromXML(fileConfig);

	    if (args.length > 0) {	    
		    switch (args[0]) {
		    case "run":
		    	int port = Integer.parseInt(config.getProperty("port"));
		    	String path = config.getProperty("path");
		    	int shutdown =  Integer.parseInt(config.getProperty("shutdown"));
		    	Daemon daemon = new Daemon(port, path, shutdown);
				daemon.start();	    	
		    case "start":
		    	String java  = "java -jar";
		    	String jar = solutionPath + "/bin/wallet.jar";
		    	String command = "run";
		    	System.out.println(String.format("%s %s %s", java, jar, command));
		    	Runtime.getRuntime().exec(String.format("%s \"%s\" %s", java, jar, command));
		    	break;
		    case "stop":		    	
		    	Daemon.stopDaemon(Integer.parseInt(config.getProperty("shutdown")));
		    	break;
		    }
	    }
	    else {
	    	halt("help: wallet [start|stop|run]");
	    }	    
	}

	private static void halt(String message) {
		System.out.println(message);
		System.exit(-1);
	}
		
}
