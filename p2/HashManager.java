import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.math.*;
import java.security.*;




public class HashManager {

	private int numberOfReplicas = 2;
	private double tableSize = Math.pow(2, 16);
  	private SortedMap<Integer, Integer> circle = new TreeMap<Integer, Integer>();
  	private SortedMap<Integer, Integer> killedDevices = new TreeMap<Integer, Integer>();
  	private int lookupSize = 100;


  	public void updateCircle(Integer i, Integer j) {
  		circle.put(i, j);
  	}

	public HashManager() {

		System.out.println("The size of the Hash Manager lookup table is " + lookupSize);
	}


	public void printCircle() {
		System.out.println("\nPrinting the circle");
		for (SortedMap.Entry<Integer, Integer> entry : circle.entrySet()) {
			System.out.println(" Key " + entry.getKey() + " is mapped to a host with ID of " + entry.getValue());
		}
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

	public int getNext(HostInfo h) {

		System.out.println(h.hostId);

		int hashedLocation = getLocation(h.stringValue());

		for (int i = hashedLocation; i <= lookupSize; i++) {
			if (i == lookupSize) {
				i = 0;
			}

			if (circle.containsKey(i)) {
				return circle.get(i);
			}
		}

		return 0;
	}

	// adds 1 host to the circle
	public Integer[] addHost(HostInfo h) {

		String val = h.stringValue();
		int location = getLocation(val);

		System.out.println("Added host at location " + location);
		circle.put(location, h.hostId);

		Integer[] parts = new Integer[2];
		parts[0] = location;
		parts[1] = h.hostId;

		return parts;

		// for (int i = 0; i < numberOfReplicas; i++) {

		// 	String val = h.stringValue() + i;
		// 	int location = getLocation(val);

		// System.out.println("Added host at location " + location);

		// 	circle.put(location, h.hostId);
		// }
		
	}


	// removes 1 host from the circle
	public void removeHost(HostInfo h) {

		String val = h.stringValue();
		int location = getLocation(val);
		
		System.out.println("Removed host at location " + location);
		circle.remove(location);

		// for (int i = 0; i < numberOfReplicas; i++) {

		// 	String val = h.stringValue() + i;
		// 	int location = getLocation(val);
		// 	System.out.println("Removed host at location " + location);

		// 	circle.remove(location);
		// }
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