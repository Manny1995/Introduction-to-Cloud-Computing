import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.math.*;


public class HashManager {

	private int numberOfReplicas = 2;
	private double tableSize = Math.pow(2, 16);
  	private SortedMap<Integer, HostInfo> circle = new TreeMap<Integer, HostInfo>();

	public HashManager() {

		System.out.println("The size of the Hash Manager lookup table is " + Math.pow(2, 16));
	}

	// public ArrayList<int> getHashLists(Tuple t) {
	// 	String tupleString = t.stringValue();
	// 	ArrayList<int> hashList;
	// 	for (int i = 0; i < numberOfReplicas; i++) {
			
	// 	}

	// }



	// // adds 2 hosts because replication factor is 2
	// public void addHost(HostInfo h) {
	// 	for (int i = 0; i < numberOfReplicas; i++) {
	// 		circle.put(hashFunction.hash(h.ipAddress + i), h);
	// 	}
	// }

	// // removes 2 hosts because replication factor is 2
	// public void removeHost(HostInfo h) {
	// 	for (int i = 0; i < numberOfReplicas; i++) {
	// 		circle.remove(hashFunction.hash(h.ipAddress + i));
	// 	}
	// }

	// public void addTuple(Tuple t) {
	// 	for (int i = 0; i < replicationFactor; i++) {

	// 	}
	// }

	// public void removeTuple(Tuple t) {

	// 	for (int i = 0; i < replicationFactor; i++) {

	// 	}

	// }


}