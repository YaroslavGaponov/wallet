package wallet;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Storage {
	private static int COUNT_OFFSET = 10;
	private static int TABLE_SIZE_OFFSET = 6;
	private static int TABLE_START_OFFSET = 18;
	
	private final int TABLE_SIZE;
	
	private RandomAccessFile raf;
	
	public Storage(String filename) throws WalletException {
		File file = new File(filename);
		if (!file.exists()) {
			throw new WalletException("File is not found");
		}
		
		try {
			raf = new RandomAccessFile(filename, "rw");
		
			String sign = getHeaderSign();
			if (!sign.equals("wallet")) {
				throw new WalletException("Format is not right");
			}
			
			TABLE_SIZE = getHeaderTableSize();
		} catch (IOException e) {
			WalletException ex = new WalletException("Some error in read header");
			ex.initCause(e);
			throw ex;
		}
	}

	private String getHeaderSign() throws IOException {
		raf.seek(0);
		byte[] buf = new byte[6];
		raf.read(buf);
		return new String(buf);
	}
	
	private int getHeaderTableSize() throws IOException {
		raf.seek(TABLE_SIZE_OFFSET);
		return raf.readInt();
	}
	
	public long count() throws IOException {
		raf.seek(COUNT_OFFSET);
		return raf.readLong();
	}


	
	public void close() {
		try {
			raf.close();
		} catch (IOException e) {
		}
	}
				
	public void save(String key, byte[] data) throws IOException {
		int index = hash(key);
		
		raf.seek((index<<3)+TABLE_START_OFFSET);
		long offset = raf.readLong();
		
		raf.seek((index<<3)+TABLE_START_OFFSET);
		raf.writeLong(raf.length());
		
		raf.seek(raf.length());
		raf.writeLong(offset);
		raf.writeInt(key.length());
		raf.writeBytes(key);
		raf.writeInt(data.length);
		raf.write(data);
		
		raf.seek(COUNT_OFFSET);
		long count  = raf.readLong();
		raf.seek(COUNT_OFFSET);
		raf.writeLong(count+1);
	}

	public boolean exists(String key) throws IOException {
		int index = hash(key);
		long offset = loadNext((index<<3)+TABLE_START_OFFSET);
		while (offset > 0) {
			String key2 = loadKey(offset);
			if (key.equals(key2)) return true;
			offset = loadNext(offset);
		}
		return false;
	}
	
	
	public byte[] load(String key) throws IOException {
		int index = hash(key);
		long offset = loadNext((index<<3)+TABLE_START_OFFSET);
		while (offset > 0) {
			String key2 = loadKey(offset);
			if (key.equals(key2)) return loadData(offset);
			offset = loadNext(offset);
		}
		return null;
	}
	
	public void remove(String key) throws IOException {
		int index = hash(key);
		long offset = loadNext((index<<3)+TABLE_START_OFFSET);
		long prev = offset;
		while (offset > 0) {
			String key2 = loadKey(offset);
			if (key.equals(key2)) {
				long next = loadNext(offset);
				raf.seek(prev);
				raf.writeLong(next);
				
				raf.seek(COUNT_OFFSET);
				long count  = raf.readLong();
				raf.seek(COUNT_OFFSET);
				raf.writeLong(count-1);
				
				return;
			}
			prev = offset;
			offset = loadNext(offset);
		}
	}
	
	
	public static Storage createStorage(String filename, int tablesize) throws WalletException {		
		File file = new File(filename);
		if (file.exists()) {
			file.delete();
		}

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(filename, "rw");
				
			raf.seek(0);
			raf.writeBytes("wallet");
			raf.writeInt(tablesize);
			raf.writeLong(0); 
			raf.write(new byte[tablesize<<3]);	
			
			
		} catch (IOException e) {
			WalletException ex = new WalletException("Some error in create storage");
			ex.initCause(e);
			throw ex;
		} finally {
			try {
				if (raf != null)
					raf.close();
			} catch (IOException e) {
			}
		}
		return new Storage(filename);
	}
	
	public static void rebuild(String filename, int deep) throws IOException {
		Storage db = null;
		Storage tmp = null;
		try {
			db = new Storage(filename);
			int tablesize = (int) (db.count() == 0 ? 1024 :  db.count()/deep);
			tmp = Storage.createStorage(filename + ".temp", tablesize);
			for (int i=0; i<db.TABLE_SIZE; i++) {
				long offset = db.loadNext((i<<3)+TABLE_START_OFFSET);
				while (offset > 0) {
					String key = db.loadKey(offset);
					byte[] data = db.loadData(offset);
					tmp.save(key, data);
					offset = db.loadNext(offset);
				}
			}
		} catch (WalletException e) {
			e.printStackTrace();
		} finally {
			db.close();
			tmp.close();
		}
		
		File dbf = new File(filename);
		dbf.delete();
		File tmpf = new File(filename + ".temp");
		tmpf.renameTo(dbf);
	}
	
	
	private long loadNext(long offset) throws IOException {
		raf.seek(offset);
		return raf.readLong();
	}
	
	private String loadKey(long offset) throws IOException {
		raf.seek(offset + 8);
		int size = raf.readInt();
		byte[] data = new byte[size];
		raf.read(data);
		return new String(data);
	}
	
	private byte[] loadData(long offset) throws IOException {
		raf.seek(offset + 8);
		int size = raf.readInt();
		raf.seek(offset + size + 12);
		size = raf.readInt();
		byte[] data = new byte[size];
		raf.read(data);
		return data;
	}	
	
	private int hash(String key) {
		int hash = 0;
		for (int i = 0; i < key.length(); i++) {
		  hash = (hash << 5) - hash + key.charAt(i);
		}
		hash = (hash & Integer.MAX_VALUE) | 1;
		return hash  % TABLE_SIZE;
	}

}
