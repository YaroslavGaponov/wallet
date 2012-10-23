package gap.Wallet;

interface IBucket {
	public long get(int index);
	public void set(int index, long offset);
}
