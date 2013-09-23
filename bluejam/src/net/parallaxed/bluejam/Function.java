package net.parallaxed.bluejam;

/**
 * This interface defines the behaviours of a GP function. 
 * 
 * Since this interface is only to facilitate a gateway to the
 * NoteSequence provided by any implementing classes, there is
 * only one function - getNoteSequence().
 * 
 * There may be multiple types of function (such as reverse,
 * augment, swing etc), but the default implementation found
 * here in the current classes is a vanilla "play()".
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface Function 
{
	/**
	 * Since the default function is to "play" the contents of 
	 * the tree, this function should return a reference to
	 * a playable note sequence.
	 * 
	 * Provides a reference to the note sequence contained in
	 * the implementing class.
	 * 
	 * 
	 * @return A reference to the NoteSequence
	 */
	public NoteSequence getNoteSequence();
}
