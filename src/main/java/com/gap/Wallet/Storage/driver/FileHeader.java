package com.gap.wallet.storage.driver;

import java.io.IOException;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

class FileHeader {
	
	public static final long Identifier = 0x5B57414C4C45545DL;
	
	public static final int header_offset_start = 0;
	public static final int header_size = 32;
	public static final int header_offset_stop = header_offset_start + header_size;

	public static final int index_identifier = 0;
	public static final int index_version = 1;
	public static final int index_count = 2;
	public static final int index_bucketSize = 3;	
	
	private final LongBuffer header;

	public FileHeader(FileChannel channel) throws IOException {
		header = channel.map(MapMode.READ_WRITE, header_offset_start, header_size).asLongBuffer();
	}

	public long getIdentifier() {
		return header.get(index_identifier);
	}

	public void setIdentifier(long identifier) {
		header.put(index_identifier, identifier);
	}

	public long getVersion() {
		return header.get(index_version);
	}

	public void setVersion(long version) {
		header.put(index_version, version);
	}
		
	public long getCount() {
		return header.get(index_count);
	}

	public void setCount(long count) {
		assert(count >= 0);		
		header.put(index_count, count);
	}

	public int getBucketSize() {
		return (int) header.get(index_bucketSize);
	}

	public void setBucketSize(int bucketSize) {
		assert(bucketSize > 0);		
		header.put(index_bucketSize, bucketSize);
	}

	
	public void countInc() {		
		setCount(getCount() + 1);
	}
	
	public void countDec() {
		setCount(getCount() - 1);
	}
}
