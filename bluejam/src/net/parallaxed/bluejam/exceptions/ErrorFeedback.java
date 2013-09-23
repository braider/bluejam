package net.parallaxed.bluejam.exceptions;


/**
 * This class is designed to capture all exceptions from
 * the program and output them to some sensible location.
 * Originally designed for the planned SWT interface, this
 * class may be extended in the future to support outputting
 * errors to the GUI in the proper way.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class ErrorFeedback 
{
	private ErrorFeedback()
	{
		
	}
	
	/**
	 * Prints out a custom error message to the appropriate location
	 *  
	 * @param message The message to display
	 * @param e The causing exception
	 */
	public static void handle(String message, Exception e)
	{		
		System.out.println(message+"\r\n");
		handle(e);
	}
	
	/**
	 * Handles regular exceptions. Some get error
	 * messages printed, some get silenced.
	 * 
	 * @param e The exception to handle.
	 */
	public static void handle(Exception e)
	{
		
		if (e.getClass() == NullNoteException.class)
			return;
		if (e.getClass() == BreedException.class)
			return;
		if (e.getClass() == OutOfMemoryError.class)
			return;
		if (e.getClass() == ValidationException.class)
			return;
		if (e.getMessage().matches("(?i).*?WARNING.*"))
			return;
		
		throw new RuntimeException(e);
	}
	
	/* Not Implemented *
	public static void setDisplay(MessageDisplay screen)
	{
		
	}
	
	public static final void display(String message)
	{
		
	}
	
	public static final void display(String message, Exception e)
	{
		
	}
	*/
}
