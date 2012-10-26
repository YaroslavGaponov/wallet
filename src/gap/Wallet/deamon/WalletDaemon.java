package gap.Wallet.deamon;

import java.io.IOException;

public class WalletDaemon {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("wallet {port} {path}");
			return;
		}
				
		int port = Integer.parseInt(args[0]);
		String path = args[1];
		
		WalletSocketServer server = new  WalletSocketServer(port, path);
		System.out.println("wallet demon is starting on " + port);		
		Thread service = new Thread(server);		
		service.run();				
	}

}