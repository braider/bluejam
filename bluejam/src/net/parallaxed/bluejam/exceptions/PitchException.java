package net.parallaxed.bluejam.exceptions;

import net.parallaxed.bluejam.Note;

/**
 * Thrown when a the pitch of a note is invalid at playtime 
 * (i.e. a note has not successfully been evaluated from Pitch.R.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class PitchException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	/**
	 * Constructs the exception with a reference to the note
	 * that failed Validation.
	 * @param n The note that failed validation.
	 */
	public PitchException(Note n)
	{
		super("The pitch of "+n.toString()+" is invalid");
	}
}
