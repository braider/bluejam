package net.parallaxed.bluejam.exceptions;

import net.parallaxed.bluejam.IndividualParameters;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.SequenceParameters;

/**
 * Thrown when a Parameter provided to an instance of
 * SequenceParameters, IndividualParameters or PopulationParameters
 * is invalid.
 * 
 * @see SequenceParameters
 * @see IndividualParameters
 * @see PopulationParameters
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class ParameterException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a vanilla Parameter exception.
	 */
	public ParameterException()
	{
		super("Invalid Parameter Supplied");
	}
	/**
	 * Constructs a ParameterException with a custom message.
	 * @param message The message to add to this exception
	 */
	public ParameterException(String message)
	{
		super(message);
	}
}
