// Immanuel Amirtharaj
// COEN 241
// Programming Assignment 1
// Host.java
// The Host class is responsible for reading and writing to the file, and sending requests to other hosts for add, in, out, and rd

import java.net.*;
import java.io.*;
import java.util.*;
import java.security.*;
import java.math.*;
import java.lang.*;
import java.nio.file.Files;

enum VarType {
    INTEGER, FLOAT, STRING, EMPTY
}

public class Host {

	private Server receiver;
	private String hostName;
	private Boolean isMaster;
	private int numDevices;
	private String dir;

	public int hostID;
	private Boolean takingInput = true;

	/**Server Delegate Methods */

	public Boolean addServer(String hostString) {
		return saveHost(new HostInfo(hostString));
	}

	public Boolean outServer(String tupleString) {
		return saveTuple(new Tuple(tupleString));
	}

	public Tuple inServer(String tupleString) {
		return findTuple(new Tuple(tupleString), true);
	}

	public Tuple rdServer(String tupleString) {
		return findTuple(new Tuple(tupleString), false);
	}

	public Boolean updateID(String s) {
		hostID = Integer.parseInt(s);
		return true;
	}

	public void waitingForTuple() {
		System.out.println("blocking input");
	}


	// call when adding a tuple
	public void broadcastAddedTuple(Tuple t) {
		String request = "block:" + t.stringValue();
		ArrayList<HostInfo> savedHosts = savedHosts();
		for (int i = 0; i < savedHosts.size(); i++) {
			HostInfo h = savedHosts.get(i);
			Client c = new Client(h.ipAddress, h.portNo);
			String res = c.sendMessage(request);
			if (res.equals("success")) {
				return;
			}
		}
	}






	/* Client Methods */


	private void addClient(HostInfo destinationHost) {

		isMaster = true;
		hostID = 0;

		Client c = new Client(destinationHost.ipAddress, destinationHost.portNo);
		c.sendMessage("add:" + myHostInfo().stringValue());
		System.out.println(c.getResponse());

		ArrayList<HostInfo> savedHosts = savedHosts();

		for (int i = 0; i < savedHosts.size(); i++) {
			HostInfo h = savedHosts.get(i);

			Client savedDestination = new Client(h.ipAddress, h.portNo);
			savedDestination.sendMessage("add:" + destinationHost.stringValue());

			c.sendMessage("add:" + h.stringValue());
		}

		saveHost(destinationHost);
		int destinationID = numHosts() - 1;
		c.sendMessage("setID:" + destinationID);

	}

	private void outClient(Tuple t) {
		int tupleLocation = getTupleLocation(t);

		if (tupleLocation == hostID) {
			System.out.println("put tuple (" + t.stringValue() + ") on " + receiver.getLocalAddress()); 
			saveTuple(t);
		}
		else {
			HostInfo destination = hostInfoWithId(tupleLocation);
			Client c = new Client(destination.ipAddress, destination.portNo);
			String response = c.sendMessage("out:" + t.stringValue());
			System.out.println(response);
		}

		broadcastAddedTuple(t);

	}

	public void rdClient(Tuple targetTuple) {

		String request = "rd:" + targetTuple.stringValue();
		sharedRead(targetTuple, request, false);
	}


	public void inClient(Tuple targetTuple) {

		String request = "in:" + targetTuple.stringValue();
		sharedRead(targetTuple, request, true);

	}

	public void stopTakingInput(Tuple t) {
		System.out.println("Waiting for a tuple that matches (" + t.stringValue() + ")");
		printLindaLabel();
		takingInput = false;
	}

	public void resumeTakingInput() {
		takingInput = true;
		System.out.println("Received tuple, now ready to take input");
		printLindaLabel();
	}

	public void printLindaLabel() {
		System.out.print("linda>\t");
	}

	private void sharedRead(Tuple targetTuple, String request, Boolean delete) {

		// variable match
		if (targetTuple.hasVariable == true) {
			ArrayList<HostInfo> hostList = savedHosts();

			hostList.add(myHostInfo());
			Collections.shuffle(hostList);

			Boolean foundTuple = false;

			for (int i = 0; i < hostList.size(); i++) {

				HostInfo info = hostList.get(i);

				Client c = new Client(info.ipAddress, info.portNo);

				String response = c.sendMessage(request);
				if (response.equals("failure"))
					continue;
				else {
					System.out.println(c.getResponse());
					return;
				}
			}

			// not found so stalll;
			stopTakingInput(targetTuple);
			receiver.waitingForTuple(targetTuple, delete);
		}

		// value match
		else {

			int tupleLocation = getTupleLocation(targetTuple);

			HostInfo destination = hostInfoWithId(tupleLocation);

			Client c = new Client(destination.ipAddress, destination.portNo);
			String response = c.sendMessage(request);

			// System.out.println(response);
			if (response.equals("failure")) {
				stopTakingInput(targetTuple);
				receiver.waitingForTuple(targetTuple, delete);
			}
			else {
				System.out.println(response);
			}
		}
	}





