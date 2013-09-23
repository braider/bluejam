package net.parallaxed.bluejam.exceptions;

import net.parallaxed.bluejam.Note;

/**
 * Thrown when the rhythm of a note fails validation.
 * 
 * This most likely occurs if a note is found at the wrong
 * depth in a note tree, or alternatively, the rhythm of the
 * note was not assigned at all.s
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class RhythmException extends RuntimeException 
{
	private static final long serialVersionUID = 1L;
	/**
	 * Constructs the exception with a reference to the note
	 * that failed validation.
	 * @param n The note that failed.
	 */
	public RhythmException(Note n)
	{
		super("The rhythm of "+n.toString()+" is invalid");
	}
}
