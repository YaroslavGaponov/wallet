package com.gap.Wallet;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

class Storage implements IStorage {
	private final FileChannel channel;
	private final ByteBuffer headerBuf;

	public Storage(FileChannel channel) {
		this.channel = channel;
		this.headerBuf = ByteBuffer.allocate(IStorage.recordHeaderSize);
	}

	public byte[] getKey(long offset) throws IOException {
		assert(offset > 0);
		assert(offset < channel.size());
		
		channel.read(headerBuf, offset);
		headerBuf.flip();
		long size = headerBuf.asLongBuffer().get(IStorage.keySize_index);
		ByteBuffer buffer = ByteBuffer.allocate((int) size);
		channel.read(buffer, offset + IStorage.recordHeaderSize);
		
		return buffer.array();
	}

	public byte[] getValue(long offset) throws IOException {
		assert(offset > 0);
		assert(offset < channel.size());
		
		channel.read(headerBuf, offset);
		headerBuf.flip();
		long lengthKey = headerBuf.asLongBuffer().get(IStorage.keySize_index);
		long size = headerBuf.asLongBuffer().get(IStorage.valueSize_index);
		ByteBuffer buffer = ByteBuffer.allocate((int) size);
		channel.read(buffer, offset + lengthKey + IStorage.recordHeaderSize);
		
		return buffer.array();
	}

	public long getNextOffset(long offset) throws IOException {
		assert(offset > 0);
		assert(offset < channel.size());
		
		channel.read(headerBuf, offset);
		headerBuf.flip();
		
		return headerBuf.asLongBuffer().get(IStorage.offsetNext_index);
	}

	public void setNextOffset(long offset, long nextOffset) throws IOException {
		assert(offset > 0);
		assert(offset < channel.size());
		assert(nextOffset > 0);
		assert(nextOffset < channel.size());

		
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(nextOffset);
		buf.flip();
		channel.write(buf, offset + 16);
	}

	public long save(byte[] key, byte[] value, long nextOffset) throws IOException {
		assert(nextOffset > 0);
		assert(nextOffset < channel.size());

		long position = channel.size();
		ByteBuffer record = ByteBuffer.allocate(recordHeaderSize + key.length + value.length);
		record.putLong(key.length);
		record.putLong(value.length);
		record.putLong(nextOffset);
		record.put(key);
		record.put(value);
		record.flip();
		channel.write(record, position);
		
		return position;
	}
	
}
