// Immanuel Amirtharaj
// COEN 241
// Programming Assignment 1
// Host.java
// The Server class is responsible for listening to requests from hosts and notifies the Host class of them


import java.net.*;
import java.io.*;


public class Server extends Thread {
	private ServerSocket serverSocket;
	private Boolean running;
	public String hostName;
	private Host serverHost;
	private String failureHeader = "failure";

	private Tuple requestedTuple = null;
	private Boolean expectingTuple = false;
	private Boolean isVal = false;
	private Boolean isIn = false;

	public void waitingForTuple(Tuple t, Boolean in) {
		requestedTuple = t;
		expectingTuple = true;
		isVal = !requestedTuple.hasVariable;
		isIn = in;
	}

	public void gotTuple() {
		requestedTuple = null;
		expectingTuple = false;
	}

	public Server(int port, String host, Host h) {
		try {
			serverSocket = new ServerSocket(port, 1000);
			System.out.println(InetAddress.getLocalHost().getHostAddress() + " at port number: " + serverSocket.getLocalPort());
		}
		catch (IOException e) {
			System.out.println("Failed to create server socket");
			e.printStackTrace();
		}

		hostName = host;
		running = true;

		serverHost = h;
	}

	public int getPort() {
		return serverSocket.getLocalPort();
	}

	public String getLocalAddress() {
		String s = "";
		try {
			s = InetAddress.getLocalHost().getHostAddress();
		}
		catch (IOException e) {
			System.out.println("Unable to get local address");
		}

		return s;
	}

	public void stopListening() {
		running = false;
		try {
			serverSocket.close();
		}
		catch(IOException e) {
		}		

		System.err.println("Stopped listening");
	}

	public void run() {
		while (running) {
			try {
				Socket server = serverSocket.accept();
				DataInputStream in = new DataInputStream(server.getInputStream());
		    	String message = in.readUTF();
		    	
		    	String[] parts = message.split(":", 2);
		    	String header = parts[0];
		    	String body = parts[1];

		    	String response = processRequest(header, body);
		    	DataOutputStream out = new DataOutputStream(server.getOutputStream());

		    	out.writeUTF(response);
		    	// out.close();

		    	// server.close();
		    }
		    catch(IOException e) {
		    }
		}
		System.err.println("Server done running");
	}

	private String processRequest(String header, String body) {

		Boolean success = false;
		String response = "";

		String localAddress = getLocalAddress();
		switch (header) {
			case "add":
				success = serverHost.addServer(body);
				if (success == true) {
					response = "Successfully added host (" + localAddress + ") on port " + getPort();
				}
				else {
					response = failureHeader;
				}
				break;
			case "delete":
				System.out.println("Delete server");
				success = serverHost.deleteServer(body);
				if (success == true) {
					response = "Successfully deleted host (" + localAddress + ")";
				}
				else {
					response = failureHeader;
				}
				break;
			case "updateCircle":
				System.out.println("update circle");
				success = serverHost.updateCircle(body);
				break;
			case "move":
				success = serverHost.moveServer(body);
				if (success == true) {
					response = "Successfully moved tuples to another host";
				}
				else {
					response = failureHeader;
				}
				break;
			case "setID":
				success = serverHost.updateID(body);
				break;
			case "out":
				success = serverHost.outServer(body);
				if (success == true) {
					response = "put tuple (" + body + ") on " + localAddress;
				}
				else {
					response = failureHeader;
				}

				break;
			case "in":
				Tuple res = serverHost.inServer(body);
				if (res != null) {
					response = "get tuple (" + res.stringValue() + ") on " + localAddress;
				}
				else {
					response = failureHeader;
				}

				break;
			case "rd":
				Tuple readTup = serverHost.rdServer(body);
				if (readTup != null) {
					response = "read tuple (" + readTup.stringValue() + ") on " + localAddress;
				}
				else {
					response = failureHeader;
				}
				break;
			case "block":
				Tuple blockTup = new Tuple(body);

				Boolean matchedTuple = false;

				if (expectingTuple == false || requestedTuple == null) {
					// response = "failure";
					break;
				}

				// System.out.println(requestedTuple.stringValue());
				// System.out.println(blockTup.stringValue());

				if (requestedTuple.hasVariable) {
					matchedTuple = blockTup.variableMatch(requestedTuple);

				}
				else {
					matchedTuple = blockTup.valueMatch(requestedTuple);
				}

				if (matchedTuple == true) {

					if (serverHost.hostID == serverHost.getTupleLocation(blockTup)) {
						serverHost.findTuple(blockTup, isIn);
						response = "success";
						serverHost.resumeTakingInput();
						gotTuple();
						break;
					}

					blockTup.hasVariable = false;
					if (isIn == true) {
						serverHost.inClient(blockTup);
					}
					else {
						serverHost.rdClient(blockTup);
					}

					response = "success";
					serverHost.resumeTakingInput();
					gotTuple();
				}

				break;
			default:
				break;
		}

		return response;
	}
}
