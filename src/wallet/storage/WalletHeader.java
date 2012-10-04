package wallet.storage;

interface WalletHeader {
	public final static String SIGN = "WALLET";
	public final static int HEADER_SIGN_OFFSET = 0;
	public final static int HEADER_BUCKETSSIZE_OFFSET = 6;
	public final static int HEADER_COUNT_OFFSET = 10;
	public final static int BUCKETS_START_OFFSET = 18;
}
