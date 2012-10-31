package com.gap.wallet.storage.driver;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.gap.wallet.exception.WalletException;

public class Driver {

	public static final long version = 1;
	
	// database channel
	private final FileChannel channel;

	// logical database parts
	private final FileHeader header;
	private final FileBucket bucket;
	private final FileSpace  space;

	// bucket table size
	private final int bucketSize;

	public Driver(String filename) throws WalletException {
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
			header = new FileHeader(channel);
			bucketSize = header.getBucketSize();
			bucket = new FileBucket(channel, FileHeader.header_offset_stop, bucketSize);

			// initialize data storage
			space = new FileSpace(channel);

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
			int bucketSize = Hashcode.getPrime(records);
	
			// initialize header
			FileHeader header = new FileHeader(channel);
			header.setIdentifier(FileHeader.Identifier);
			header.setBucketSize(bucketSize);
			header.setVersion(version);
			header.setCount(0L);
	
			// initialize bucket table
			FileBucket bucket = new FileBucket
				(
						channel,
						FileHeader.header_offset_start + FileHeader.header_size,
						bucketSize
				);
			
			for (int i = 0; i < bucketSize; i++) {
				bucket.set(i, FileSpace.EOF);
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
		Driver src = new Driver(filename);

		// create and open temp. filename
		Driver.createStorage(tmpfilename, src.count());
		Driver tmp = new Driver(tmpfilename);

		// copy
		for (int i = 0; i < src.bucketSize; i++) {
			long offset = src.bucket.get(i);
			while (offset != FileSpace.EOF) {
				byte[] key = src.space.getKey(offset);
				byte[] value = src.space.getValue(offset);
				tmp.set(key, value);
				offset = src.space.getNextOffset(offset);
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

		int index = Hashcode.hashCode(key, bucketSize);
		assert(index >= 0);
		assert(index < bucketSize);

		long current = bucket.get(index);
		while (current != FileSpace.EOF) {
			byte[] findKey = space.getKey(current);
			switch (Hashcode.compare(key, findKey)) {
			case equal:
				return true;				
			case more:
				return false;
			}
			current = space.getNextOffset(current);
		}
		return false;
	}

	@SuppressWarnings("incomplete-switch")
	public byte[] get(byte[] key) throws WalletException {
		assert (key != null);
		assert (key.length > 0);

		int index = Hashcode.hashCode(key, bucketSize);
		assert(index >= 0);
		assert(index < bucketSize);

		long current = bucket.get(index);
		while (current != FileSpace.EOF) {
			byte[] findKey = space.getKey(current);
			switch (Hashcode.compare(key, findKey)) {
				case equal:
					return space.getValue(current);					
				case more:
					return null;
			}
			current = space.getNextOffset(current);
		}
		return null;
	}

	@SuppressWarnings("incomplete-switch")
	public boolean set(byte[] key, byte[] value) throws WalletException {
		assert (key != null);
		assert (key.length > 0);

		int index = Hashcode.hashCode(key, bucketSize);
		assert(index >= 0);
		assert(index < bucketSize);
		
		long current = bucket.get(index);
		long previous = FileSpace.EOF;
		while (current != FileSpace.EOF) {
			byte[] findKey = space.getKey(current);
			switch (Hashcode.compare(key, findKey)) {
				case equal:
					current = space.getNextOffset(current);
					current = space.save(key, value, current);
					saveLink(index, current, previous);
					return true;					
				case more:
					current = space.save(key, value, current);
					saveLink(index, current, previous);
					header.countInc();
					return true;
			}
			previous = current;
			current = space.getNextOffset(current);
		}

		current = space.save(key, value, FileSpace.EOF);
		saveLink(index, current, previous);
		header.countInc();
		return true;
	}

	@SuppressWarnings("incomplete-switch")
	public boolean remove(byte[] key) throws WalletException {
		assert (key != null);
		assert (key.length > 0);

		int index = Hashcode.hashCode(key, bucketSize);
		assert(index >= 0);
		assert(index < bucketSize);
				
		long current = bucket.get(index);
		long previous = FileSpace.EOF;
		while (current != FileSpace.EOF) {
			byte[] findKey = space.getKey(current);
			switch (Hashcode.compare(key, findKey)) {
				case equal:
					current = space.getNextOffset(current);
					saveLink(index, current, previous);
					header.countDec();
					return true;
				case more:
					return false;
			}
			previous = current;
			current = space.getNextOffset(current);
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
			while (offset != FileSpace.EOF) {
				recs++;
				byte[] key = space.getKey(offset);
				byte[] value = space.getValue(offset);
				boolean resume = iterationAction.fire(key, value);
				if (!resume) {
					return recs;
				}
				offset = space.getNextOffset(offset);
			}
		}
		return recs;
	}

	private void saveLink(int index, long current, long previous) throws WalletException {
		assert(index >= 0); assert(index <= bucketSize);
		
		if (previous == FileSpace.EOF) {
			bucket.set(index, current);
		} else {
			space.setNextOffset(previous, current);
		}
	}

}
