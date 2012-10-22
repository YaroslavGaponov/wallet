package com.gap.Wallet;
import java.io.IOException;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

class Header implements IHeader {
	private final LongBuffer header;

	public Header(FileChannel channel) throws IOException {
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
		header.put(index_count, count);
	}

	public int getBucketSize() {
		return (int) header.get(index_bucketSize);
	}

	public void setBucketSize(int bucketSize) {
		header.put(index_bucketSize, bucketSize);
	}

	
	public void countInc() {		
		setCount(getCount() + 1);
	}
	
	public void countDec() {
		setCount(getCount() - 1);
	}
}
