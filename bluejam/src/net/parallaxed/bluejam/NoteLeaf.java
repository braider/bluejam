package net.parallaxed.bluejam;

import java.util.Iterator;

import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.NoteAddException;
import net.parallaxed.bluejam.exceptions.PitchException;
import net.parallaxed.bluejam.exceptions.RhythmException;
import net.parallaxed.bluejam.exceptions.SequenceException;
import net.parallaxed.bluejam.exceptions.ValidationException;
import net.parallaxed.bluejam.playback.MIDI;
import net.parallaxed.bluejam.playback.Saveable;

/**
 * Implements a unary note-sequence (only one note in the 
 * sequence)
 * 
 * NoteLeaf instances extend the functionality of Note, such that
 * it can be added and manipulated within a NoteTree. It is part
 * of the default BlueJam implementation.
 * 
 * Instances of this class can be manipulated in the same way as
 * a note sequences, to add more notes (branch off) or swap
 * itself.
 * 
 * This class also implements Terminal, inferring that it can be
 * part of a terminal set, and that when added to a NoteTree, the
 * leaf is only manipulable within the bounds given by it's lockMask.
 * 
 * Note that a Terminal on a NoteTree can only be condensed by having
 * it's parent swapped out, it cannot be removed, only cleared (or
 * "rested").
 * 
 * Consider NoteLeaf interning by using a HashMap over the heap.
 * 
 * @see NoteSequence
 * @see Note
 */
public class NoteLeaf extends Note implements NoteSequence, Cloneable, Terminal, Saveable
{
	private static final String E_REL_CONTEXT = 
		"Unable to evaluate pitch: this NoteLeaf has not been added to a NoteTree";
	private static final String E_REL_CONTEXT_JAM = 
		"Unable to evaluate pitch: the parent of this NoteLeaf does not have" +
			" an associated instance of JamParameters";
	
	/**
	 * This public identifier is only used when building 
	 * note trees to describe notes that may be paired into 
	 * swing partners, or have trails between them.
	 * 
	 * Can also be used in NoteCollections to identify notes.
	 */
	public int id = -1;
	
	//////	
	/**
	 * Defines whether or not the note is complete, and if not,
	 * which properties are mutable.
	 * 
	 * The mutability is a bitmask that is AND'ed with the 
	 * MUTABLE constants to decide if that property is changeable.
	 * 
	 * If a note is incomplete, it is probably in a heuristic
	 * tree and has values that have not been filled. When a note
	 * is marked incomplete, the pitch (pitchClass and octave)
	 * of the note may be changed.
	 */	
	protected byte mutable = Mutable.ALL;
	
	/**
	 * Returns a bitmask indicating which parts of the note
	 * are mutable.
	 * @return a masked value from (0-3)
	 */
	public byte mutable() { return mutable;	}
	//////
	
	/**
	 * This should be set by the add() methods in NoteTree.
	 */
	public NoteTree _parent = null;
		
	//////
	/**
	 * To honour locking.
	 */
	public void rhythm(Rhythm r)
	{
		if (r != rhythm && _parent != null)
		{
			@SuppressWarnings("unused")
			boolean b = true;
		}
		if ((mutable & Mutable.RHYTHM) != 0)
			super.rhythm(r);
	}
	
	/**
	 * {@inheritDoc}
	 * @see Note
	 */
	public void pitchClass(Pitch p) {
		if ((mutable & Mutable.PITCH) != 0)
			super.pitchClass(p);
	}

	/**
	 * {@inheritDoc}
	 * @see Note
	 */
	public void octave(int octave) {
		if ((mutable & Mutable.PITCH) != 0) 
			super.octave(octave);
	}
	/**
	 * Constructs a NoteLeaf initialized with blank values
	 * (for building trees from files). 
	 * 
	 */
	public NoteLeaf(Rhythm rhythm)
	{
		super();
		this.rhythm = rhythm;				
	}
	
	/**
	 * For initialization - no parent or octave is needed yet (these
	 * are contextually added).
	 * @param pitchClass The pitchClass of this note
	 */
	public NoteLeaf(Pitch pitchClass) {
		super();
		// Set the pitch class.
		pitchClass(pitchClass);
	}
	
	/**
	 * For construction in a set - no parent is needed.
	 * @param pitchClass The pitchClass of this note
	 * @param octave The octave in which this note occurs.
	 */
	public NoteLeaf(Pitch pitchClass, int octave) {
		super(pitchClass,octave);	
	}
	
	/**
	 * For construction at runtime. 
	 * @param pitchClass The pitchClass of this note
	 * @param rhythm The rhythm of this note
	 * @param octave The octave in which this note occurs
	 */
	public NoteLeaf(Pitch pitchClass, Rhythm rhythm, int octave)
	{
		super(pitchClass,octave);		
		this.rhythm = rhythm;		
	}
	
