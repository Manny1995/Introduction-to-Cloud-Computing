import java.util.*;
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

			return sol.intValue();

		} catch(NoSuchAlgorithmException e) {
			System.out.println("Failed to hash");

		}

		return 0;
	}

	public int getNext(HostInfo h) {

		String val = h.hostId + h.ipAddress;

		int hashedLocation = getLocation(val);


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

	public Integer[] getHostLocation(HostInfo h) {
		// String val = h.stringValue();
		String val = h.hostId + h.ipAddress;
		int location = getLocation(val);


		Integer[] parts = new Integer[2];
		parts[0] = location;
		parts[1] = h.hostId;

		return parts;

	}

	// adds 1 host to the circle
	public Integer[] addHost(HostInfo h) {

		// String val = h.stringValue();
		String val = h.hostId + h.ipAddress;
		int location = getLocation(val);

		circle.put(location, h.hostId);

		Integer[] parts = new Integer[2];
		parts[0] = location;
		parts[1] = h.hostId;

		return parts;

	}


	// removes 1 host from the circle
	public void removeHost(HostInfo h) {

		String val = h.hostId + h.ipAddress;

		int location = getLocation(val);
		
		circle.remove(location);

	}

	public int getNext(int location) {
		for (int i = location + 1; i <= lookupSize; i++) {
			if (i == lookupSize) {
				i = 0;
			}


			if (circle.containsKey(i)) {
				return circle.get(i);
			}
		}

		System.out.println("You really messed up");
		return -1;
	}

	public ArrayList<Integer> locationsForTuple(Tuple t) {

		int numRepetitions = 0;

		int prevLocation = 0;

		ArrayList<Integer> tupleLocations = new ArrayList<Integer>();

		for (int a = 0; a < numberOfReplicas; a++) {

			String val = t.stringValue() + a;
			int location = getLocation(val);

			tupleLocations.add(getNext(location));

		}
	
		return tupleLocations;

	}
}