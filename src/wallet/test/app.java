package wallet.test;

import wallet.Storage;

public class app {

	public static void main(String[] args) throws Exception {
		Storage s = null;
		try {
			System.out.println("initializing storage ...");
			s = new Storage("/Users/yaroslav/projects/test.db");
			
			/*
			System.out.println("save data ...");
			for(int i=0; i<1000000; i++) {
				String id = java.util.UUID.randomUUID().toString(); 
				s.save(id, ("Hello worlds " + i).getBytes());
			}*/
		
			
			System.out.println("searching data ...");
			long start = System.currentTimeMillis();
			byte data[] = s.load("1c4018f7-7cf6-444e-b86e-22d1087384eb");
			long elapsedTimeMillis = System.currentTimeMillis() - start;		
			System.out.println("elapse time :  " + elapsedTimeMillis + " ms");	
			if (data != null) {
				System.out.println(new String(data));
			} else {
				System.out.println("data is not found");
			}
			
			System.out.println("remove data ...");
			start = System.currentTimeMillis();
			s.remove("1c4018f7-7cf6-444e-b86e-22d1087384eb");
			elapsedTimeMillis = System.currentTimeMillis() - start;		
			System.out.println("elapse time :  " + elapsedTimeMillis + " ms");	
			
			
			s.rebuild();
		
		} finally {
			s.close();
		}
	}

}
