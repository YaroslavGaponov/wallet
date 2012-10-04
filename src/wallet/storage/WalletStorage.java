package wallet.storage;

import java.io.*;


public class WalletStorage implements WalletHeader, WalletConst {
		
	private final RandomAccessFile db;
	private final int size;

	/**
	 *  constructor
	 * @param filename
	 * @throws WalletException
	 */
	public WalletStorage(String filename) throws WalletException {
		// file must be present
		File file = new File(filename);
		if (!file.exists()) {
			throw new WalletException("Database file is not found.");
		}

		try {
			db = new RandomAccessFile(filename, "rw");
			
			// check sign
			String sign = getFileSign();
			if (!sign.equals(SIGN)) {
				throw new WalletException("Database file has the wrong format.");
			}

			// get buckets table size
			size = getFileTableSize();
		} catch (IOException e) {
			WalletException ex = new WalletException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * close database file
	 */
	public void close() {
		try {
			if (db != null) db.close();
		} catch (IOException e) {
		}
	}

	/**
	 * get record count
	 * @return
	 * @throws WalletException
	 */
	public long count() throws WalletException {
		try {
			db.seek(HEADER_COUNT_OFFSET);
			return db.readLong();
		} catch (IOException e) {
			WalletException ex = new WalletException(e.getMessage());
			ex.initCause(e);
			throw ex;			
		}
	}	
	
	/**
	 * save record
	 * @param key
	 * @param value
	 * @throws WalletException
	 */
	public void put(String key, byte[] value) throws WalletException {		
		int index = hash(key) % size;		
		try {
			// get first element offset
			db.seek((index << 3) + BUCKETS_START_OFFSET);
			long offset = db.readLong();

			// set to bucket table offset for new element 
			db.seek((index << 3) + BUCKETS_START_OFFSET);
			db.writeLong(db.length());

			// save a new element
			db.seek(db.length());
			db.writeLong(offset);
			db.writeInt(key.length());
			db.writeBytes(key);
			db.writeInt(value.length);
			db.write(value);

			// increment record count 
			db.seek(HEADER_COUNT_OFFSET);
			long count = db.readLong();
			db.seek(HEADER_COUNT_OFFSET);
			db.writeLong(count + 1);
			
		} catch (IOException e) {
			WalletException ex = new WalletException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * check exists record or not 
	 * @param key
	 * @return
	 * @throws WalletException
	 */
	public boolean exists(String key) throws WalletException {
		int index = hash(key) % size;
		try {
			// get offset for first element
			long offset = getNext((index << 3) + BUCKETS_START_OFFSET);
			// search
			while (offset != EOF) {
				// get key for element
				String key2 = getKey(offset);
				// compare keys
				if (key.equals(key2)) {
					return true;
				}
				// get next offset
				offset = getNext(offset);
			}
			return false;
		} catch (IOException e) {
			WalletException ex = new WalletException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * get record by key
	 * @param key
	 * @return
	 * @throws WalletException
	 */
	public byte[] get(String key) throws WalletException {
		int index = hash(key) % size;
		try {
			// get offset for first element
			long offset = getNext((index << 3) + BUCKETS_START_OFFSET);
			// search
			while (offset != EOF) {
				// get key for element
				String key2 = getKey(offset);
				// compare keys
				if (key.equals(key2)) {
					return getValue(offset);
				}
				// get next offset
				offset = getNext(offset);
			}
			return null;
		} catch (IOException e) {
			WalletException ex = new WalletException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * fast delete record by key 
	 * @param key
	 * @throws WalletException
	 */
	public void remove(String key) throws WalletException {
		int index = hash(key) % size;
		try {
			// get offset for first element
			long offset = getNext((index << 3) + BUCKETS_START_OFFSET);
			// keep previous offset
			long prev = offset;
			// search
			while (offset != EOF) {
				// get key
				String key2 = getKey(offset);
				// compare keys
				if (key.equals(key2)) {
					// save to previous element link for next element
					long next = getNext(offset);
					db.seek(prev);
					db.writeLong(next);

					// decrement record count
					db.seek(HEADER_COUNT_OFFSET);
					long count = db.readLong();
					db.seek(HEADER_COUNT_OFFSET);
					db.writeLong(count - 1);

					return;
				}
				// keep previous offset
				prev = offset;
				// get next offset
				offset = getNext(offset);
			}
		} catch (IOException e) {
			WalletException ex = new WalletException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	public void iterator(Callback callback) throws WalletException {
		for(int index=0; index<size; index++) {
			try {
				// get offset for first element
				long offset = getNext((index << 3) + BUCKETS_START_OFFSET);
				// search
				while (offset != EOF) {
					// get key for element
					String key = getKey(offset);
					// return element
					callback.run(key, getValue(offset));
					// get next offset
					offset = getNext(offset);
				}				
			} catch (IOException e) {
				WalletException ex = new WalletException(e.getMessage());
				ex.initCause(e);
				throw ex;
			}						
		}
	}
	
	/**
	 * create empty database
	 * @param filename
	 * @param tablesize
	 * @return
	 * @throws WalletException
	 */
	public static WalletStorage create(String filename, int tablesize) throws WalletException {
		// delete file
		File file = new File(filename);
		if (file.exists()) {
			file.delete();
		}

		RandomAccessFile db = null;
		try {
			db = new RandomAccessFile(filename, "rw");

			// write header
			// 1] write sign
			db.seek(HEADER_SIGN_OFFSET);
			db.writeBytes(SIGN);
			// 2] write bucket table size
			db.writeInt(tablesize);
			// 3] write record count
			db.writeLong(0L);
			// 4] create bucket table
			for(int i=0;i<tablesize; i++) {
				db.writeLong(EOF);
			}			
		} catch (IOException e) {
			WalletException ex = new WalletException(e.getMessage());
			ex.initCause(e);
			throw ex;
		} finally {
			try {
				if (db != null) db.close();
			} catch (IOException e) {
			}
		}
		
		// return WalletStorage object
		return new WalletStorage(filename);
	}

	/**
	 * rebuild database
	 * @param filename
	 * @param size
	 * @throws WalletException
	 */
	public static void rebuild(String filename, int size, Callback callback) throws WalletException {
		WalletStorage source = null;
		WalletStorage target = null;
		try {
			source = new WalletStorage(filename);
			target = WalletStorage.create(filename + ".temp", size);
			long total = source.count(); 
			long processed = 0;			
			for (int i = 0; i < source.size; i++) {
				long offset = source.getNext((i << 3) + BUCKETS_START_OFFSET);
				while (offset != EOF) {
					String key = source.getKey(offset);
					byte[] data = source.getValue(offset);
					target.put(key, data);
					processed++;
					if (callback != null) {
						callback.run(total, processed, processed * 100 / total);
					}
					offset = source.getNext(offset);
				}
			}
		} catch (IOException e) {
			WalletException ex = new WalletException(e.getMessage());
			ex.initCause(e);
			throw ex;
		} finally {
			source.close();
			target.close();
		}

		// delete old file and rename new file
		File sourceFile = new File(filename);
		sourceFile.delete();
		File targetFile = new File(filename + ".temp");
		targetFile.renameTo(sourceFile);
	}

	/**
	 * get sign from file
	 * @return
	 * @throws IOException
	 */
	private String getFileSign() throws IOException {
		db.seek(HEADER_SIGN_OFFSET);
		byte[] buf = new byte[SIGN.length()];
		db.read(buf);
		return new String(buf);
	}

	/**
	 * get bucket table size
	 * @return
	 * @throws IOException
	 */
	private int getFileTableSize() throws IOException {
		db.seek(HEADER_BUCKETSSIZE_OFFSET);
		return db.readInt();
	}

	/**
	 * get next record offset 
	 * @param offset
	 * @return
	 * @throws IOException
	 */
	private long getNext(long offset) throws IOException {
		db.seek(offset);
		return db.readLong();
	}

	/**
	 * get key
	 * @param offset
	 * @return
	 * @throws IOException
	 */
	private String getKey(long offset) throws IOException {
		db.seek(offset + 8);
		int size = db.readInt();
		byte[] data = new byte[size];
		db.read(data);
		return new String(data);
	}

	/**
	 * get value
	 * @param offset
	 * @return
	 * @throws IOException
	 */
	private byte[] getValue(long offset) throws IOException {
		db.seek(offset + 8);
		int size = db.readInt();
		db.seek(offset + size + 12);
		size = db.readInt();
		byte[] data = new byte[size];
		db.read(data);
		return data;
	}

	/**
	 * get hash for key
	 * @param key
	 * @return
	 */
	private int hash(String key) {
		int hash = 0;
		for (int i = 0; i < key.length(); i++) {
			hash = (hash << 5) - hash + key.charAt(i);
		}
		hash = (hash & Integer.MAX_VALUE) | 1;
		return hash;
	}

}
