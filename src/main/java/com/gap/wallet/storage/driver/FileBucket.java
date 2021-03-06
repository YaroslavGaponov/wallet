package com.gap.wallet.storage.driver;

import java.io.IOException;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;


class FileBucket {
	private LongBuffer bucket;
	
	public FileBucket(FileChannel channel, int offset, int size) throws IOException {
		bucket = channel.map(MapMode.READ_WRITE, offset, size << 3).asLongBuffer();
	}
	
	public long get(int index) {
		assert(index >= 0); 
		assert(index < bucket.capacity());
		
		return bucket.get(index);
	}
	
	public void set(int index, long offset) {
		assert(index >= 0);
		assert(index < bucket.capacity());
		
		bucket.put(index, offset);
	}

}
