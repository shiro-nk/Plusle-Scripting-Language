package nk.hiroshi.plusle.runtime;

import javax.swing.JTextArea;

import nk.hiroshi.plusle.script.cmd.Conditional;
import nk.hiroshi.plusle.script.cmd.Function;
import nk.hiroshi.plusle.script.cmd.Script;
import nk.hiroshi.plusle.script.data.Numeral;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * This class takes a script and runs commands based off of the functions it owns and proprietary commands (echo). <br>
 * This class is split into 2 main parts: sorting and execution. <br>
 * The sorting section keeps all the if statements in neat packages (unlike the Parser class, that was too messy but I didn't want to clean it). <br>
 * The execution section takes the values from the sorting part and executes them accordingly. <br>
 * @author Ryan Kerr
 * @since 11 January, 2015
 */
public class Interpreter 
{
	/** API Command References 
	 *	echo:   Println to output
	 *  prompt: Print   to output
	 *  return: Set value of a function
	 *  if:     Run conditional statement
	 */
	private String[] cmd = {"echo", "prompt", "return", "if", "write", "copy", "read", "get"};
	
	private final Script script;
	private JTextArea textArea = null;
	
	/**
	 * Creates an interpreter for a script; All variables, functions, and numerals are referenced as commands
	 * @param s Script to run
	 */
	public Interpreter(Script s)
	{
		script = s;
	}
	
	/**
	 * Creates an interpreter for a script with a given output area
	 * @param s The script to run
	 * @param output The GUI Component to write text to
	 */
	public Interpreter(Script s, JTextArea output)
	{
		this(s);
		textArea = output;
	}
	
	/**
	 * Reads each command and sends it to be executed. Only works with a script.
	 * If a script is null (ie. The script failed to be parsed), nothing happens.
	 */
	public void run()
	{
		try
		{
			if(script != null) for(String c : script.getRunnable()) exec(c, null);
		}
		catch(StackOverflowError e)
		{
			echo("Error: Stack Overflow Error");
		}
		catch(Exception e)
		{
			echo("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Same as run() however it runs commands stored inside a function
	 * @param f The function to read the commands from
	 */
	private void runFunction(Function f)
	{
		for(String c : f.getRunnable()) exec(c, f);
	}

	/**
	 * 
	 * @param cmd Command to be interpreted
	 * @param fnc Function which is running these commands. This is required for the "return" command
	 */
	private void exec(String cmd, Function fnc)
	{
		//System.out.println("Running: " + cmd);
		
		String[] split = cmd.split(" ");

		switch(getCommand(cmd))
		{
			case -1:
				switch(getType(cmd))
				{
					case 0: break;
					case 4:
						if(3 <= split.length && split[1].equals("="))
						{
							String extra = "";
						
							for(int i = 2; i!= split.length; i++) extra += split[i] + " ";
							extra = extra.trim();
				
							script.getVariable(split[0]).setValue(print(extra));
						}
						break;
					case 8:
						if(split.length == 3 && getType(split[2]) == 10)
						{
							switch(operation(split[1]))
							{
								case -1: break;
								case 0: Numeral.add(script.getNumeral(split[0]), script.getNumeral(split[0]), script.getNumeral(split[2].substring(1))); break;
								case 1: Numeral.subtract(script.getNumeral(split[0]), script.getNumeral(split[0]), script.getNumeral(split[2].substring(1))); break;
							}
						}
						else if(split.length == 3 && split[2].replaceAll("[0-9.]", "").equals(""))
						{
							switch(operation(split[1]))
							{
								case -1: break;
								case 0: Numeral.add(script.getNumeral(split[0]), script.getNumeral(split[0]), new Numeral("add", Double.parseDouble(split[2]))); break;
								case 1: Numeral.subtract(script.getNumeral(split[0]), script.getNumeral(split[0]), new Numeral("sub", Double.parseDouble(split[2]))); break;
							}
						}
						else if(split.length == 5 && split[1].equals("=") && getType(split[0]) == 8)
						{
							Numeral a = script.getNumeral(split[0]);
							Numeral b, c;
							if(getType(split[2]) == 10) b = script.getNumeral(split[2].substring(1));
							else if(split[2].replaceAll("[0-9.]", "").equals("")) b = new Numeral("a", Double.parseDouble(split[2]));
							else b = new Numeral("null", 0.0);
							
							if(getType(split[4]) == 10) c = script.getNumeral(split[4].substring(1));
							else if(split[4].replaceAll("[0-9.]", "").equals("")) c = new Numeral("b", Double.parseDouble(split[4]));
							else c = new Numeral("null", 0.0);
							
							switch(operation(split[3]))
							{
								case 0: Numeral.add(a, b, c); break;
								case 1: Numeral.subtract(a, b, c); break;
								case 2: Numeral.multiply(a, b, c); break;
								case 3: Numeral.divide(a, b, c); break;
							}
									
 						}
						break;
					case 16: runFunction(script.getFunction(script.getFunctionIndex(cmd))); break;
					case 24: 
				}
				break;
			case 0: echo(print(cmd.substring(5)) + "\n"); break;
			case 1: echo(print(cmd.substring(7))); break;
			case 2: if(fnc != null) fnc.setValue(print(cmd.substring("return ".length()))); break;
			case 3: 
				Conditional c = script.getCondition(Integer.parseInt(cmd.split(" ")[1]));
				String runnable = c.getRunnable()[0];
				if(script.isFunction(runnable)) runFunction(script.getFunction(runnable));
				break;
			case 7: 
				if(cmd.split(" ").length == 2) echo(System.getProperty(cmd.split(" ")[1]));
				break;
		}
		// Keeping track of all these cases and switch statements has been a nightmare, that is, if I could sleep... (2:13 AM January 16th, 2015)
	}
	
	/**
	 * The print formatter. Using the same methods that Variable operations are performed, 
	 * @param input 
	 * @param sc The parent script
	 * @return
	 */
	private String print(String input)
	{
		String[] printSwap;
		String output = "";
		boolean openQuote = false;
		
		if(input.contains("&")) printSwap = input.split(" & ");
		else if(input.equals("\" \"")) printSwap = input.split("");
		else printSwap = input.split(" ");
		
		for(int i = 0; i != printSwap.length; i++)
		{
			printSwap[i] = printSwap[i].	trim();
			if(printSwap[i].contains("\"") && !printSwap[i].endsWith("\"") && printSwap[i].startsWith("\"")) output += printSwap[i].replaceAll("[\"]", "") + " ";
			else if(printSwap[i].contains("\"")) output += printSwap[i].replaceAll("[\"]", "");
			else if(printSwap[i].trim() != "")
			{
				switch(getType(printSwap[i].trim()))
				{
					case 0:	case 1: case 2: case 3: case 4: case 5: case 7: case 8: case 9: case 11: case 12: case 13: case 14: case 15:
					case 16: output += printSwap[i]; break;
					case 6:  output += script.getVariable(printSwap[i].trim().substring(1)).getValue(); break;
					case 10: output += script.getNumeral(printSwap[i].trim().substring(1)).getValue();  break;
					case 17: Function printFunction = script.getFunction(printSwap[i].trim().substring(1));
						runFunction(printFunction);
						output += printFunction.getValue();
						break;
				}
			}
			
			if(openQuote && !printSwap[i].endsWith("\"")) output += " ";
			else if(!openQuote && printSwap[i].startsWith("\"")) openQuote = true;
			else openQuote = false;
		}
		
		return output;
	}
	
	/**
	 * Checks each "Script" property of a given String. Each property is added using a base 2 numbering system. <br>
	 * Variable is 4, Variable notation ($) is 2; A variable with a variable notation is 2 + 4 = 6
	 * @param string The String to be analyzed
	 * @param script The script which the string comes from
	 * @return +1: Function notation; +2: Variable notation; +4 is a variable; +8 is a numeral; +16 is a function
	 */
	private int getType(String string)
	{
		int value = 0;
		string = string.trim();
		String[] split = string.split(" ");
		
		if(string.startsWith("@")) value += 1;
		else if(string.startsWith("$")) value += 2;

		if(!string.equals("") || split[0] != null)
		{
			if(0 < value) split[0] = split[0].substring(1);
			if(script.isVariable(split[0])) value += 4;
			if(script.isNumeral(split[0]))  value += 8;
			if(script.isFunction(split[0])) value += 16;
		}

		return value;
	}
	
	/**
	 * Takes an input and checks if it matches any operation syntax
	 * @param string The character to analyze
	 * @return 0: Addition; 1: Subtraction; 2: Multiplication; 3: Division; -1: No Operation
	 */
	private int operation(String string)
	{
		if	   (string.equals("+")) return 0;
		else if(string.equals("-")) return 1;
		else if(string.equals("*")) return 2;
		else if(string.equals("/")) return 3;
		return -1;
	}
	
	/**
	 * Goes through all the given commands
	 * @param command The command to search for
	 * @return The index of the command (for switch statements)
	 */
	private int getCommand(String command)
	{
		command = command.split(" ")[0];
		for(int i = 0; i != cmd.length; i ++) if(command.equals(cmd[i])) return i;
		return -1;	
	}
	
	/**
	 * This has been added for output to the Console GUI
	 * @param o The text (object) to be print
	 */
	private void echo(Object o)
	{
		if(textArea != null) textArea.append(o + "");
		else System.out.println(o);
	}
}
