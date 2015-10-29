package nk.hiroshi.plusle.script.data;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * This DataType is intended for strings. It is just an extended DataType with a specific type argument.
 * @author Ryan Kerr
 * @since 12 January, 2015
 */
public class Variable extends DataType<String> 
{
	/**
	 * Creates a String variable
	 * @param n Name
	 * @param v Value
	 */
	public Variable(String n, String v){super(n, v);}

	@Override
	public void setValue(String v){set(v);}
	
	@Override
	public String getValue(){return get();}
}
