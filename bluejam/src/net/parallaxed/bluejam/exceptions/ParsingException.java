package net.parallaxed.bluejam.exceptions;

import java.io.File;

/**
 * Thrown when the TreeParser encounters an exception while reading
 * the supplied Tree file.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class ParsingException extends Exception 
{
	private static final long serialVersionUID = 1L;
	/**
	 * Constructs a vanilla parsingException.
	 */
	public ParsingException()
	{
		super("");
	}
	
	/**
	 * References the file that failed parsing
	 * @param f The file that failed
	 */
	public ParsingException(File f)
	{
		super("Error while parsing file "+f.getAbsolutePath());
	}
	
	/**
	 * Constructs the exception with a custom message.
	 * @param message The message to add.
	 */
	public ParsingException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructs an exception with both types of information
	 * @param message The custom message
	 * @param f The file that failed parsing
	 */
	public ParsingException(String message, File f)
	{
		super(message + (f == null ? "" : f.getAbsolutePath()));
	}
}
