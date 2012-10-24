package gap.Wallet.Storage;

public interface IterationAction {
	public boolean fire(byte[] key, byte[] value);
}
