package net.parallaxed.bluejam;
/**
 * Utility Class defining which properties of a note can be changed.
 * Assigns bitmasks to deal with permissions under NoteLeaf.
 * 
 * @see NoteLeaf
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public final class Mutable {

	/**
	 * Indicates none of the note properties can be changed.
	 */
	public static final byte NONE = 0;	
	/**
	 * This note can have it's duration increased/decreased 
	 * (in the note tree implementation, this note can change
	 * it's level in the tree).
	 */
	public static final byte RHYTHM = 1;
	/**
	 * The pitchClass/noteValue can change. The note can also
	 * toggleRest()
	 */
	public static final byte PITCH = 2;
	/**
	 * Both pitch and rhythm can change (sum of other flag
	 * values).
	 */
	public static final byte ALL = 3;
}
