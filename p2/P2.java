// Immanuel Amirtharaj
// COEN 241
// Programming Assignment 1
// P1.java
// The P1 class is the main driver class for this project


import java.net.*;
import java.io.*;
import java.util.*;

public class P2 {

	public static void main(String [] args) {
		if (args.length < 1) {
			System.out.println("Please enter a host name");
			return;
		}
		Host h = new Host(args[0]);
		h.start();
	}
}