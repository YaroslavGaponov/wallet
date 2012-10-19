package com.gap.Wallet;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class WalletStorage implements IWalletStorage {

	// database channel
	private final FileChannel channel;

	// logical database parts
	private final IHeader header;
	private final IBucket bucket;
	private final IStorage storage;

	// bucket table size
	private final int bucketSize;

	public WalletStorage(String filename) throws WalletException {
		assert(filename != null);
		
		Path path = Paths.get(filename);

		if (! path.toFile().exists()) {
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

			// mapping header and bucket  structures
			header = new Header(channel);
			bucketSize = header.getBucketSize();
			bucket = new Bucket(channel, IHeader.header_offset_stop, bucketSize);
			
			// initialize data storage 
			storage = new Storage(channel);

		} catch (IOException e) {
			WalletException ex = new WalletException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	public void close() {
		try {
			if (channel != null) {
				channel.close();
			}
		} catch (IOException e) {
		}
	}

	public static void createStorage(String filename, long records)
			throws IOException, WalletException {
		
		assert(filename != null);
		assert(records >= 0);
		
		// create database file
		FileChannel channel = FileChannel.open(Paths.get(filename),
				StandardOpenOption.WRITE, 
				StandardOpenOption.READ,
				StandardOpenOption.CREATE);

		// calculate a bucket size base on planning count of records
		int bucketSize = Helper.getPrime(records);

		// initialize header
		Header header = new Header(channel);
		header.setIdentifier(IHeader.Identifier);
		header.setBucketSize(bucketSize);
		header.setVersion(version);
		header.setCount(0L);

		//  initialize bucket table
		Bucket bucket = new Bucket(channel, IHeader.header_offset_start + IHeader.header_size, bucketSize);
		for (int i = 0; i < bucketSize; i++) {
			bucket.set(i, IStorage.EOF);
		}

		// close
		channel.close();
	}

	public static void rebuild(String filename) throws WalletException, IOException {
		assert(filename != null);
		
		// generate temp filename		
		String tmpfilename = File.createTempFile("wallet", ".tmp").getAbsolutePath();
		
		// open database filename
		WalletStorage src = new WalletStorage(filename);
		
		// create and open temp filename
		WalletStorage.createStorage(tmpfilename, src.count());
		WalletStorage tmp = new WalletStorage(tmpfilename);
		
		// copy
		for (int i=0; i<src.bucketSize; i++) {
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
	
	public byte[] get(byte[] key) throws IOException {
		assert(key != null);
		assert(key.length > 0);
		
		int index = Helper.hashCode(key, bucketSize);
		long offset = bucket.get(index);
		while (offset != IStorage.EOF) {
			byte[] findKey = storage.getKey(offset);
			if (Helper.compare(key, findKey)) {
				return storage.getValue(offset);
			}
			offset = storage.getNextOffset(offset);
		}
		return null;
	}

	public boolean set(byte[] key, byte[] value) throws IOException {
		assert(key != null);
		assert(key.length > 0);
		
		int index = Helper.hashCode(key, bucketSize);
		long offset = bucket.get(index);
		offset = storage.save(key, value, offset);
		bucket.set(index, offset);
		header.countInc();
		
		return true;
	}

	public boolean remove(byte[] key) throws IOException {
		assert(key != null);
		assert(key.length > 0);
		
		int index = Helper.hashCode(key, bucketSize);
		long offset = bucket.get(index);
		long pred = IStorage.EOF;
		while (offset != IStorage.EOF) {
			byte[] akey = storage.getKey(offset);
			if (Helper.compare(key, akey)) {
				if (pred == IStorage.EOF) {
					bucket.set(index, storage.getNextOffset(offset));
				} else {
					storage.setNextOffset(pred, storage.getNextOffset(offset));
				}
				header.countDec();
				return true;
			}
			pred = offset;
			offset = storage.getNextOffset(offset);
		}
		return false;
	}

	public long count() {
		return header.getCount();
	}
	
	public long iterator(IterationAction iterationAction) throws IOException {
		long recs = 0;
		for (int i=0; i<bucketSize; i++) {
			long offset = bucket.get(i);
			while (offset != IStorage.EOF) {
				recs++;
				byte[] key = storage.getKey(offset);
				byte[] value = storage.getValue(offset);
				boolean resume = iterationAction.fire(key, value);
				if (! resume) { 
					return recs;		
				}
				offset = storage.getNextOffset(offset);
			}
		}
		return recs;		
	}


}
