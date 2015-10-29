package nk.hiroshi.plusle.script.cmd;

import nk.hiroshi.plusle.script.data.Numeral;
import nk.hiroshi.plusle.script.data.Variable;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * @author Ryan Kerr
 * @since January 03, 2015
 */
public class Script implements Commander
{
	private String name; // Contains the name of the script (denoted by "::")
	
	private String[] script,		// Contains the "main method" of the script
					 numeralNames,  // Contains the names of all numerals
					 functionNames, // Contains all the Function Names of the script
					 variableNames; // Contains all the Variable Names of the script
	
	private Function[] 	  functions; // Functions owned by script
	private Variable[] 	  variables; // Variables owned by script
	private Numeral[]     numerals;  // Numerals owned by script
	private Conditional[] condition; // Conditional Statements
	
	/**
	 * Creates Scipt
	 * @param n The name of the script (denoted as "::Script Name" in the script)
	 * @param sc The main method of the script (any commands prior to a function)
	 * @param fn The names of all the functions
	 * @param f All functions declared in the script
	 * @param vn The names of all the variables from the script
	 * @param v All variables declared in the script
	 */
	public void makeScript(String name, String[] sc, String[] fn, Function[] f, String[] vn, Variable[] v, String[] nn, Numeral[] n, Conditional[] c)
	{
		this.name = name;
		script = sc;
		functionNames = fn;
		variableNames = vn;
		numeralNames = nn;
		condition = c;
		functions = f;
		variables = v;
		numerals  = n;
	}
	
	/** @returns The script name */
	public String getName(){return name;}
	
	/** @returns The main method */
	public String[] getRunnable(){return script;}
	
	/** @returns The Variable name array */
	public String[] getVariableNames(){return variableNames;}
	
	/** @returns The Function name array*/
	public String[] getFunctionNames(){return functionNames;}
	
	/** @returns The Numeral name array*/
	public String[] getNumeralNames(){return numeralNames;}
	
	/** @returns All the variables */
	public Variable[] getVariables(){return variables;}
	
	/** @returns All the function owned by the script */
	public Function[] getFunctions(){return functions;}
	
	/** @returns All the numerals owned by the script */
	public Numeral[] getNumerals(){return numerals;}

	/**
	 * Goes through every element in an array and sees if the given value exists in the array
	 * 
	 * @param v The String to find in the String array 
	 * @param arr The String array to find the matching value in
	 * @return <b>true</b> if the value is matched to a value in the array; <br>
	 * 	       <b>false</b> if the value is not matched
	 */
	private boolean isValueIn(String v, String[] arr)
	{
		for(String s : arr) if(s.equals(v)) return true;
		return false;
	}
	
	/**
	 * Goes through every element in an array and matches it to the given value
	 * 
	 * @param v The value to find in the array
	 * @param arr The array to find the matching value in
	 * @return <b>value equal or greater than 0</b> The index of the matching value in the array
	 * <br>	   <b>-1</b> The value did not exist in the given array
	 */
	private int getIndexOf(String v, String[] arr)
	{
		for(int i = 0; i != arr.length; i++) if(v.equals(arr[i])) return i;
		return -1;
	}
	
	/**
	 * Gives the Object in the specified index in the given array
	 * as long as the given index is between 0 and the array length
	 * 
	 * @param index The index of the Object
	 * @param t The array to get the Object from
	 * @return <b>The Object at t[index]</b> When the index is in a valid range <br>
	 * 		   <b>null</b> When the index is outside the valid range
	 */
	private <Type> Type getValueOf(int index, Type[] t)
	{
		if(0 <= index && index < t.length) return t[index];
		return null;
	}
	

	/* The following lines of code check if values exist in their respective Name Arrays */
	public boolean isFunction(String fName){return isValueIn(fName, functionNames);}
	public boolean isVariable(String vName){return isValueIn(vName, variableNames);}
	public boolean isNumeral(String nName){return isValueIn(nName, numeralNames);}
	
	/* get Type Index gets the position of the desired element */
	public int getFunctionIndex(String fName){return getIndexOf(fName, functionNames);}
	public int getVariableIndex(String vName){return getIndexOf(vName, variableNames);}
	public int getNumeralIndex(String nName){return getIndexOf(nName, numeralNames);}
	
	/* Gets the element from the specified index */
	public Function getFunction(int index){return getValueOf(index, functions);}
	public Variable getVariable(int index){return getValueOf(index, variables);}
	public Numeral getNumeral(int index){return getValueOf(index, numerals);}
	
	public Conditional getCondition(int index){return getValueOf(index, condition);}
	
	/* Gets the element from the name (potential arrayOutOfBounds) */
	public Function getFunction(String fName){return getFunction(getFunctionIndex(fName));}
	public Variable getVariable(String vName){return getVariable(getVariableIndex(vName));}
	public Numeral getNumeral(String nName){return getNumeral(getNumeralIndex(nName));}
	
	/** Searches if the dataType exists under that name */
	public boolean isStorageData(String name)
	{
		for(String n : variableNames) if(n.equals(name)) return true;
		for(String n : numeralNames)   if(n.equals(name)) return true;
		return false;
	}
	
	// These have no use in Scripts
	public String getValue(){return null;}
	public Script getParent(){return null;}
	public void setValue(String value){}
	
}
