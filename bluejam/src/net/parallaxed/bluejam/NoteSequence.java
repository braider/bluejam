package net.parallaxed.bluejam;

import java.util.Iterator;

import net.parallaxed.bluejam.exceptions.SequenceException;
import net.parallaxed.bluejam.exceptions.ValidationException;

/**
 * NoteSequence is the interface describing operations that can be
 * performed over a sequence of Notes.
 * 
 * This class fully abstracts it's implementing classes to 
 * potentially support alternatives to tree-based GP/EA approaches.
 * 
 * The default implementation of NoteSequence is NoteTree/NoteLeaf,
 * each having additional operations supporting a subset of GP
 * operations.
 * 
 * NoteSequence provides the basic functionality for 1-point crossover 
 * and mutation, with replaceNotes().
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface NoteSequence extends Cloneable
{
	/**
	 * This abstracted function returns an iterator capable of
	 * accessing each note in the NoteSequence in an ordered
	 * fashion. Since an the representation of a note tree
	 * may be in no particular order, implementation of this
	 * function forces an order to be imposed at some point.
	 * 
	 * In short, this allows the NoteSequence to be read into
	 * a buffer for playback through one of the appropriate classes
	 * 
	 * NoteSequences are normally collapsed into NoteCollections
	 * using this iterator, to fix the ordering before playback.
	 * 
	 * __ADD SEE CLAUSES__
	 * @return An iterator over all the notes in the NoteSequence
	 */
	public Iterator<Note> getNotes();  
	
	/**
	 * Removes a note from the NoteSequence.
	 * 
	 * Removal of a note should (in a linear representation) shift
	 * all subsequent notes left. For removing a note without this
	 * behaviour, see restNote()
	 * 
	 * @param notes A reference to the note to be removed.
	 */
	public void removeNotes(NoteSequence notes);
	
	/**
	 * Swaps notes in a sequence. The supplied NoteSequence is
	 * located, and replaced by the given NoteSequence.
	 * 
	 * Depending on the implementation, this function can be
	 * designed to act as mutation, 1-point crossover, or both.
	 *
	 * @param swapOut First argument to swap 
	 * @param swapIn Second argument to swap
	 */
	public boolean swapNotes(NoteSequence swapOut, NoteSequence swapIn) throws SequenceException;
	public boolean addNotes(NoteSequence notes) throws SequenceException;
	
	/**
	 * Called before locking the note to make sure all
	 * properties covered by the lockMask are set.
	 * 
	 * i.e. if lockMask = MUTABLE_RHYTHM, all the rhythm
	 * related properties will be checked before setting
	 * this.mutable.
	 * 	
	 * This method ensures all notes in the sequence are 
	 * ready for playback. Must be called before any attempt to
	 * read the noteValue or duration fields.
	 */
	public void validateNotes() throws ValidationException;
	
	/**
	 * Returns the parameters of this sequence. Normally only
	 * valid in function implementations, Terminals are not
	 * required to provide a link to their parameters.
	 * 
	 * @return A reference to the sequence parameters for this NoteSequence.
	 */
	public SequenceParameters sequenceParameters();
	
	/**
	 * If supported, this should return a copy of this 
	 * NoteSequence independent from the original. This should
	 * be used mainly when an implementation wants to produce
	 * children or copies of itself.
	 * 
	 * Heuristics use this method to return copies of
	 * themselves which can then be mutated.
	 * 
	 * @return A copy of this NoteSeqeunce
	 * @throws CloneNotSupportedException
	 */
	public NoteSequence clone() throws CloneNotSupportedException;
	
	/**
	 * @param n The NoteSequence to look for
	 * @return True if this NoteSequence contains n
	 */
	public boolean contains(NoteSequence n);
}