	/**
	 * Shouldn't be used in this implementation - this is for
	 * reconstructing playback melodies only.
	 * 
	 * @param noteValue The floatValue (MIDI number) of this note.
	 */
	public NoteLeaf(float noteValue, int duration)
	{
		super(noteValue);
		this.duration = duration;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SequenceParameters sequenceParameters() {
		if (_parent != null)
			return _parent.sequenceParameters();
		return null;
	}
	/**
	 * Returns an iterator that simply wraps this note to
	 * keep in line with the Iterator pattern.
	 * 
	 * Somewhat expensive, possibly a candidate for optimization.
	 */
	public Iterator<Note> getNotes() {
		return new Itr(this);
	}
	
	/**
	 * NoteLeaves do not support swapping - this should be
	 * done by NoteTree's (i.e. the _parent)
	 * @see NoteTree
	 */
	public boolean swapNotes(NoteSequence swapOut, NoteSequence swapIn) throws NoteAddException
	{
		return false;
		/*
		invalidate();
		if (swapOut != this)
			return false;
		
		if ((swapIn.getClass() == NoteTree.class) || (swapIn.getClass() == NoteLeaf.class))
		{
			/// Not supported
		}
		*/
	}
	
	/**
	 * Transforms this NoteLeaf into a NoteTree.
	 * 
	 * Supports only the addition of NoteLeafs, therefore
	 * NoteSequences of size 1 only.
	 * 
	 * The note currently on this leaf will split and accept
	 * one note in the given NoteSequence as it's child, if the
	 * rhythm for that NoteSequence is valid at this depth. 
	 * 
	 * @return True on success, false on failure.
	 * 
	 */
	public boolean addNotes(NoteSequence notes) 
	{ 
		if ((mutable & Mutable.RHYTHM) == 0)
			return false;
		if (rhythm == Rhythm.HEMIQUAVER)
			return false;
		// TODO Check implementation.		
		try {
			NoteLeaf note = (NoteLeaf) notes.getNotes().next();
			if (note == null)
				throw new SequenceException();
			
			NoteTree composited = new NoteTree(this._parent);
			
			// Forces incoming note to take the rhythm of this note.
			rhythm = rhythm.increase(1);
			note.rhythm = rhythm;
			
			// add it to the composited note tree
			
			composited.addNotes(note);			
			_parent.swapNotes(this,composited);
			// CRUCIAL TO DO THIS AFTER
			// Otherwise composited will be the parent of this before
			// we call swapNotes(), which will have disasterish effects...
			composited.addNotes(this);
			invalidate();
			return true;
		}
		catch (SequenceException e)	{
			return false;
		}
	}
	
	/**
	 * This method simply sets the parent to null,
	 * if the passed NoteSequence == this.
	 * 
	 * NB: the parent should erase this child (leaving a null
	 * gap in the children array). The function call passed to
	 * this node should simply be a signal to get ready and GC 
	 * the note.
	 * 
	 * @param notes The NoteSequence to remove
	 */
	public void removeNotes(NoteSequence notes) {
		if (notes == this)
			_parent = null;	
	}
	
	public Note getNote() {
		return null;
	}
	
	/**
	 * Returns a unique signed integer for this note
	 * (the MIDI note number), or -1 if this note is relative.
	 * 
	 * This method is used for building TerminalSets
	 */
	public int getValue()
	{
		if (isRelative())
			return -1;
		if (!validatePitch())
			return -1;
		
		return (octave() * 12 + MIDI.position(pitchClass));
	}
	
	/**
	 * @return True if this NoteLeaf bears relative pitch.
	 */
	public boolean isRelative()
	{
		if (pitchClass == Pitch.R)
			return true;
		return false;
	}
	
	//////
	private int _swingPartnerId = -1;
	/**
	 * 
	 * @return The ID of the swingPartner if this note leaf has one
	 */
	public int swingPartnerId()	{
		return _swingPartnerId;
	}
	/**
	 * Used when building trees to assign a swing partner to 
	 * this NoteLeaf at a later time.
	 * 
	 * swingPartnerId will be used to assign this.swingPartner
	 * 
	 * @param swingPercent The amount to swing this note
	 * @param swingPartnerId The ID of the paired note
	 */
	public void swingNote(int swingPercent, int swingPartnerId)
	{		
		if (this._swingPartner == null)
		{
			this._swingPercent = swingPercent;
			_swingPartnerId = swingPartnerId;			
		}
	}
	//////
	
	/**
	 * Validates this NoteSequence.
	 * {@inheritDoc}
	 */
	public void validateNotes() throws ValidationException
	{
		if (_parent == null)
		{
			ErrorFeedback.handle("Note has no parent", new ValidationException(this));
			return;
		}
		
		super.validateNotes();
		//lockMask(Mutable.NONE);
	}
	
	/**
	 * Validates that this NoteLeaf is ready to play inside
	 * it's current context (assuming it has a parent), or is
	 * configured to play standalone.
	 */
	protected boolean validatePitch()
	{
		if (validated && !_parent.sequenceParameters().Changed)
			return true;
		
		if (pitchClass != null)
		{
			if (pitchClass == Pitch.R && _parent == null) {
				ErrorFeedback.handle(E_REL_CONTEXT, new PitchException(this));
				return validated = false;
			}
			// evaluatePitch() won't be called if pitchClass != Pitch.R
			if (!evaluatePitch(_parent.sequenceParameters().Jam)) {
				ErrorFeedback.handle(E_REL_CONTEXT_JAM, new PitchException(this));
				return validated = false;
			}
		}
		return super.validatePitch();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected boolean validateRhythm()
	{
		if ((duration > 0) && validated && !_parent.sequenceParameters().Changed)
			return true;
				
		if (rhythm != null) 		
			if (evaluateRhythm(_parent.sequenceParameters()))
				return super.validateRhythm();
		
		ErrorFeedback.handle("",new RhythmException(this));
		return false;
	}
	

	/**
	 * Will perform the appropriate lock operations given the
	 * passed mask.
	 * 
	 * If the operation cannot be performed due to validation
	 * inconsistencies, this method will swallow the exception
	 * and forward it to an active display.
	 * 
	 * @param lockMask A non-negative integer
	 */
	public void lockMask(int lockMask) throws ValidationException {
		lockMask(lockMask,false);
	}
	
	/**
	 * TreeParser needs to be able to force a lock mash when 
	 * building trees from files.
	 * 
	 * @param lockMask A non-negative integer
	 * @param force Whether to force the lock mask application or not
	 * @throws ValidationException
	 */
	public void lockMask(int lockMask, boolean force) throws ValidationException
	{
		if (lockMask < 0)
			return;
	
		if ((lockMask & Mutable.PITCH) == 0) {
			if (force)
				mutable &= ~Mutable.PITCH;
			else
				lockPitch();
		}
		
		if ((lockMask & Mutable.RHYTHM) == 0) {
			if (force)
				mutable &= ~Mutable.RHYTHM;
			else
				lockRhythm();
		}						
	}
	/**
	 * This is arbitrary, no validation takes place here.
	 * 
	 * Subclasses must override these methods to provide 
	 * correct validation.
	 */
	protected void lockRhythm() throws ValidationException { 
		if (validated)
			mutable &= ~Mutable.RHYTHM;
		else
			throw new ValidationException(this);
	}
	
	/**
	 * This is arbitrary, no validation takes place here.
	 * 
	 * Subclasses must override these methods to provide 
	 * correct validation.
	 */	
	protected void lockPitch() throws ValidationException {
		if (validated)
			mutable &= ~Mutable.PITCH;
		else
			throw new ValidationException(this);
	}
	
	/**
	 * Attempts to lock all note properties and make the 
	 * note immutable - if this fails, an exception thrown
	 * explaining which properties are invalid.
	 * 
	 * This is arbitrary, no validation takes place here.
	 * 
	 * Subclasses must override these methods to provide 
	 * correct validation.	 
	 *  
	 * @deprecated
	 * @throws ValidationException if the note cannot be locked
	 */
	protected void lockAll() throws ValidationException { 
		lockRhythm(); 
		lockPitch();
	}
	//////
	
	/**
	 * {@inheritDoc}
	 * NB The proper way to do this would involve a call to super.clone()
	 */
	public NoteLeaf clone() 
	{
		NoteLeaf n = new NoteLeaf(this.pitchClass, octave());
		n.noteValue = noteValue;
		n._evaluatedPitch = _evaluatedPitch;
		n.duration = duration;
		n.validated = validated;
		n._parent = null;
		n.rhythm = rhythm();
		n.mutable = mutable;
		n._swingPartnerId = _swingPartnerId;
		n.id = id;
		n.pitchRelative = pitchRelative;
		n.rest = rest;
		n._swingPercent = _swingPercent;
		return n;
	}
	
	/**
	 * This class provides an Iterator object that will return this 
	 * note once only from it's next() method.
	 */
	private class Itr implements Iterator<Note> {
		boolean hasNext = true;
		Note _n = null;
		public Itr(Note n) {
			_n = n;
		}
		
		public boolean hasNext() {		
			return hasNext;
		}

		public Note next() {
			hasNext = false;
			return _n;
		}
		
		// You cannot modify this iterator.
		public void remove() {
			return;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean contains(NoteSequence n)
	{
		if (n == this)
			return true;
		return false;
	}
	
	/**
	 * Returns a stringified version of this NoteLeaf
	 * {@inheritDoc}
	 */
	public String stringify() {
		return "r("+rhythm.toString()+")s("+_swingPartner.toString()+","+_swingPercent+")p("
		+pitchClass.toString()+","+pitchRelative+")m("+mutable+")re("+(rest?1:0)+")o("+octave()+")";
	}
}
