package com.gap.wallet.storage.driver;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.gap.wallet.exception.WalletException;

class FileSpace {
	public static final long EOF	= -1;
	
	private static final int recordHeaderSize = 24;	
	private static final int keySize_index	= 0;
	private static final int valueSize_index = 1;
	private static final int offsetNext_index = 2;
	
	
	private final FileChannel channel;
	private final ByteBuffer headerBuf;

	public FileSpace(FileChannel channel) {
		this.channel = channel;
		this.headerBuf = ByteBuffer.allocate(recordHeaderSize);
	}

	public byte[] getKey(long offset) throws WalletException {
		try {
			assert(offset > 0);
			assert(offset < channel.size());
			
			channel.read(headerBuf, offset);
			headerBuf.flip();
			long size = headerBuf.asLongBuffer().get(keySize_index);
			ByteBuffer buffer = ByteBuffer.allocate((int) size);
			channel.read(buffer, offset + recordHeaderSize);
			
			return buffer.array();
		} catch (IOException e) {
			throw new WalletException("Exception caught during Storage.getKey: " + e.getMessage(), e);
		}
	}

	public byte[] getValue(long offset) throws WalletException {
		try {
			assert(offset > 0);
			assert(offset < channel.size());
			
			channel.read(headerBuf, offset);
			headerBuf.flip();
			long lengthKey = headerBuf.asLongBuffer().get(keySize_index);
			long size = headerBuf.asLongBuffer().get(valueSize_index);
			ByteBuffer buffer = ByteBuffer.allocate((int) size);
			channel.read(buffer, offset + lengthKey + recordHeaderSize);
			
			return buffer.array();
		} catch (IOException e) {
			throw new WalletException("Exception caught during Storage.getValue: " + e.getMessage(), e);
		}
	}

	public long getNextOffset(long offset) throws WalletException {
		try {
			assert(offset > 0);
			assert(offset < channel.size());
			
			channel.read(headerBuf, offset);
			headerBuf.flip();
			
			return headerBuf.asLongBuffer().get(offsetNext_index);
		} catch (IOException e) {
			throw new WalletException("Exception caught during Storage.getNextOffset: " + e.getMessage(), e);
		}
	}

	public void setNextOffset(long offset, long nextOffset) throws WalletException {
		try {
			assert(offset > 0);
			assert(offset < channel.size());
			assert(nextOffset > 0);
			assert(nextOffset < channel.size());
				
			ByteBuffer buf = ByteBuffer.allocate(8);
			buf.putLong(nextOffset);
			buf.flip();
			channel.write(buf, offset + 16);
		} catch (IOException e) {
			throw new WalletException("Exception caught during Storage.setNextOffset: " + e.getMessage(), e);
		}		
	}

	public long save(byte[] key, byte[] value, long nextOffset) throws WalletException {
		try {
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
		} catch (IOException e) {
			throw new WalletException("Exception caught during Storage.save: " + e.getMessage(), e);
		}		
		
	}
	
}
