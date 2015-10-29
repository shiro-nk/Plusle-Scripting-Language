package nk.hiroshi.plusle;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;

import nk.hiroshi.plusle.runtime.Interpreter;
import nk.hiroshi.plusle.runtime.Parser;

/* This file is part of Plusle Scripting Language *
 * Copyright (C) 2014-2015 Ryan Kerr              *
 * Please refer to the MIT license                */

/**
 * This class contains the GUI and Main method
 * @author Ryan Kerr
 * @since 17 January, 2015
 */
public class ScriptCore implements ActionListener
{
	/**
	 * Instantiates the core and runs the splash screen
	 * @param args
	 */
	public static void main(String[] args)
	{
		ScriptCore core = new ScriptCore();
		try
		{
			core.splashScreen();
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}
	
	/** The main window */
	private JFrame plusleWindow;
	
	/** The panel which stores buttons */
	private JPanel buttonPane;
	
	/** Allow scrolling for the editor and console */
	private JScrollPane scriptPane, consolePane;
	
	/** Button labels */
	private String[]  bLabels = {"Run", "Clear", "Load", "Save", "About", "Syntax", "Help", "Quit"};
	
	/** Corresponding buttons (for each label, there is a button) */
	private JButton[] buttons = new JButton[bLabels.length];
	
	/** The Script editor and the output area */
	private JTextArea scriptArea,  //Script editor
					  consoleArea; //Output area
	
	/** Image Labels */
	private JLabel background, //Image that lays at the back of all components
				   logo;	   //The program logo
	
	/** The parsing class for reading text from the scriptArea */
	private Parser parser;
	
	/** The interpreter class for executing commands */
	private Interpreter inter;
	
	/** JFileChooser to allow the user to load files and save files */
	private JFileChooser fileChooser;
	
	/**
	 * Creates the Scripting Environment GUI
	 * @throws IOException in the event an image is not found
	 */
	private void build() throws IOException
	{
		//Nimbus looks better than the default
		try{UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");}
		catch(Exception e){e.printStackTrace();}
		
		//The file chooser
		fileChooser = new JFileChooser(System.getProperty("java.class.path"));
//		fileChooser.setFileFilter(new FileNameExtensionFilter("Plusle File", ".plusle.nk"));
		
		//The main JFrame
		plusleWindow = new JFrame();
		plusleWindow.setLayout(null);
		plusleWindow.setSize(1000, 600);
		
		//The background
		background = new JLabel(new ImageIcon(getResource("background.png").getScaledInstance(1000, 600, 0)));
		background.setBounds(0, 0, 1000, 600);
		
		//The プラスル Logo in the corner
		logo = new JLabel(new ImageIcon(getResource("splash.png").getScaledInstance(300, 206, 100)));
		logo.setBounds(700, -47, 300, 206);
		
		//The panel which contains the buttons
		buttonPane = new JPanel(null);
		buttonPane.setOpaque(false);
		buttonPane.setBounds(523, 100, 500, 250);
		
			//As you can see, Layouts are not my thing
			int X = 0, Y = 0, W = 1000 / 10, H = 600 / 20, aY = Y + 35, aX = 120, x=0;
		
			for(int i = 0; i!=buttons.length; i++)
			{
				if(i % 4 == 0){X = 0; Y += aY; x = 0;}
				
				buttons[i] = new JButton(bLabels[i]);
				buttons[i].setBounds(X + (aX * x), Y, W, H);
				buttons[i].addActionListener(this);
				buttonPane.add(buttons[i]);
				x++;
			}
		
		//The script editor with a default welcome script
		scriptArea = new JTextArea();
		scriptArea.setFont(new Font("monospaced", Font.PLAIN, 12));
		scriptArea.setText
		(
			"::Hello World Script\n\n"
			+ "prompt \"Hello, \"\n"
			+ "get user.name\n"
			+ "echo \"!\"\n"
			+ "echo \"Welcome to the Ryan Kerr's Plusle Scripting Environment\"\n\n"
			+ "//This is a variable (String)\n"
			+ "var: h = Hi\n"
			+ "h = \"Hello\"\n\n"
			+ "//This is a numeral (Double)\n"
			+ "num: pi = 3.14\n\n"
			+ "//This is a conditional statement\n"
			+ "if $pi = 3.14 pie\n"
			+ "else pie\n\n"
			+ "//This is a function call\n"
			+ "say\n\n"
			+ "//This is a function declaration\n"
			+ "[Function]\n"
			+ "return \"World!\"\n\n"
			+ "[pie]\n"
			+ "echo \"This has been the result of echo statements!\"\n\n"
			+ "[say]\n"
//			+ "//A $ Sign denotes a variable replacement; \n//An @ sign denotes the value of a function\n"
			+ "echo $h & \" \" & @Function"
		);
		
		//Script Editor Container
		scriptPane = new JScrollPane(scriptArea);
		scriptPane.setBounds(10, 10, 1000 / 2, 600 - 45);
		scriptPane.setBorder(null);
		
		//The output area
		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consoleArea.setHighlighter(null);
		consoleArea.setFont(new Font("monospaced", Font.PLAIN, 12));

		//Automatically scroll to the bottom of the Console area
		DefaultCaret caret = (DefaultCaret) consoleArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		//Allow scrolling
		consolePane = new JScrollPane(consoleArea);
		consolePane.setBounds((1000 / 2) + 20, (600 / 3) + 15, 1000 / 2 - 30, ((600 / 3) * 2) - 45);
		consolePane.setBorder(BorderFactory.createTitledBorder(null, "Output", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.LEFT, null, Color.RED));

		//Setup the plusleWindow
		plusleWindow.add(scriptPane);
		plusleWindow.add(consolePane);
		plusleWindow.add(buttonPane);
		plusleWindow.add(logo);
		plusleWindow.add(background);
		plusleWindow.getContentPane().setBackground(new Color(0x4b4b4b));
		plusleWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		plusleWindow.setTitle("プラスル  Scripting Environment");
		plusleWindow.setResizable(false);
		plusleWindow.setLocationRelativeTo(null);
		plusleWindow.setIconImage(getResource("icon.png"));
	}
	
	/**
	 * Sets up a splash screen (gets image and makes window the same size). This shows while the program loads + 1000ms. Application is shown after
	 * @throws Exception No image
	 */
	private void splashScreen() throws Exception
	{
		JDialog win = new JDialog();
		JLabel  img = new JLabel(new ImageIcon(getResource("splash-large.png")));
		
		win.add(img);
		win.setUndecorated(true);
		win.setBackground(new Color(0, 0, 0, 0));
		
		win.setSize(img.getIcon().getIconWidth(), img.getIcon().getIconHeight());
		win.setLocationRelativeTo(null);
		win.toFront();
		win.setVisible(true);
		build();
		Thread.sleep(1000);
		win.setVisible(false);

		win.dispose();
		plusleWindow.setVisible(true);
	}
	/**
	 * Accesses the runtime to get a resource (used in one of my older programs. "res/img.png" doesn't work in jars)
	 * @param res The image to load
	 * @return The image
	 * @throws IOException image not found or loaded
	 */
	private BufferedImage getResource(String res) throws IOException
	{
		return ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(res));
	}
	
	/**
	 * Opens the filchooser dialog
	 * @param isSaveDialog true: Save button, false: open button
	 * @return The file name that was chosen
	 * @throws Exception if the cancel button was pressed
	 */
	private String fileChooser(boolean isSaveDialog) throws Exception
	{
		int result;
		if(isSaveDialog) result = fileChooser.showSaveDialog(plusleWindow);
		else result = fileChooser.showOpenDialog(plusleWindow);
		
		if(result == JFileChooser.CANCEL_OPTION) throw new Exception("Operation Cancelled");
		return fileChooser.getSelectedFile().toString();
	}
	
	/**
	 * Backup text in script editor <br>
	 * I didn't like calling up the JFileChooser each time I pressed Quit when debugging
	 */
	private void backup()
	{
		try 
		{
			save("script_backup.plusle.nk", scriptArea.getText().split("\n"));
		} 
		catch (IOException e)
		{
			consoleArea.setText("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes text to file
	 * @param file File to write to
	 * @param input Lines to write
	 * @throws IOException In the event writing is not possible 
	 */
	private static void save(String file, String[] input) throws IOException
	{
		if(!file.endsWith(".plusle.nk")) file += ".plusle.nk";
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
		for(String s : input) pw.println(s);
		pw.close();
	}
	
	/**
	 * Read lines from a file
	 * @param file File to read from
	 * @return The lines that were read
	 * @throws Exception
	 */
	private static String[] load(String file) throws Exception
	{
		if(file.endsWith(".plusle.nk"))
		{
			Scanner scan = new Scanner(new BufferedReader(new FileReader(file)));
			ArrayList<String> input = new ArrayList<String>();
			while(scan.hasNext()) input.add(scan.nextLine());
			scan.close();
			return input.toArray(new String[input.size()]);
		}
		else throw new Exception("File is not a .plusle.nk file");
	}

	/**
	 * This is intended only for the Buttons. Buttons are identified and trigger their corresponding by label, not object.
	 */
	public void actionPerformed(ActionEvent action) 
	{
		Object src = action.getSource();
		
		if(src.getClass().getSimpleName().equals("JButton"))
		{
			JButton source = (JButton) src;
			String  label = source.getText();
			
			switch(label)
			{
				case "Run":
					consoleArea.setText("");
					parser = new Parser(scriptArea.getText().split("\n"), consoleArea);
					inter = new Interpreter(parser.getScript(), consoleArea);
					inter.run();
					break;
				
				case "Save":
					try{save(fileChooser(true), scriptArea.getText().split("\n"));}
					catch(Exception e)
					{
						consoleArea.setText("Error: " + e.getMessage());
					}
					break;
				case "Load":
					String[] load;
					try
					{
						String file = fileChooser(false);
						load = load(file);
					}
					catch(Exception e)
					{
						consoleArea.setText("Error: " + e.getMessage());
						load = scriptArea.getText().split("\n");
					}
					scriptArea.setText("");
					for(String line : load) scriptArea.setText(scriptArea.getText() + line + "\n");
					break;
					
				case "Syntax": //Syntax Reference
					consoleArea.setText("");
					parser = new Parser(new String[]
					{
						"echo \"[a] Denotes Function\"",
						"echo \"'$' Denotes Variable\"",
						"echo \"'@' Denotes Function Value\"",
						"echo \"'&' Splits Text\"",
						"echo \"// Comment\"",
						"echo \"#  Comment\"",
						"echo \"-- Comment\"",
						"echo \"<var> = \"Set <var> value\"\"",
						"echo \"<num> = Set <num> value\"",
						"echo \"<num> = <num A> <+-*/> <num B>\""
					});
					inter = new Interpreter(parser.getScript(), consoleArea);
					inter.run();
					break;
					
				case "About": //Information script is run
					consoleArea.setText("");
					parser = new Parser(new String[]
					{
						"echo \"Project:   ICS3U Summative\"",
						"echo \"Name:      Plusle Scripting Language\"",
						"echo \"Author:    Ryan Hiroshi Kerr\"",
						"echo \"Since:     January 7, 2015\"",
						"echo \"Completed: January 20, 2015\""
					}, consoleArea);
					inter = new Interpreter(parser.getScript(), consoleArea);
					inter.run();
					break;

				case "Help": //Command Reference Script is Run
					consoleArea.setText("");
					parser = new Parser(new String[]
					{
						"echo \"echo <text> || Printlns text to the output\"",
						"echo \"prompt <text> || Prints text to the output\"",
						"echo \"return <text> || Sets the value of a function\"",
						"echo \"if <condition> <function> || Checks <condition>\"",
						"echo \"else <function> || Runs the function if false\"",
						"echo \"get <system.property> || Prints system property\""
					});
					inter = new Interpreter(parser.getScript(), consoleArea);
					inter.run();
					break;
					
				case "Clear": //Backup and clear text
					backup();
					consoleArea.setText("");
					scriptArea.setText("");
					break;
					
				case "Quit": //Backup and exit
					backup();
					plusleWindow.dispose();
					System.exit(0);
					break;
			}
		}
	}
}
