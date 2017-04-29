import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.math.*;
import java.security.*;




public class HashManager {

	private int numberOfReplicas = 2;
	private double tableSize = Math.pow(2, 16);
  	private SortedMap<Integer, Integer> circle = new TreeMap<Integer, Integer>();
  	private SortedMap<Integer, Integer> killedDevices = new TreeMap<Integer, Integer>();
  	private int lookupSize = 100;


	public HashManager() {

		System.out.println("The size of the Hash Manager lookup table is " + Math.pow(2, 16));
	}


	// hashes a string to a range of [0 and the lookup size]
	public int getLocation(String targetString) {

		BigInteger hashedString = new BigInteger("0");

		try {
			NoSuchAlgorithmException e;

	        MessageDigest m = MessageDigest.getInstance("MD5");

	        m.update(targetString.getBytes(),0,targetString.length());

	        hashedString = new BigInteger(1, m.digest());
	        BigInteger sol = hashedString.mod(new BigInteger(Integer.toString(lookupSize)));

	        System.out.println("hashed value is " + hashedString + " and location is " + sol.intValue());

			return sol.intValue();

		} catch(NoSuchAlgorithmException e) {
			System.out.println("Failed to hash");

		}

		return 0;
	}


	// adds 2 hosts because replication factor is 2
	public void addHost(HostInfo h) {
		for (int i = 0; i < numberOfReplicas; i++) {

			String val = h.stringValue() + i;
			int location = getLocation(val);

			circle.put(location, h.hostId);
		}
	}


	// removes 2 hosts because replication factor is 2
	public void removeHost(HostInfo h) {
		for (int i = 0; i < numberOfReplicas; i++) {

			String val = h.stringValue() + i;
			int location = getLocation(val);

			circle.remove(location);
		}
	}

	public int locationForTuple(Tuple t) {
		String val = t.stringValue();
		int location = getLocation(val);

		int numRepetitions = 0;
		for (int i = 0; i <= lookupSize; i++) {
			if (i == lookupSize) {
				i = 0;
			}

			if (circle.containsKey(i)) {
				return circle.get(i);
			}
		}

		return 0;
	}


}