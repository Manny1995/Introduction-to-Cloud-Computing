// Immanuel Amirtharaj
// COEN 241
// Programming Assignment 1
// Client.java
// The Client class is responsible for sending a message (request) to a Host class


import java.net.*;
import java.io.*;

public class Client {

	private int port;
	private String serverName;
    private String response = "if you are seeing this you messed up";

	public Client(String destination, int mPort) {
		port = mPort;
        serverName = destination;

	}

    public String getResponse() {
        return response;
    }

	public String sendMessage(String message) {

        Boolean success = false;
        try {
            Socket client = new Socket(serverName, port);
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF(message);
            // out.close();

            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);

            response = in.readUTF();
            // System.out.println(response);
            client.close();

        } catch(IOException e) {
            // e.printStackTrace();
            System.out.println("failed");
        }

        return response;
	}

/*	public static void main(String [] args) {
    	int port = Integer.parseInt(args[0]);
    	try {
    		String serverName = InetAddress.getLocalHost().getHostName();
        	System.out.println("Connecting to " + serverName + " on port " + port);
        	Socket client = new Socket(serverName, port);
         
        	System.out.println("Just connected to " + client.getRemoteSocketAddress());
        	OutputStream outToServer = client.getOutputStream();
        	DataOutputStream out = new DataOutputStream(outToServer);
         
        	out.writeUTF("Hello from " + client.getLocalSocketAddress());
        	InputStream inFromServer = client.getInputStream();
        	DataInputStream in = new DataInputStream(inFromServer);
         
        	System.out.println("Server says " + in.readUTF());
        	client.close();
     	} catch(IOException e) {
        	e.printStackTrace();
     	}
   }
	*/
}