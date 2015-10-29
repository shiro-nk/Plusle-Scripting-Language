package nk.hiroshi.plusle.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JTextArea;

import nk.hiroshi.plusle.script.cmd.Conditional;
import nk.hiroshi.plusle.script.cmd.Function;
import nk.hiroshi.plusle.script.cmd.Script;
import nk.hiroshi.plusle.script.data.Numeral;
import nk.hiroshi.plusle.script.data.Variable;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * The parser takes either a String[] or a File and formats it's contents for execution
 * @author Ryan Kerr
 * @since January 7, 2014
 */
public class Parser 
{
	/** The Resulting Script */
	private Script script = new Script();
	
	/** Storage Variable */
	private String scriptName = "", //Contains the given name of the script (if any)
				   fnName	  = "", //Temporary Storage: holds the name of a function being parsed
				   swap		  = "", //Temporary Storage: holds any variable for short use
				   ifSwap     = "", //Temporary Storage: holds conditional commands
				   elseSwap   = "", //Temporary Storage: holds conditional commands
				   conSwap	  = ""; //Temporary Storage: holds conditional statements
	
	
	private boolean openFunction  = false, // Writes to script when false, Writes to function when true
					openCondition = false; // Checks if conditional statement has been opened
	
	/** Parsed information */
	private ArrayList<String> rawFile 		= new ArrayList<String>(), //Lines read from the file or TextArea
							  refined 		= new ArrayList<String>(), //Refined script (commands only)
							  functionNames = new ArrayList<String>(), //Names of all functions in the script
							  variableNames = new ArrayList<String>(), //Names of all variables in the script
							  numeralNames	= new ArrayList<String>(), //Names of any numerals in the script
							  fncSwapSpace 	= new ArrayList<String>(); //Temporary function command storage

	/** Parsed functions: Functions that are read are stored here; Stored parallel to functionNames */
	private ArrayList<Function> function = new ArrayList<Function>();
	
	/** Parsed Strings: Variables that are read are stored here; Parallel to variableNames */
	private ArrayList<Variable>	variable = new ArrayList<Variable>();
	
	/** Parsed Numbers: Numerals that are read are stored here; Must be stored parallel to numeralNames */
	private ArrayList<Numeral>	numerals = new ArrayList<Numeral>();

	/** Parsed Conditional Statements: Conditional statements are stored here; Conditionals are parsed as indexes instead of names */
	private ArrayList<Conditional> conditions = new ArrayList<Conditional>();
	
	/** The current line being read */
	private int lineNumber = 0;
	
	/** Append text to GUI console */
	private JTextArea console;

	/**
	 * Makes sure the script can be read, and if so, reads it
	 * @param scriptFile File to parse into a script object
	 */
	public Parser(String scriptFile)
	{
		File file = new File(scriptFile);
		
		try
		{
			if(file.exists() && file.isFile() && file.toString().endsWith(".plusle.nk"))
				readFile(scriptFile);
			else 
				throw new ScriptException(0, "Not a .plusle.nk file!");
			refine();
		}
		catch(Exception e) //Voids the script so it cannot be run
		{
			echo(e.getMessage());
			script = null;
		}
	}
	
	/**
	 * Converts the given array to rawFile and refines it
	 * @param input The raw script
	 */
	public Parser(String[] input)
	{
		this(input, null);
	}
	
	/** Redirect output from console to JTextArea */
	public Parser(String[] input, JTextArea out)
	{
		console = out;
		for(String i : input) rawFile.add(i);
		try
		{
			refine();			
		}
		catch(Exception e) //Voids the script
		{
			echo(e.getMessage());
			script = null;
		}
	}
	
	/** @returns The parsed script */
	public Script getScript(){return script;}

