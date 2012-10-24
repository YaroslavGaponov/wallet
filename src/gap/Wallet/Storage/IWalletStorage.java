package gap.Wallet.Storage;


public interface IWalletStorage {
	public final long version = 1L;
	
	public boolean exists(byte[] key) throws WalletException;
	public byte[] get(byte[] key) throws WalletException;
	public boolean set(byte[] key, byte[] value) throws WalletException;
	public boolean remove(byte[] key) throws WalletException;
	public long count();
	
	public void close();
}
