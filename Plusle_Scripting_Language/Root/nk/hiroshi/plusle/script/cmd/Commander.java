package nk.hiroshi.plusle.script.cmd;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * Contains methods that are required for getting information from "Command Type" objects
 * In all honesty, it's not really used for anything
 * @author Ryan Kerr
 * @since 06 January, 2015
 */
interface Commander 
{
	/** @return The Object's Parent (for Functions) */
	public Script getParent();
	
	/** @return The given value */
	public String getValue();

	/** @return The set name */
	public String getName();

	/** @return Any commands from the script that are stored in that object */
	public String[] getRunnable();
	
	/** 
	 * Sets the value of a function
	 * @param value new value of a function
	 */
	public void setValue(String value);
}
