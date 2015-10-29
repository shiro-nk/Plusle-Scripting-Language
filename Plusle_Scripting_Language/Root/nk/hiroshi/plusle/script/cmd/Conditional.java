package nk.hiroshi.plusle.script.cmd;

import nk.hiroshi.plusle.script.data.Numeral;
import nk.hiroshi.plusle.script.data.Variable;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * The conditional command type is composed of logical operators and variable reading. <br>
 * This type will return a value based off of the return value of the logical operators. <br>
 * In essence, I have made this class as a cross between a variable and a function
 * @author Ryan Kerr
 * @since 15 January, 2015
 */
public class Conditional implements Commander
{
	/** The script which stores the referred variables */
	private final Script parent;
	
	/** The result of the evaluation of the conditional */
	private boolean value;

	/** The given condition */
	private final String condition;
	
	/** Function to run */
	private final String[] trueRun, //The command to run when the condition is evaluated as true
						   elseRun; //The command to run when the condition is evaluated as false
	
	/**
	 * Creates a Conditional statement with a parent script, conditional statement, and commands
	 * @param parent - Script to read values from
	 * @param c Conditional Statement
	 * @param t Function to run when condition is true
	 * @param f Function to run when condition is false
	 */
	public Conditional(Script parent, String c, String[] t, String[] f) 
	{
		trueRun = t;
		elseRun = f;
		condition = c;
		this.parent = parent;
	}
	
	/**
	 * Sets the value to the value of the condition in it's current state
	 * @return true when condition is true, false when condition is false;
	 */
	private boolean evaluate()
	{
		value = value();
		return value;
	}
	
	/**
	 * Process the conditional statement. It is either read as a variable condition or numeral condition <br>
	 * I have only wrote this to read variables and numerals; functions require a parser
	 * @return True when condition is true, false when condition is false
	 */
	private boolean value()
	{
		String[] s = condition.split(":");
		
		String swapA = s[0].substring(1),
			   swapB = s[2].substring(1);
	
/*		//Inefficient section of code
		if(parent.isVariable(swapA) && parent.isVariable(swapB))
		{
			Variable a = parent.getVariable(swapA),
					 b = parent.getVariable(swapB);
			
			if(s[1].equals("=")) 	   return condition(EQUALS, a, b);
			else if(s[1].equals("!=")) return condition(NOT_EQUAL, a, b);
			else return false;
		}
		else if(parent.isNumeral(swapA) && parent.isNumeral(swapB))
		{
			Numeral a = parent.getNumeral(swapA),
					b = parent.getNumeral(swapB);
			
			int type = getOperation(s[1]);
			if(0 < type) return condition(type, a, b);
			else return false;
		} 
		else*/ 
		
		//System.out.println(parent.isNumeral(swapA) ? swapA : swapA);
		//System.out.println(parent.isVariable(swapA) ? "True" : "false");
		
		//Numeral operations. Allows for a numeral and a numeral to compared or a numeral and a number
		if(parent.isNumeral(swapA) || parent.isNumeral(swapB))
		{
			Numeral a, b;
			if(swapA.replaceAll("[0-9.]", "").equals("")) a = new Numeral("op", Double.parseDouble(s[0]));
			else if(parent.isNumeral(swapA)) a = parent.getNumeral(swapA);
			else a = new Numeral("null", 0.0);
			
			if(swapB.replaceAll("[0-9.]", "").equals("")) b = new Numeral("op", Double.parseDouble(s[2]));
			else if(parent.isNumeral(swapB)) b = parent.getNumeral(swapB);
			else b = new Numeral("null", 0.0);
			
			int type = getOperation(s[1]);
			if(0 <= type) return condition(type, a, b);
			else return false;
		}
		
		//Variable operations. Allows for a variable and a variable to be compared or a variable and a string
		else if(parent.isVariable(swapA) || parent.isVariable(swapB))
		{
			Variable a, b;
			if(parent.isVariable(swapA)) a = parent.getVariable(swapA);
			else a = new Variable("null", s[0]);
			
			if(parent.isVariable(swapB)) b = parent.getVariable(swapB);
			else b = new Variable("null", s[2]);
			
			int type = getOperation(s[1]);
			if(0 <= type) return condition(type, a, b);
			else return false;
		}
		return false; //Standard number to number or variable to variable operations are not supported
	}
	
	/**
	 * Logic statements for variables
	 * @param logic Logical operator to perform (equal to or not equal to)
	 * @param a Variable to read value from
	 * @param b Variable to read and compare
	 * @return
	 */
	private static boolean condition(int logic, Variable a, Variable b)
	{
		boolean rtn = false;
		switch(logic)
		{
			case 0: if(!a.getValue().equals(b.getValue()))  rtn = true; else break;
			case 1: if(a.getValue().equals(b.getValue()))   rtn = true; else break;
		}
		return rtn;
	}
	
	/**
	 * Logic statements for numerals
	 * @param logic Logical operator to perform (not equal to, equal to, greater than, greater or equal, less than, lesser or equal)
	 * @param a Numeral to read
	 * @param b Numeral to read and compare to numeral A
	 * @return
	 */
	private static boolean condition(int logic, Numeral a, Numeral b)
	{
		double x = a.getValue(),
			   y = b.getValue();
		
		boolean rtn = false;
		switch(logic)
		{
			case 0: if(x != y) rtn = true; else break;
			case 1: if(x == y) rtn = true; else break;
			case 2: if(x <  y) rtn = true; else break;
			case 3: if(x <= y) rtn = true; else break;
			case 4: if(x >  y) rtn = true; else break;
			case 5: if(x >= y) rtn = true; else break;
		}
		return rtn;
	}

	/** @return The command to run */
	public String[] getRunnable(){evaluate(); return value ? trueRun : elseRun;}
	
	/**
	 * Parses the operation symbol to a number which corresponds to the condition method switches
	 * @param input The string to parse
	 * @return 0 to 5: an operation; -1 not an operation
	 */
	public int getOperation(String input)
	{
		if(input.equals("!=")) 		return 0;
		else if(input.equals("=")) 	return 1;
		else if(input.equals("<"))  return 2;
		else if(input.equals("<=")) return 3;
		else if(input.equals(">"))  return 4;
		else if(input.equals("=>")) return 5;
		return -1;
	}
	
	
	/* Inherited Methods (mostly useless :D) */
	public String getName(){return "";}
	public Script getParent(){return parent;}
	public String getValue(){return value + "";}
	public void setValue(String value){}
	public void setValue(boolean value){this.value = value;}
}
