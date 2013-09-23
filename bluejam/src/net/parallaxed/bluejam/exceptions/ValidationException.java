package net.parallaxed.bluejam.exceptions;

import net.parallaxed.bluejam.Note;

/**
 * Thrown when an exception occurs in validation.
 * 
 * This class can internally store two separate exceptions
 * to describe the nature of the exception that occurred.
 * 
 * These can be inspected at runtime to ascertain a course
 * of action.
 * 
 * @see RhythmException
 * @see PitchException
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;
	private RhythmException _rhythm = null;
	private PitchException _pitch = null;
	
	/**
	 * Constructs a vanilla ValidationException
	 */
	protected ValidationException() { }
	
	/**
	 * Constructs the exception with a reference to the note
	 * that failed validation.
	 * @param n The note that failed validation.
	 */
	public ValidationException(Note n) 
	{		
		super("Note "+n.toString()+" failed validation");
	}
	
	/**
	 * Appends the passed RhythmException to this ValidationException.
	 * @param e The RhythmException that occurred in the given Note.
	 */
	public void append(RhythmException e) { _rhythm = e; }

	/**
	 * Appends the passed PitchException to this ValidationException.
	 * @param e The PitchException that occurred in the given Note.
	 */
	public void append(PitchException e) { _pitch = e; }
	
	/**
	 * @return The RhythmException that occurred (if any), or null.
	 */	
	public RhythmException isRhythmException() { return _rhythm; }

	/**
	 * @return The PitchException that occurred (if any), or null.
	 */	
	public PitchException isPitchException() { return _pitch; }
}
