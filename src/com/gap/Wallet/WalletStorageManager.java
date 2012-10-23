package com.gap.Wallet;

import java.util.HashMap;
import java.util.Map;

public class WalletStorageManager implements IWalletStorage, ITransaction {
	
	private WalletStorage walletStorage = null;
	private boolean transaction = false;
	private long counter;
	
	// transaction logs
	private enum Operation { set, remove };
	private Map<Long, Operation> operation = null;
	private Map<Long, byte[]> keys = null;
	private Map<Long, byte[]> values = null;
	
	public WalletStorageManager(String filename) throws WalletException {
		walletStorage = new WalletStorage(filename);
		
		// transaction logs
		operation = new HashMap<Long, Operation>();
		keys = new HashMap<Long, byte[]>();
		values = new HashMap<Long, byte[]>();
	}

	public void start() throws WalletException {
		if (transaction) {
			throw new WalletException("Transaction has already started."); 
		}
		
		// initialize transaction structures and variables		
		operation.clear();
		keys.clear();
		values.clear();
		
		// save records counter
		counter  = walletStorage.count();		
		
		// start transaction
		transaction = true;
	}
		
	public void commit() throws WalletException {
		if (!transaction) {
			throw new WalletException("Transaction has not started.");
		}
		
		// stop transaction
		transaction = false;
		
		// commit all changes 
		byte[] key, value;		
		for(Long hash : operation.keySet()) {
			switch (operation.get(hash)) {
				case set:
					key = keys.get(hash);
					value = values.get(hash);
					walletStorage.set(key, value);
					break;
				case remove:
					key = keys.get(hash);
					walletStorage.remove(key);
					break;
			}
		}
	}

	public void rollback() throws WalletException {
		if (!transaction) {
			throw new WalletException("Transaction has not started.");
		}
		
		// stop transaction
		transaction = false;		
	}

	public boolean exists(byte[] key) throws WalletException {
		if (transaction) {
			//create hash code for key
			long hash = Helper.hashCodeLong(key);
			
			// if we can't work with this key - get from storage
			if (!operation.containsKey(hash)) {
				return walletStorage.exists(key);
			}
			
			// if we worked early
			switch (operation.get(hash)) {
				case set:
					return true;
				case remove:
					return false;
			}
		}
		
		return walletStorage.exists(key);
	}

	public byte[] get(byte[] key) throws WalletException {
		if (transaction) {
			//create hash code for key
			long hash = Helper.hashCodeLong(key);
			
			// if we can't work with this key - get from storage
			if (!operation.containsKey(hash)) {
				return walletStorage.get(key);
			}
			
			// if we worked early
			switch (operation.get(hash)) {
				case set:				
					byte[] value = values.get(hash);
					return value;				
				case remove:
					return null;
			}
		}
		
		return walletStorage.get(key);
	}

	public boolean set(byte[] key, byte[] value) throws WalletException {
		if (transaction) {						
			// create hash code for key
			long hash = Helper.hashCodeLong(key);
			
			// if this key is new - increment count variable
			if 
			(
					!(
							(operation.containsKey(hash) && (operation.get(hash) == Operation.set)) 
							|| 
							(walletStorage.exists(key))
					)
			) 
			{ counter++; }
			
			// add record to transaction log 
			operation.put(hash, Operation.set);
			keys.put(hash, key);
			values.put(hash, value);
						
			// exit successful
			return true;
		}
		
		// set without transaction
		return walletStorage.set(key, value);
	}

	public boolean remove(byte[] key) throws WalletException {
		if (transaction) {
			// create hash code for key
			long hash = Helper.hashCodeLong(key);
			
			// if this key exists - decrement count variable
			if 
			(
				(operation.containsKey(hash) && (operation.get(hash) == Operation.set))
				|| 
				(walletStorage.exists(key))
			) 
			{ counter--; }
			
			// add information to transaction log
			operation.put(hash, Operation.remove);
			keys.put(hash, key);
			
			// exit successful
			return true;
		}
		
		// remove without transaction
		return walletStorage.remove(key);
	}

	public long count() {
		return 
				transaction 
				? 
				counter : walletStorage.count();
	}

	public void close() {
		 walletStorage.close();
	}

}
