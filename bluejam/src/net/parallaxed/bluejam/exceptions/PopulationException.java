package net.parallaxed.bluejam.exceptions;

/**
 * Thrown when a generic error occurs in the population.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class PopulationException extends Exception{
	public static final long serialVersionUID = 1L;
	/**
	 * Constructs the exception with a message explaining what
	 * happened.
	 * @param message An explanatory snippet of text.
	 */
	public PopulationException(String message)
	{
		super(message);
	}
	/**
	 * A vanilla populationException.
	 */
	public PopulationException() {}
}
