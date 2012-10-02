package wallet;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Storage {
	private final int MAXINDEXSIZE = 1024 * 1024;
	private final String filename;
	private RandomAccessFile file;
	
	public Storage(String filename) throws IOException {
		this.filename = filename;	
		this.file = new RandomAccessFile(filename, "rw");
		
		if (file.length() == 0) {
			file.seek(0);
			file.write(new byte[MAXINDEXSIZE<<3]);
		}
	}
	
	public void close() {
		try {
			file.close();
		} catch (IOException e) {
		}
	}
				
	public void save(String key, byte[] data) throws IOException {
		int index = hash(key);
		
		file.seek(index<<3);
		long offset = file.readLong();
		
		file.seek(index<<3);
		file.writeLong(file.length());
		
		file.seek(file.length());
		file.writeLong(offset);
		file.writeInt(key.length());
		file.writeBytes(key);
		file.writeInt(data.length);
		file.write(data);
	}
	
	public byte[] load(String key) throws IOException {
		int index = hash(key);
		long offset = loadNext(index<<3);
		while (offset > 0) {
			String key2 = loadKey(offset);
			if (key.equals(key2)) return loadData(offset);
			offset = loadNext(offset);
		}
		return null;
	}
	
	public void remove(String key) throws IOException {
		int index = hash(key);
		long offset = loadNext(index<<3);
		long prev = offset;
		while (offset > 0) {
			String key2 = loadKey(offset);
			if (key.equals(key2)) {
				long next = loadNext(offset);
				file.seek(prev);
				file.writeLong(next);
				return;
			}
			prev = offset;
			offset = loadNext(offset);
		}
	}
	
	public void rebuild() throws IOException {
		Storage temp = null;
		try {
			temp = new Storage(filename + ".tmp");
			for (int i=0; i<MAXINDEXSIZE; i++) {
				long offset = loadNext(i<<3);
				while (offset > 0) {
					String key = loadKey(offset);
					byte[] data = loadData(offset);
					System.out.println(key+ " : " + new String(data));
					temp.save(key, data);
					offset = loadNext(offset);
				}
			}
		} finally {
			temp.close();
		}
	}
	
	
	private long loadNext(long offset) throws IOException {
		file.seek(offset);
		return file.readLong();
	}
	
	private String loadKey(long offset) throws IOException {
		file.seek(offset + 8);
		int size = file.readInt();
		byte[] data = new byte[size];
		file.read(data);
		return new String(data);
	}
	
	private byte[] loadData(long offset) throws IOException {
		file.seek(offset + 8);
		int size = file.readInt();
		file.seek(offset + size + 12);
		size = file.readInt();
		byte[] data = new byte[size];
		file.read(data);
		return data;
	}	
	
	private int hash(String key) {
		int hash = 0;
		for (int i = 0; i < key.length(); i++) {
		  hash = (hash << 5) - hash + key.charAt(i);
		}
		hash = (hash & Integer.MAX_VALUE) | 1;
		return hash  % MAXINDEXSIZE;
	}

}
