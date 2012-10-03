package wallet.test;

import wallet.Storage;

public class app {

	public static void main(String[] args) throws Exception {
		
		String key = "";

		System.out.println("initializing storage ...");
		Storage s = Storage.createStorage("/Users/yaroslav/projects/test.db",1024);
		
		
		System.out.println("save data ...");
		for(int i=0; i<1000000; i++) {
			String id = java.util.UUID.randomUUID().toString(); 
			if (i == 12345) key = id;
			s.save(id, ("Hello worlds " + i).getBytes());
		}
		
		System.out.println("searching data ...");
		long start = System.currentTimeMillis();
		byte data[] = s.load(key);
		long elapsedTimeMillis = System.currentTimeMillis() - start;		
		System.out.println("elapse time :  " + elapsedTimeMillis + " ms");	
		if (data != null) {
			System.out.println(new String(data));
		} else {
			System.out.println("data is not found");
		}			
		
		s.close();
		
		System.out.println("rebuilding...");
		Storage.rebuild("/Users/yaroslav/projects/test.db",3);
	
		s = new Storage("/Users/yaroslav/projects/test.db");
		
		System.out.println("searching data ...");
		long start2 = System.currentTimeMillis();
		byte[] data2 = s.load(key);
		long elapsedTimeMillis2 = System.currentTimeMillis() - start2;		
		System.out.println("elapse time :  " + elapsedTimeMillis2 + " ms");	
		if (data != null) {
			System.out.println(new String(data2));
		} else {
			System.out.println("data is not found");
		}
		
		System.out.println("count = " + s.count());
		s.close();		

	}

}
