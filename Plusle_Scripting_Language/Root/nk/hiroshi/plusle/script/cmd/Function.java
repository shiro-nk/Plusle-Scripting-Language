package nk.hiroshi.plusle.script.cmd;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * A "mini" script. Has commands but doesn't have variables
 * @author Ryan Kerr
 * @since 06 January, 2015
 */
public class Function implements Commander
{
	/** The script which the function belongs to. 
	 * This allows for this function to run other functions and access variables */
	private final Script PARENT;
	
	/** The name of the function (not used) */
	private final String NAME;
	
	/** The commands that are written into the function */
	private final String[] COMMANDS;
	
	/** Return value of the function */
	private String value;
	
	/**
	 * Creates a new function object
	 * @param n The name of the function
	 * @param c The commands written into the function
	 * @param p The parent script
	 */
	public Function(String n, String[] c, Script p)
	{
		NAME = n;
		COMMANDS = c;
		PARENT = p;
	}
	
	@Override
	public String getName(){return NAME;}
	
	@Override
	public Script getParent(){return PARENT;}

	@Override
	public String getValue(){return value;}

	@Override
	public String[] getRunnable(){return COMMANDS;}

	@Override
	public void setValue(String value){this.value = value;}
}
