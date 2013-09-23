package net.parallaxed.bluejam.exceptions;

import net.parallaxed.bluejam.Population;

/**
 * Thrown when an error in breeding occurs. Plenty of pun potential here...
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class BreedException extends Exception {
	public static final long serialVersionUID = 1L;
	
	/**
	 * Which population the exception occurred in
	 */
	public Population targetPopulation = null;
	
	/**
	 * Constructs a vanilla breeding exception.
	 */
	public BreedException()
	{
		super("Error while breeding.");
	}
	
	/**
	 * Associates the constructed BreedException with a Population
	 * @see Population
	 * @param population Which population the exception occurred in
	 */
	public BreedException(Population population) 
	{
		targetPopulation = population;
	}
	/**
	 * Adds a custom message to this BreedException.
	 * @param message The message to add
	 * @param population Which population the exception occurred in
	 */
	public BreedException(String message, Population population) 
	{
		super(message);
		targetPopulation = population;
	}
}
