package com.gap.wallet.storage;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.gap.wallet.exception.WalletException;
import com.gap.wallet.storage.driver.Driver;

class Session {
	
	// pool for database file drivers
	private final static Map<String,Driver> drivers = new HashMap<String,Driver>();
	private final static Map<String, Integer> clients = new HashMap<String, Integer>();
	
	// blocking or nonblocking mode
	private boolean blocking = false; 
	 
	// session variables
	private final String storagefile;
	private final Driver driver;
	
	private Session(String storagefile) {
		this.storagefile = storagefile;
		this.driver = drivers.get(storagefile);
	}
	
	public void configureBlocking(boolean blocking) {
		this.blocking = blocking;
	}
	
	public static synchronized Session createSession(String filename) {
		assert(filename != null);
		
		// initialize driver for new database file 
		String storagefile = Paths.get(filename).toFile().getAbsolutePath();
		if (!drivers.containsKey(storagefile)) {
			drivers.put(storagefile, new Driver(storagefile));
		}
		
		// initialize or increment clients counter
		if (clients.containsKey(storagefile)) {
			clients.put(storagefile, clients.get(storagefile) + 1);
		} else {
			clients.put(storagefile, 1);
		}
		
		return new Session(storagefile);
	}

	public boolean exists(byte[] key) throws WalletException {
		if (blocking && (clients.get(storagefile) > 1)) {			
			synchronized (driver) {
				return driver.exists(key);
			}
		}
		return driver.exists(key);
	}


	public byte[] get(byte[] key) throws WalletException {;
		if (blocking && (clients.get(storagefile) > 1)) {			
			synchronized (driver) {
				return driver.get(key);
			}
		}
		return driver.get(key);

	}

	public boolean set(byte[] key, byte[] value) throws WalletException {
		if (blocking && (clients.get(storagefile) > 1)) {			
			synchronized (driver) {
				return driver.set(key, value);
			}
		}
		return driver.set(key, value);

	}

	public boolean remove(byte[] key) throws WalletException {
		if (blocking && (clients.get(storagefile) > 1)) {			
			synchronized (driver) {
				return driver.remove(key);
			}
		}
		return driver.remove(key);

	}

	public long count() {
		if (blocking && (clients.get(storagefile) > 1)) {			
			synchronized (driver) {
				return driver.count();
			}
		}
		return driver.count();

	}

	public void close() {
		if (clients.containsKey(storagefile)) {
			clients.put(storagefile, clients.get(storagefile) - 1);
			if (clients.get(storagefile).equals(0)) {
				driver.close();
				drivers.remove(storagefile);
				clients.remove(storagefile);
			}
		}
	}

}
