package nk.hiroshi.plusle.script.data;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * This DataType allows for storage of doubles in scripts. 
 * This DataType also provides simple mathematical operations.
 * 
 * @author Ryan Kerr
 * @since 12 January 2015
 */
public class Numeral extends DataType<Double>
{
	/** Operation identifiers (for the switch statement) */
	private static final int ADD = 0,
							 SUBTRACT = 1,
							 MULTIPLY = 2,
							 DIVIDE   = 3;
	
	public Numeral(String n, Double v){super(n, v);}

	@Override
	public void setValue(Double v){set(v);}

	@Override
	public Double getValue(){return get();}
	
	/**
	 * Sets the value of Numeral A to the result of the operation of Numerals b and c
	 * 
	 * @param operation Addition, Subtraction, Multiplication, or Division
	 * @param a Gets the resulting value
	 * @param b First term in the operation
	 * @param c Second term in the operation
	 */
	private static void operation(int operation, Numeral a, Numeral b, Numeral c)
	{
		double value = 0.0;

		switch(operation)
		{
			case 0: value = b.getValue() + c.getValue(); break;
			case 1: value = b.getValue() - c.getValue(); break;
			case 2: value = b.getValue() * c.getValue(); break;
			case 3: value = b.getValue() / c.getValue(); break;
		}
		a.setValue(value);
	}
	
	/** a = b + c */
	public static void add(Numeral a, Numeral b, Numeral c){operation(ADD, a, b ,c);}
	
	/** a = b - c */
	public static void subtract(Numeral a, Numeral b, Numeral c){operation(SUBTRACT, a, b, c);}
	
	/** a = b * c */
	public static void multiply(Numeral a, Numeral b, Numeral c){operation(MULTIPLY, a, b, c);}
	
	/** a = b / c */
	public static void divide(Numeral a, Numeral b, Numeral c){operation(DIVIDE, a, b ,c);}
}