	/* Getters */


	public HostInfo myHostInfo() {
		HostInfo h = null;
		try {
			h = new HostInfo(hostID, hostName, InetAddress.getLocalHost().getHostAddress(), receiver.getPort());
		}
		catch(IOException e) {

		}
		return h;
	}


	private int numHosts() {

		String nets = dir + "/nets.txt";

		int count = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(nets));
    		String line;
    		
    		while ((line = reader.readLine()) != null) {
    			count++;
    		}

    		reader.close();
    	}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return count + 1;
	}


	public Tuple findTuple(Tuple t, Boolean delete) {
		ArrayList<Tuple> tupleList = savedTuples();

		Tuple temp = null;
		int pos = - 1;

		for (int i = 0; i < tupleList.size(); i++) {
			if (t.hasVariable == false) {
				if (tupleList.get(i).valueMatch(t)) {
					temp = tupleList.get(i);
					pos = i;
					break;
				}
			}
			else {
				if (tupleList.get(i).variableMatch(t)) {
					temp = tupleList.get(i);
					pos = i;

					break;
				}
			}
		}

		if (delete == true && pos != -1) {
			tupleList.remove(pos);
			saveTuplesFromArray(tupleList);
		}

		return temp;
	}



	public int getTupleLocation(Tuple t) {

		String s = t.stringValue();

		BigInteger hashedTuple = new BigInteger("0");

		String tupleString = t.stringValue();
		try {
			NoSuchAlgorithmException e;

	        MessageDigest m = MessageDigest.getInstance("MD5");

	        m.update(tupleString.getBytes(),0,tupleString.length());

	        hashedTuple = new BigInteger(1, m.digest());
	        BigInteger sol = hashedTuple.mod(new BigInteger(Integer.toString(numHosts())));

	        // System.out.println("hashed value is " + hashedTuple + " and location is " + sol.intValue());

			return sol.intValue();

		} catch(NoSuchAlgorithmException e) {
			System.out.println("Failed to hash");

		}

		return 0;
	}


	private void saveTuplesFromArray(ArrayList<Tuple> tupleList) {
		
		try {
			File file = new File(dir + "/tuples.txt");
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(dir + "/tuples.txt", false);
			BufferedWriter bw = new BufferedWriter(fw);


			for (int i = 0; i < tupleList.size(); i++) {
				String data = tupleList.get(i).stringValue() + "\n";
				bw.write(data);
			}

			bw.close();
			fw.close();
		}
		catch (IOException e) {

			System.out.println(e.getMessage());
		}
	}

	private Boolean saveTuple(Tuple t) {
		try {
			File file = new File(dir + "/tuples.txt");
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(dir + "/tuples.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);

			String data = t.stringValue() + "\n";
			bw.write(data);
			bw.close();
			fw.close();
			return true;
		}
		catch (IOException e) {

			System.out.println(e.getMessage());
			return false;
		}

	}


	private HostInfo hostInfoWithId(int hostId) {
		if (hostId == hostID) 
			return myHostInfo();

		HostInfo returnInfo = null;
		ArrayList<HostInfo> hosts = savedHosts();
		for (int i = 0; i < hosts.size(); i++) {
			HostInfo h = hosts.get(i);
			if (h.hostId == hostId) {
				returnInfo = h;
				break;
			}
		}	

		return returnInfo;
	}

	private ArrayList<HostInfo> savedHosts() {
		ArrayList<HostInfo> hosts = new ArrayList<HostInfo>();

		String nets = dir + "/nets.txt";

		int count = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(nets));
    		String line;
    		
    		while ((line = reader.readLine()) != null) {
    			hosts.add(new HostInfo(line));
    		}

    		reader.close();
    	}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return hosts;
	}

	private ArrayList<Tuple> savedTuples() {
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();

		String nets = dir + "/tuples.txt";

		int count = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(nets));
    		String line;
    		
    		while ((line = reader.readLine()) != null) {
    			tuples.add(new Tuple(line));
    		}

    		reader.close();
    	}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return tuples;
	}

	private void createDirectoryWithHostName() {

		String nets = dir + "/nets.txt";
		String tuples = dir + "/tuples.txt";

		try {
			File dirPath = new File(dir);
			Files.deleteIfExists(dirPath.toPath());
			dirPath.mkdirs();

			File f = new File(nets);
			Files.deleteIfExists(f.toPath());
			f.createNewFile();
			f = new File(tuples);
			Files.deleteIfExists(f.toPath());
			f.createNewFile();
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private Boolean saveHost(HostInfo h) {

		try {
			File file = new File(dir + "/nets.txt");
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(dir + "/nets.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);

			String data = h.stringValue() + "\n";
			bw.write(data);

			bw.close();
			fw.close();
			return true;
		}
		catch (IOException e) {

			System.out.println(e.getMessage());
			return false;
		}

	}










	/* Console Methods */

	private void startConsole() {

		Scanner scan = new Scanner(System.in);
		Boolean finished = false;

		while (!finished) {
			printLindaLabel();
			String instruction = scan.nextLine();

			if (takingInput == false) {
				System.out.println("Blocked because waiting for a request");
				continue;
			}

			String [] parts = instruction.split("\\(", 2);


			// trim " "
			String command = parts[0].trim();

			if (command.equals("exit")) {
				finished = true;
				System.out.println("Successfully exited");
				receiver.stopListening();
				continue;
			}

			if (command.equals("status")) {
				printStatus();
				continue;
			}

			if (parts.length < 2) {
				System.out.println("Error parsing the command.  Please try again");
				continue;
			}

			parts[1] = parts[1].substring(0,parts[1].length()-1);


			switch(command) {

				case "add":
					String [] hosts = parts[1].split("\\)[ ]*\\(");
					hostID = 0;

					for (int i = 0; i < hosts.length; i++) {
						String[] hostComponents = hosts[i].split(",");
						if (hostComponents.length != 3) {
							System.out.println("Wrong format for the host you added");
						}
						else {
							HostInfo destination = new HostInfo(
								numHosts(), 
								hostComponents[0].trim(), 
								hostComponents[1].trim(), 
								Integer.parseInt(hostComponents[2].trim())
							);

							addClient(destination);
						}
					}

					break;
				case "out":
					Tuple t = new Tuple(parts[1].trim());
					outClient(t);
					break;
				case "rd":
					Tuple u = new Tuple(parts[1].trim());
					rdClient(u);
					break;
				case "in":
					Tuple v = new Tuple(parts[1].trim());
					inClient(v);
					break;
				default:
					System.out.println("Invalid command!");
					break;
			}

		}
	}

	private void printStatus() {

		System.out.println("My host Id is " + hostID + "\n\n");

		System.out.println("List of Connected Hosts\n");

		ArrayList<HostInfo> hostInfo = savedHosts();
		for (int i = 0; i < hostInfo.size(); i++) {
			System.out.println(hostInfo.get(i).stringValue());
		}

		System.out.println("\n\nList of Tuples");

		ArrayList<Tuple> tuples = savedTuples();
		for (int i = 0; i < tuples.size(); i++) {
			System.out.println(tuples.get(i).stringValue());
		}
	}








	/* Driver */

	public void start() {
		receiver = new Server(0, hostName, this);
		receiver.start();
		createDirectoryWithHostName();
		startConsole();
	}









	/* Constructor */

	public Host(String hName) {
		hostName = hName;

		isMaster = false;
		hostID = 0;
		dir = "/tmp/iamirtha/linda/" + hostName;
	}



}











class HostInfo {

	public int hostId;
	public String hostName;
	public String ipAddress;
	public int portNo;

	public String stringValue() {
		String s = hostId + "," + hostName + "," + ipAddress + "," + portNo;
		return s;
	}

	public Boolean equals(HostInfo dest) {
		return ipAddress == dest.ipAddress && portNo == dest.portNo;
	}

	public HostInfo(String hostString) {
		String []parts = hostString.split(",");
		hostId = Integer.parseInt(parts[0]);
		hostName = parts[1];
		ipAddress = parts[2];
		portNo = Integer.parseInt(parts[3]);
	}

	public HostInfo(int a, String b, String c, int d) {
		hostId = a;
		hostName = b;
		ipAddress = c;
		portNo = d;
	}
}


