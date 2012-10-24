package gap.Wallet.Storage;
interface IHeader {
	public final long Identifier = 0x5B57414C4C45545DL;
	
	public final int header_offset_start = 0;
	public final int header_size = 32;
	public final int header_offset_stop = header_offset_start + header_size;

	public final int index_identifier = 0;
	public final int index_version = 1;
	public final int index_count = 2;
	public final int index_bucketSize = 3;
		
	public long getIdentifier();
	public void setIdentifier(long identifier);
	
	public long getVersion();
	public void setVersion(long version);
	
	public long getCount();
	public void setCount(long count);
	
	public int getBucketSize();
	public void setBucketSize(int bucketSize);
	
	
	public void countInc();	
	public void countDec();
}
