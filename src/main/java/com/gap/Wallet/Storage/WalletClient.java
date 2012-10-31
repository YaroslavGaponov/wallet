package com.gap.wallet.storage;

import java.util.HashMap;
import java.util.Map;

import com.gap.wallet.exception.WalletException;

public class WalletClient {
	
	private final Session session;
	private boolean transaction = false;
	private long counter;
	
	// transaction logs
	private enum Operation { set, remove };
	private Map<Long, Operation> operation = null;
	private Map<Long, byte[]> keys = null;
	private Map<Long, byte[]> values = null;
	
	public WalletClient(String filename) throws WalletException {
		session = Session.createSession(filename);
		
		// transaction logs
		operation = new HashMap<Long, Operation>();
		keys = new HashMap<Long, byte[]>();
		values = new HashMap<Long, byte[]>();
	}

	public void configureBlocking(boolean blocking) {
		session.configureBlocking(blocking);
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
		counter  = session.count();		
		
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
					session.set(key, value);
					break;
				case remove:
					key = keys.get(hash);
					session.remove(key);
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
			long hash = hashCodeLong(key);
			
			// if we can't work with this key - get from storage
			if (!operation.containsKey(hash)) {
				return session.exists(key);
			}
			
			// if we worked early
			switch (operation.get(hash)) {
				case set:
					return true;
				case remove:
					return false;
			}
		}
		
		return session.exists(key);
	}

	public byte[] get(byte[] key) throws WalletException {
		if (transaction) {
			//create hash code for key
			long hash = hashCodeLong(key);
			
			// if we can't work with this key - get from storage
			if (!operation.containsKey(hash)) {
				return session.get(key);
			}
			
			// if we worked early
			return 
				operation.get(hash) == Operation.set 
					?  values.get(hash) :  null;
		}
		
		return session.get(key);
	}

	public boolean set(byte[] key, byte[] value) throws WalletException {
		if (transaction) {						
			// create hash code for key
			long hash = hashCodeLong(key);
			
			// if this key is new - increment count variable
			if 
			(
					!(
							(operation.containsKey(hash) && (operation.get(hash) == Operation.set)) 
							|| 
							(session.exists(key))
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
		return session.set(key, value);
	}

	public boolean remove(byte[] key) throws WalletException {
		if (transaction) {
			// create hash code for key
			long hash = hashCodeLong(key);
			
			// if this key exists - decrement count variable
			if 
			(
				(operation.containsKey(hash) && (operation.get(hash) == Operation.set))
				|| 
				(session.exists(key))
			) 
			{ counter--; }
			
			// add information to transaction log
			operation.put(hash, Operation.remove);
			keys.put(hash, key);
			
			// exit successful
			return true;
		}
		
		// remove without transaction
		return session.remove(key);
	}

	public long count() {
		return 
			transaction 
				? counter : session.count();
	}

	public void close() {
		 session.close();
	}
	
	
	public static long hashCodeLong(byte[] key) {
	    long hash = 0;
	    
	    for(int i = 0; i < key.length; i++) {
	        hash += key[i];
	        hash += (hash << 10);
	        hash ^= (hash >> 6);
	    }
	    hash += (hash << 3);
	    hash ^= (hash >> 11);
	    hash += (hash << 15);
	    
	    return hash;
	}

}
