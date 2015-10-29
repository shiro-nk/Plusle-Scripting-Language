package nk.hiroshi.plusle.script.data;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * The template for Script defined variables
 * 
 * @author Ryan Kerr
 * @since 07 January, 2015
 * @param <Type> The type of DataType that should be used. Recommended to stick with primitive DataTypes
 */
abstract class DataType<Type>
{
	/** The name of the DataType */
	private final String name;
	
	/** The value of the DataType*/
	private Type value;
	
	/**
	 * @param n The name of the variable
	 * @param v The value of the variable
	 */
	public DataType(String n, Type v)
	{
		name  = n;
		value = v;
	}
	
	/** @return Given Name */
	public String getName(){return name;}
	
	/** Set the value of the DataType */
	public abstract void setValue(Type v);
	
	/** Get the value of the DataType */
	public abstract Type getValue(); // This couldn't be implemented in this class because of <Type> conflicts
	
	/** @return the Type of Data */
	public Class<?> getType(){return value.getClass();}
	
	/** @return The Type of Data (simple) */
	public String getDataType(){return getType().getSimpleName();}
	
	public String toString(){return getName() + " " + getDataType() + " " + value;}
	
	/** Get the value of the DataType */
	protected Type get(){return value;}
	
	/** Set the value of the DataType */
	protected void set(Type v){value = v;}
}
