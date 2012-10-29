package com.gap.wallet.storage;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class StorageDriver implements IWalletStorage {

	// database channel
	private final FileChannel channel;

	// logical database parts
	private final IHeader header;
	private final IBucket bucket;
	private final IStorage storage;

	// bucket table size
	private final int bucketSize;

	public StorageDriver(String filename) throws WalletException {
		assert (filename != null);

		Path path = Paths.get(filename);

		if (!path.toFile().exists()) {
			throw new WalletException("database file is not found.");
		}

		try {
			// get channel for database file
			channel = FileChannel.open
				(
					path, 
					StandardOpenOption.READ,					
					StandardOpenOption.WRITE
				);

			// mapping header and bucket structures
			header = new Header(channel);
			bucketSize = header.getBucketSize();
			bucket = new Bucket(channel, IHeader.header_offset_stop, bucketSize);

			// initialize data storage
			storage = new Storage(channel);

		} catch (IOException e) {
			throw new WalletException("Exception caught during WalletStorage.ctor: " + e.getMessage(), e);
		}
	}

	public void close() {
		try {
			if (channel != null && channel.isOpen()) {
				channel.close();
			}
		} catch (IOException e) {
		}
	}

	public static void createStorage(String filename, long records) throws WalletException {
		assert (filename != null);
		assert (records >= 0);

		FileChannel channel = null;
		try {
			// create database file
			channel = FileChannel.open
				(
						Paths.get(filename),
						StandardOpenOption.WRITE, 
						StandardOpenOption.READ,
						StandardOpenOption.CREATE
				);
	
			// calculate a bucket size base on planning count of records
			int bucketSize = Helper.getPrime(records);
	
			// initialize header
			Header header = new Header(channel);
			header.setIdentifier(IHeader.Identifier);
			header.setBucketSize(bucketSize);
			header.setVersion(version);
			header.setCount(0L);
	
			// initialize bucket table
			Bucket bucket = new Bucket
				(
						channel,
						IHeader.header_offset_start + IHeader.header_size,
						bucketSize
				);
			
			for (int i = 0; i < bucketSize; i++) {
				bucket.set(i, IStorage.EOF);
			}
	
			// close
			channel.close();
		
		} catch (IOException e) {
			throw new WalletException("Exception caught during WalletStorage.createStorage: " + e.getMessage(), e);
		} finally {
			if (channel != null && channel.isOpen()) {				
				try {
					channel.close();
				} catch (IOException e) {}
			}
		}
		
	}

	public static void rebuild(String filename) throws WalletException {
		assert (filename != null);

		// generate temp. filename
		String tmpfilename;
		try {
			tmpfilename = File.createTempFile("wallet", ".tmp").getAbsolutePath();
		} catch (IOException e) {
			throw new WalletException("Exception caught during WalletStorage.rebuild: " + e.getMessage(), e);
		}

		// open database filename
		StorageDriver src = new StorageDriver(filename);

		// create and open temp. filename
		StorageDriver.createStorage(tmpfilename, src.count());
		StorageDriver tmp = new StorageDriver(tmpfilename);

		// copy
		for (int i = 0; i < src.bucketSize; i++) {
			long offset = src.bucket.get(i);
			while (offset != IStorage.EOF) {
				byte[] key = src.storage.getKey(offset);
				byte[] value = src.storage.getValue(offset);
				tmp.set(key, value);
				offset = src.storage.getNextOffset(offset);
			}
		}

		// close
		tmp.close();
		src.close();

		// rename
		Paths.get(filename).toFile().delete();
		Paths.get(tmpfilename).toFile().renameTo(Paths.get(filename).toFile());
	}

	@SuppressWarnings("incomplete-switch")
	public boolean exists(byte[] key) throws WalletException {
		assert (key != null);
		assert (key.length > 0);

		int index = Helper.hashCode(key, bucketSize);
		assert(index >= 0);
		assert(index < bucketSize);

		long current = bucket.get(index);
		while (current != IStorage.EOF) {
			byte[] findKey = storage.getKey(current);
			switch (Helper.compare(key, findKey)) {
			case equal:
				return true;				
			case more:
				return false;
			}
			current = storage.getNextOffset(current);
		}
		return false;
	}

	@SuppressWarnings("incomplete-switch")
	public byte[] get(byte[] key) throws WalletException {
		assert (key != null);
		assert (key.length > 0);

		int index = Helper.hashCode(key, bucketSize);
		assert(index >= 0);
		assert(index < bucketSize);

		long current = bucket.get(index);
		while (current != IStorage.EOF) {
			byte[] findKey = storage.getKey(current);
			switch (Helper.compare(key, findKey)) {
				case equal:
					return storage.getValue(current);					
				case more:
					return null;
			}
			current = storage.getNextOffset(current);
		}
		return null;
	}

	@SuppressWarnings("incomplete-switch")
	public boolean set(byte[] key, byte[] value) throws WalletException {
		assert (key != null);
		assert (key.length > 0);

		int index = Helper.hashCode(key, bucketSize);
		assert(index >= 0);
		assert(index < bucketSize);
		
		long current = bucket.get(index);
		long previous = IStorage.EOF;
		while (current != IStorage.EOF) {
			byte[] findKey = storage.getKey(current);
			switch (Helper.compare(key, findKey)) {
				case equal:
					current = storage.getNextOffset(current);
					current = storage.save(key, value, current);
					saveLink(index, current, previous);
					return true;					
				case more:
					current = storage.save(key, value, current);
					saveLink(index, current, previous);
					header.countInc();
					return true;
			}
			previous = current;
			current = storage.getNextOffset(current);
		}

		current = storage.save(key, value, IStorage.EOF);
		saveLink(index, current, previous);
		header.countInc();
		return true;
	}

	@SuppressWarnings("incomplete-switch")
	public boolean remove(byte[] key) throws WalletException {
		assert (key != null);
		assert (key.length > 0);

		int index = Helper.hashCode(key, bucketSize);
		assert(index >= 0);
		assert(index < bucketSize);
				
		long current = bucket.get(index);
		long previous = IStorage.EOF;
		while (current != IStorage.EOF) {
			byte[] findKey = storage.getKey(current);
			switch (Helper.compare(key, findKey)) {
				case equal:
					current = storage.getNextOffset(current);
					saveLink(index, current, previous);
					header.countDec();
					return true;
				case more:
					return false;
			}
			previous = current;
			current = storage.getNextOffset(current);
		}
		return false;
	}

	public long count() {
		return header.getCount();
	}

	public long iterator(IterationAction iterationAction) throws WalletException {
		long recs = 0;
		for (int i = 0; i < bucketSize; i++) {
			long offset = bucket.get(i);
			while (offset != IStorage.EOF) {
				recs++;
				byte[] key = storage.getKey(offset);
				byte[] value = storage.getValue(offset);
				boolean resume = iterationAction.fire(key, value);
				if (!resume) {
					return recs;
				}
				offset = storage.getNextOffset(offset);
			}
		}
		return recs;
	}

	private void saveLink(int index, long current, long previous) throws WalletException {
		assert(index >= 0); assert(index <= bucketSize);
		
		if (previous == IStorage.EOF) {
			bucket.set(index, current);
		} else {
			storage.setNextOffset(previous, current);
		}
	}

}
