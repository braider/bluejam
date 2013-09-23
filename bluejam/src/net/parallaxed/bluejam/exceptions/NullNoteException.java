package net.parallaxed.bluejam.exceptions;

import net.parallaxed.bluejam.NoteTree;

/**
 * Thrown when a Note reference returned by a call
 * to NoteSequence.getNotes() is null. This is only ever
 * thrown during validation, and should normally be ignored.
 * 
 * This error is specific to NoteTrees.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class NullNoteException extends ValidationException {
	private static final long serialVersionUID = 1L;
	/**
	 * The node containing the null child.
	 */
	public NoteTree node = null;
	/**
	 * Constructs a NullNoteException referencing the tree node
	 * in which the exception occurred (if any)
	 * @param node
	 */
	public NullNoteException(NoteTree node) {
		this.node = node;
	}	
}
