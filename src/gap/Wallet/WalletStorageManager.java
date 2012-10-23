package gap.Wallet;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

class WalletStorageManager {
	private final static Map<String,IWalletStorage> files = new HashMap<String,IWalletStorage>();
	
	private WalletStorageManager() {		
	}
	
	public static synchronized IWalletStorage getInstance(String filename) {
		assert(filename != null);
		
		String fullpath = Paths.get(filename).toFile().getAbsolutePath();
		if (!files.containsKey(fullpath)) {
			files.put(fullpath, new WalletStorage(filename));
		}
		return files.get(fullpath);
	}

}
