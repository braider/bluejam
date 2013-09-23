package net.parallaxed.bluejam.exceptions;

import net.parallaxed.bluejam.NoteSequence;

/**
 * Thrown when a note (or sequence of notes) cannot be added 
 * to a NoteSequence instance.
 * 
 * This class implements NoteSequence to extract the notes
 * that failed through an Iterator<Note> in the given order.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class NoteAddException extends SequenceException implements NoteSequence
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * NoteAddException is caused by a NoteSequence not accepting
	 * the notes that were added to it. This can be caught and 
	 * the failing notes can be dealt with.
	 * 
	 * @param n The notes that failed in the add operation.
	 */
	public NoteAddException(NoteSequence n) {
		super(n);
	}
}
