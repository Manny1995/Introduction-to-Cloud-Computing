// Immanuel Amirtharaj
// COEN 241
// Programming Assignment 1
// Tuple.java
// The Tuple class represents a tuple, a chunk of data.  This class also has methods to encode and decode the tuple from a string as well as functions to
// compare two tuples (variable match and value match)



import java.io.*;
import java.util.*;

class Pair {
	public VarType first;
	public String second;

	public Boolean isVar;

	Pair(VarType f, String s) {
		first = f;
		second = s;
		isVar = false;
	}

	Pair(VarType f, String s, Boolean v) {
		first = f;
		second = s;
		isVar = v;
	}

	Boolean isEqual(Pair p2) {
		return second.equals(p2.second);
	}

	Boolean isTypeEqual(Pair p2) {
		return first == p2.first;
	}

	Boolean valueMatch(Pair p2) {
		return isEqual(p2) && isTypeEqual(p2);
	}
}

public class Tuple {

	public ArrayList<Pair> valueList;
	public ArrayList<VarType> variableList;

	public boolean hasVariable = false;

	public Tuple() {
		valueList = new ArrayList<Pair>();
		variableList = new ArrayList<VarType>();
	}

	public Tuple(ArrayList<Pair> tl, ArrayList<VarType> t2) {
		valueList = tl;
		variableList = t2;
	}

	public Tuple(String tupleString) {

		valueList = new ArrayList<Pair>();
		String []parts = tupleString.split(",");

		for (int i = 0; i < parts.length; i++) {
			String temp = parts[i].trim();

			Pair p;

			if (temp.charAt(0) == '?') {
				temp = temp.substring(1);
				String [] varParts = temp.split(":");
				p = new Pair(varTypefromString(varParts[1]), varParts[0], true);
				hasVariable = true;
			}
			else {
				p = new Pair(getVarType(temp), temp);
			}

			valueList.add(p);
		}
	}

	private VarType varTypefromString(String s) {

		// System.out.println(s);

		if (s.equals("int"))
			return VarType.INTEGER;
		if (s.equals("float"))
			return VarType.FLOAT;

		return VarType.STRING;
	}

	private String stringFromVarType(VarType v) {
		if (v == VarType.INTEGER)
			return "int";

		if (v == VarType.FLOAT)
			return "float";

		return "string";
	}

	private VarType getVarType(String amount){

		if (amount.charAt(0) == '"' && amount.charAt(amount.length()-1) == '"' && amount.length() > 1)
		 	return VarType.STRING;

	     if (amount.contains(".")) { 
	     	try {
	     		Float.parseFloat(amount); 
	       }
	       catch(NumberFormatException e) {
	         return VarType.STRING;
	       }
	       
	    	return VarType.FLOAT;
	    } 
	    else {
	    	try {
	        	Integer.parseInt(amount); 
	      	}
	    	catch(NumberFormatException e){
	        	return VarType.STRING;
	    	}
	    	return VarType.INTEGER;
	     }
	}

	public Boolean valueMatch(Tuple dest) {

		if (dest.valueList.size() != valueList.size())
			return false;

		for (int i = 0; i < valueList.size(); i++) {
			Pair p1 = valueList.get(i);
			Pair p2 = dest.valueList.get(i);
		
			if (p1.isEqual(p2) == false)
				return false; 

		}

		return true;
	}

	public Boolean variableMatch(Tuple dest) {

		// System.out.println(stringValue());
		// System.out.println(dest.stringValue());

		if (dest.valueList.size() != valueList.size())
			return false;

		for (int i = 0; i < dest.valueList.size(); i++) {

			Pair p1 = valueList.get(i);
			Pair p2 = dest.valueList.get(i);

			if (p1.isVar && !p2.isVar && p1.isTypeEqual(p2)) {
				continue;
			}

			if (!p1.isVar && p2.isVar && p1.isTypeEqual(p2)) {
				continue;
			}	

			if (!p1.isVar && !p2.isVar && p1.valueMatch(p2)) {
				continue;
			}

			return false;

		}

		return true;
	}

	public String stringValue() {
		String ret = "";
		for (int i = 0; i < valueList.size(); i++) {

			if (valueList.get(i).isVar == false) {
				ret+=(valueList.get(i).second + ",");
			}
			else {
				ret+= ("?" + valueList.get(i).second + ":" + stringFromVarType(valueList.get(i).first) + ",");
				hasVariable = true;
			}
		}

		if (ret.length() > 0 && ret.charAt(ret.length()-1) == ',') {
			ret = ret.substring(0, ret.length()-1);
		}

		return ret;
	}

	public static void test(String s1, String s2) {

		Tuple t1 = new Tuple(s1);
		Tuple t2 = new Tuple(s2);

		System.out.println("Testing for " + s1 + " and " + s2);
		System.out.println("String value of t1" + t1.stringValue());
		System.out.println("String value of t2" + t2.stringValue());

		System.out.println("Variable match returns " + t1.variableMatch(t2));
		System.out.println("Value match returns " + t1.valueMatch(t2));

		System.out.println("\n\n");

	}

	public static void main(String [] args) {

		String s1 = "sup";
		String s2 = "sup";

		test(s1, s2);

		s1 = "sup, 1, 2, 4";
		s2 = "sup, 1, 2, 4";

		test(s1, s2);

		s1 = "sup, 1, 2, 4";
		s2 = "sup, 1, 2, 3";

		test(s1, s2);


		s1 = "sup, 1, 2.5, 4";
		s2 = "sup, 1, 2.5, 4";

		test(s1, s2);

		s1 = "sup, lit";
		s2 = "?var:string, ?var:string";

		test(s1, s2);

		s2 = "sup, lit";
		s1 = "?var:string, ?var:string";

		test(s1, s2);


		s1 = "sup, lit";
		s2 = "?var:string";

		test(s1, s2);

	}

}
