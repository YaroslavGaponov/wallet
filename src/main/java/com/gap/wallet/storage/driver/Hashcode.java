package com.gap.wallet.storage.driver;

class Hashcode {

	public static int hashCode(byte[] key, int size) {
		assert(key != null);
		assert(size > 0);
		
		long a = 31415, b = 27183, h = 0;
		for (int i=0; i<key.length; i++) {
			h = (a * h + key[i]) % size;
			a = a * b % (size - 1);
		}
		
		assert(h >= 0);		
		return (int)h;
	}
	
	public enum force { less, equal, more };
	
	public static force compare(byte[] a, byte[] b) {
		assert(a != null);
		assert(b != null);
		
		if (a.length > b.length) return force.more;
		if (a.length < b.length) return force.less;
		
		for(int i=0; i<a.length; i++) {			
			if (a[i] > b[i]) return  force.more;
			if (a[i] < b[i]) return  force.less;			
		}
		
		return force.equal;
	}	
	
			
	public static int getPrime(long number){
		  int primes[] = {
		    1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 43, 47, 53, 59, 61, 71, 79, 83,
		    89, 103, 109, 113, 127, 139, 157, 173, 191, 199, 223, 239, 251, 283, 317, 349,
		    383, 409, 443, 479, 509, 571, 631, 701, 761, 829, 887, 953, 1021, 1151, 1279,
		    1399, 1531, 1663, 1789, 1913, 2039, 2297, 2557, 2803, 3067, 3323, 3583, 3833,
		    4093, 4603, 5119, 5623, 6143, 6653, 7159, 7673, 8191, 9209, 10223, 11261,
		    12281, 13309, 14327, 15359, 16381, 18427, 20479, 22511, 24571, 26597, 28669,
		    30713, 32749, 36857, 40949, 45053, 49139, 53239, 57331, 61417, 65521, 73727,
		    81919, 90107, 98299, 106487, 114679, 122869, 131071, 147451, 163819, 180221,
		    196597, 212987, 229373, 245759, 262139, 294911, 327673, 360439, 393209, 425977,
		    458747, 491503, 524287, 589811, 655357, 720887, 786431, 851957, 917503, 982981,
		    1048573, 1179641, 1310719, 1441771, 1572853, 1703903, 1835003, 1966079,
		    2097143, 2359267, 2621431, 2883577, 3145721, 3407857, 3670013, 3932153,
		    4194301, 4718579, 5242877, 5767129, 6291449, 6815741, 7340009, 7864301,
		    8388593, 9437179, 10485751, 11534329, 12582893, 13631477, 14680063, 15728611,
		    16777213, 18874367, 20971507, 23068667, 25165813, 27262931, 29360087, 31457269,
		    33554393, 37748717, 41943023, 46137319, 50331599, 54525917, 58720253, 62914549,
		    67108859, 75497467, 83886053, 92274671, 100663291, 109051903, 117440509,
		    125829103, 134217689, 150994939, 167772107, 184549373, 201326557, 218103799,
		    234881011, 251658227, 268435399, 301989881, 335544301, 369098707, 402653171,
		    436207613, 469762043, 503316469, 536870909, 603979769, 671088637, 738197503,
		    805306357, 872415211, 939524087, 1006632947, 1073741789, 1207959503,
		    1342177237, 1476394991, 1610612711, 1744830457, 1879048183, 2013265907
		  };
		  
		  if (number <= 0) {
			  return 8191;
		  }

		  for(int i = 0; i<primes.length; i++){
		    if(number <= primes[i]) {
		    	return primes[i];
		    }
		  }
		  
		  return primes[primes.length-1];
		}
	
}