	/**
	 * Reads rawFile line by line and parses commands/variables/functions/comments to refined
	 * @throws ScriptException
	 */
	private void refine() throws ScriptException
	{
		for(String line : rawFile)
		{
			lineNumber ++; //New line
			line = line.trim(); //Remove spaces prior to and following the line
			
			if(openCondition && isConditional(line) < 0)
			{
				openCondition = false;
				conditions.add(new Conditional(script, conSwap, new String[] {ifSwap}, new String[]{elseSwap}));
				conSwap  = ""; ifSwap   = ""; elseSwap = "";
				
				if(openFunction) fncSwapSpace.add("if " + (conditions.size() - 1));
				else refined.add("if " + (conditions.size() - 1));
			}
			
			if(isIgnored(line)) line.length(); //Do nothing
			else if(isNameLine(line)) //Set Script name if it hasn't already been set
			{
				if(scriptName.equals(""))
				{
					scriptName = line.substring(2).trim();
					variable.add(new Variable("SCRIPT_NAME", scriptName));
					variableNames.add("SCRIPT_NAME");
				}
			}
			else if(0 <= isVariable(line)) //if is a variable of any kind
			{				
				String[] temporary = line.split(" ");
				swap = "";
				
				switch(isVariable(line))
				{
					case 0: throw new ScriptException(lineNumber, "initialize variable"); //Not enough arguments
					case 1: // String variable
						
						/* Read all characters (except spaces) following the declaration */
						for(int i = 3; i != temporary.length; i++)
							if(i == temporary.length - 1) swap += temporary[i];
							else swap += temporary[i] + " ";
						
						/* Add the variable and it's name to their arrays */
						variable.add(new Variable(temporary[1], swap));
						variableNames.add(temporary[1]);
						break;
					
					case 2: // Double variable
						if(temporary[3].trim().replaceAll("[0-9.]", "").equals("")) //If nothing remains, it is composed of numbers
						{
							numerals.add(new Numeral(temporary[1], Double.parseDouble(temporary[3])));
							numeralNames.add(temporary[1]);
						}
						else throw new ScriptException(lineNumber, "Not a numeral!"); //If something remains, it isn't a number
						break;
				}
			}
			else if(isFunction(line)) 
			{
				if(openFunction)
				{
					function.add(new Function(fnName, fncSwapSpace.toArray(new String[fncSwapSpace.size()]), script));
					functionNames.add(fnName);
					fncSwapSpace.clear();
				}
				
				openFunction = true;
				fnName = line.replaceAll("[^A-Za-z0-9]", "");
			}
			else if(0 <= isConditional(line))
			{
				String[] split = line.split(" ");
				
				switch(isConditional(line))
				{
					case 0:
						if(openCondition)
						{
							conditions.add(new Conditional(script, conSwap, new String[] {ifSwap}, new String[]{elseSwap}));
							conSwap  = ""; ifSwap   = ""; elseSwap = "";
						}
						ifSwap = split[4];
						conSwap = split[1] + ":" + split[2] + ":" + split[3];
						openCondition = true;
						break;
					case 1:
						if(openCondition) elseSwap = split[1];
						else throw new ScriptException(lineNumber, "Unlinked else statement!");
						break;
				}
			}
			else if(openFunction)
			{
				fncSwapSpace.add(line); //Write commands to a function instead of the main method
			}
			else
			{
				refined.add(line); //Write commands to the script's main method
			}
			
			if(isConditional(line) == -1 && openCondition) openFunction = false;;
		}
		
		if(openFunction)
		{
			function.add(new Function(fnName, fncSwapSpace.toArray(new String[fncSwapSpace.size()]), script));
			functionNames.add(fnName);
		}
		
		script.makeScript(
				scriptName,
				refined.toArray(new String[refined.size()]),
				functionNames.toArray(new String[functionNames.size()]),
				function.toArray(new Function[function.size()]),
				variableNames.toArray(new String[variableNames.size()]),
				variable.toArray(new Variable[variable.size()]),
				numeralNames.toArray(new String[numeralNames.size()]),
				numerals.toArray(new Numeral[numerals.size()]),
				conditions.toArray(new Conditional[conditions.size()])
		);
		
		//for(String s : refined) System.out.println("Main: " + s);
		//for(Function f : function) for(String s : f.getRunnable()) System.out.println(f.getName() + ": " + s);
		//for(int i = 0; i != variable.size(); i++) System.out.println(variable.get(i).getName() + ":" + variableNames.get(i));
	}
	
	/** Set rawFile to store all lines from a script file
	 *  @param scriptFile File to read from
	 *  @throws Exception
	 */
	private void readFile(String scriptFile) throws Exception
	{
		Scanner scan = new Scanner(new BufferedReader(new FileReader(scriptFile)));
		while(scan.hasNext()) rawFile.add(scan.nextLine());
		scan.close();
	}
	
	/**
	 * Checks if the line holds a function declaration
	 * @param string
	 * @return true if matches function syntax
	 */
	private boolean isFunction(String string){return string.replaceAll(" ", "").startsWith("[") && string.endsWith("]");}
	
	/**
	 * Returns the type of variable
	 * @param string Line to read
	 * @return 0: Variable without enough arguments; 1: String; 2: Double
	 */
	private int isVariable(String string)
	{
		String s = string.trim();
		if(s.split(" ").length < 4){if(s.startsWith("var:") || s.startsWith("num")) return 0;}
		else if(s.startsWith("var:")) return 1;
		else if(s.startsWith("num:")) return 2;
		return -1;
	}
	
	/** @return true if any of these comment types match */
	private boolean isIgnored(String string)
	{
		String s = string.replaceAll(" ", "");
		return s.equals("") || s.startsWith("#") || s.startsWith("--") ||
			   s.startsWith("//") || s.equals("	");
	}
	
	/** @return -1 if false; 0 if "if", 1 if "else" 
	 *  @throws ScriptException */
	private int isConditional(String string)
	{
		String[] split = string.split(" ");
		if(split[0].equals("if") && split.length == 5) 		  return 0;
		else if(split[0].equals("else") && split.length == 2) return 1;
		return -1;
	}
	
	/** @return true if a name is declared */
	private boolean isNameLine(String string){return string.startsWith("::");}
	
	private void echo(Object o)
	{
		if(console != null) console.append(o + "\n");
		else System.out.println(o);
	}

	/** Special exception format for the parser (states line number and message) */
	private class ScriptException extends Exception
	{
		private static final long serialVersionUID = -2387729064343395623L;
		
		/**
		 * Formats an error to "Error on line #: something went wrong"
		 * @param line The line the error occurred on (should be relative to the raw text, not the filtered ones)
		 * @param message The error message
		 */
		private ScriptException(int line, String message){super("Error on line " + line + ": " + message);}
	}
	
}
