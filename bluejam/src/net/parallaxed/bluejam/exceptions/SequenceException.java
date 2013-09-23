package net.parallaxed.bluejam.exceptions;

import java.util.Iterator;

import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.Rhythm;
import net.parallaxed.bluejam.SequenceParameters;

/**
 * Thrown when an exception in a NoteSequence is encountered.
 * Other exceptions extend this class and provide more specific
 * information about the nature of the failure that occurred.
 * @see NoteAddException
 */
public class SequenceException extends Exception implements NoteSequence  {
	private static final long serialVersionUID = 1L;
	
	private NoteSequence _failedSequence = null;
	
	////// Fail Index
	private int _failedIndex = -1;
	/**
	 * Sometimes this field is applicable if the Exception 
	 * occurred among a group of notes that are passed
	 * to the constructor.
	 * 
	 * @return The index of the sequence at which Exception was thrown.
	 */
	public int failedIndex() { return _failedIndex; }
	//////
	
	/**
	 * Trivial Constructor.
	 */
	public SequenceException()
	{
		
	}
	
	/**
	 * Constructs a SequenceException
	 * @param notes The NoteSequence that caused the exception.
	 */
	public SequenceException(NoteSequence notes)	{
		_failedSequence = notes;
	}
	
	/**
	 * Constructs a SequenceException
	 * @param notes The NoteSequence that caused the exception.
	 * @param failedIndex If applicable, the index at which the NoteSequence failed
	 */
	public SequenceException(NoteSequence notes, int failedIndex)
	{
		this(notes);
		_failedIndex = failedIndex;
	}
	
	/**
	 * Returns an iterator over the failed notes.
	 */
	public Iterator<Note> getNotes() {
		return _failedSequence.getNotes();
	}
	
	/**
	 * Will return the SequenceParameters object that was in the 
	 * failing sequence.
	 */
	public SequenceParameters sequenceParameters() { 
		return _failedSequence.sequenceParameters();
	}	
	
	/**
	 * This method does nothing. NoteSequence mutations are not supported
	 * in exception classes.
	 */
	public boolean addNotes(NoteSequence notes) throws SequenceException 
	{		
		return false;
	}
	
	/**
	 * This method does nothing. NoteSequence mutations are not supported
	 * in exception classes.
	 */
	public void removeNotes(NoteSequence notes) 
	{
		
	}
	
	/**
	 * This method does nothing. NoteSequence mutations are not supported
	 * in exception classes.
	 */
	public boolean swapNotes(NoteSequence swapOut, NoteSequence swapIn) {
		return false;
	}
	
	/**
	 * The SequenceException class cannot perform any validation on its
	 * internals. This method just returns.
	 */
	public void validateNotes() {
		return;		
	}
	
	/**
	 * Clones are not supported from exception classes.
	 * 
	 * This method will always throw an exception.
	 * 
	 * @return This method never returns.
	 * 
	 */
	public NoteSequence clone() throws CloneNotSupportedException  {
		throw new CloneNotSupportedException();
	}
	
	/**
	 * Checks if this SequenceException contains the passed note.
	 * @return True if we do.
	 */
	public boolean contains(NoteSequence n)
	{
		return _failedSequence.contains(n);
	}
	
	/**
	 * TODO Enhance to return the smallest rhythm in the collection.
	 * @return The rhythm at which this SequenceException occured if there is one.
	 */
	public Rhythm getRhythm() {
		return _failedSequence.getNotes().next().rhythm();
	}
}
