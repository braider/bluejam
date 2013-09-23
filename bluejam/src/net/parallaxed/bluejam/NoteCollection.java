package net.parallaxed.bluejam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.ValidationException;

/**
 * Simply contains a list of notes in their order of play.
 * 
 * Note that this class is recursively typed, even though it
 * wraps a collection of <Note>, those elements added to it 
 * MUST also implement NoteSequence.
 * 
 * This class acts as a buffer between the playback classes 
 * and the evolution classes (i.e. note trees etc).
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class NoteCollection extends ArrayList<Note> implements NoteSequence
{
	private static final long serialVersionUID = 1L;
	/*
	 * Wraps this HashMap to provide support for id'd notes.
	 */
	private HashMap<Integer,Note> _idMap = new HashMap<Integer, Note>();
	/**
	 * Constructs a vanilla NoteCollection
	 */
	public NoteCollection()
	{
		
	}
	
	/**
	 * Will check if a passed note has an ID, if so
	 * it will also be added to an internal HashMap for
	 * later retrieval.
	 */
	public boolean add(Note n)
	{
		if (n == null)
			return false;
		// TODO Build interface for use of _idMap
		if (n.getClass() == NoteLeaf.class)
		{
			NoteLeaf nl = (NoteLeaf) n;
			if (nl.id > -1)
				_idMap.put(nl.id, n);
		}
		return super.add(n);
	}
	
	public Note get(int index)
	{
		return super.get(index);

	}
	
	/**
	 * To preserve original functionality, this simply calls the superclass get()
	 * @param index The index to retrieve.
	 * @return The note at index.
	 */
	public Note getIndex(int index)
	{
		Note n = _idMap.get(index);
		if (n != null)
			return n;
		
		if (index >= size())
			return null;
		
		return super.get(index);
	}
	/**
	 * Constructs a NoteCollection with the passed sequenceParameters
	 * 
	 * This is useful for subclasses that require context in their
	 * Collection.
	 * @param sequenceParameters
	 */
	public NoteCollection(SequenceParameters sequenceParameters) {
		_sp = sequenceParameters;
	}
	
	/**
	 * @return A reference to this NoteCollection's SequenceParameters.
	 * @see SequenceParameters 
	 */
	public SequenceParameters sequenceParameters() {
		return _sp;
	}
	
	private SequenceParameters _sp = null;
	
	
	/**
	 * Appends the passed NoteSeqeunce to the NoteCollection.
	 * @param notes The notes to be appended.
	 * @return Always true for this implementation.
	 */
	public boolean addNotes(NoteSequence notes) 
	{
		Iterator<Note> n = notes.getNotes();
		while (n.hasNext())
			this.add(n.next());
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * @return An iterator over this NoteCollection
	 */	
	public Iterator<Note> getNotes() {
		return this.iterator();
	}
	/**
	 * {@inheritDoc}
	 * @see NoteSequence
	 */
	public void removeNotes(NoteSequence notes) {
		Iterator<Note> n = notes.getNotes();
		while (n.hasNext())
			this.remove(n.next());
	}
	
	/**
	 * {@inheritDoc}
	 * @see NoteSequence
	 */
	public boolean swapNotes(NoteSequence swapOut, NoteSequence swapIn) {
		throw new RuntimeException("Cannot swap notes in a NoteCollection");	
	}
	
	/**
	 * Crops a noteCollection returning all elements from the
	 * specified index to the end of the collection.
	 * 
	 * @param index The note index where the crop begins
	 * @return A cropped copy of this NoteCollection
	 */
	public NoteCollection crop(int index)
	{
		return crop(index,size());
	}
	
	/**
	 * Crops a noteCollection returning all elements from the
	 * specified index to the point specified.
	 * 
	 * @param index The note index where the crop begins
	 * @param end Where to stop the crop.
	 * @return A cropped copy of this NoteCollection 
	 */
	public NoteCollection crop(int index, int end)
	{
		NoteCollection n = (NoteCollection) clone();
		n.removeRange(index, end);
		return n;
	}	
	
	/**
	 * Attempts to call validateNotes() on all it's children.
	 */
	public void validateNotes() throws ValidationException
	{
		Iterator<Note> i = iterator();
		while (i.hasNext())
		{			
			try { NoteSequence n = (NoteSequence) i.next(); n.validateNotes(); }
			catch (ClassCastException e) {ErrorFeedback.handle(e.getMessage(), e); }
		}
	}
	
	/**
	 * NoteCollections are note cloneable (...yet)
	 */
	public NoteCollection clone() {
		throw new RuntimeException("NoteCollections are not Cloneable... please see the API for more information.");
	}
	
	/**
	 * @param n The NoteSequence to search for
	 * @return True if this collection contains the passed NoteSequence.
	 */
	public boolean contains(NoteSequence n) {
		return this.contains(n);
	}
}
