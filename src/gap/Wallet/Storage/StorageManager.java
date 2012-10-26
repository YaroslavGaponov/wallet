package gap.Wallet.Storage;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

class StorageManager implements IWalletStorage {
	
	// pool for database file drivers
	private final static Map<String,StorageDriver> drivers = new HashMap<String,StorageDriver>();
	private final static Map<String, Integer> clients = new HashMap<String, Integer>();
	
	// blocking or nonblocking mode
	private static boolean blocking = true; 
	 
	// session variables
	private final String storagefile;
	private final IWalletStorage driver;
	
	private StorageManager(String storagefile) {
		this.storagefile = storagefile;
		this.driver = drivers.get(storagefile);
	}
	
	public static void configureBlocking(boolean blocking) {
		StorageManager.blocking = blocking;
	}
	
	public static synchronized StorageManager createSession(String filename) {
		assert(filename != null);
		
		// initialize driver for new database file 
		String storagefile = Paths.get(filename).toFile().getAbsolutePath();
		if (!drivers.containsKey(storagefile)) {
			drivers.put(storagefile, new StorageDriver(storagefile));
		}
		
		// initialize or increment clients counter
		if (clients.containsKey(storagefile)) {
			clients.put(storagefile, clients.get(storagefile) + 1);
		} else {
			clients.put(storagefile, 1);
		}
		
		return new StorageManager(storagefile);
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
