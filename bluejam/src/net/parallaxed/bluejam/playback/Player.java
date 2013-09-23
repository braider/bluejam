package net.parallaxed.bluejam.playback;

import net.parallaxed.bluejam.Evolve;
import net.parallaxed.bluejam.NoteSequence;

/**
 * This interface is implemented by classes that produce output
 * (default: Evolve). These classes should run in their own threads
 * and output NoteSequences to subscribed listeners.
 * 
 * Ideally, they should change their behaviour based on an
 * established protocol for exchanging feedback between it's
 * listeners.
 * 
 * @see Evolve
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface Player {
	/**
	 * This method can be called by listeners to provide an integer value
	 * (most simply, -1, 0 or +1), for the purpose of informing the player
	 * what that listener thought of the NoteSequence supplied. 
	 * @param feedback An integer value denoting the perceived quality of the NoteSeqeunce
	 * @param notes The NoteSequence we're feeding back on.
	 */
	public void feedback(int feedback, NoteSequence notes);
	
	/**
	 * A listener (class implementing the Listener interface), must call this
	 * method to register with the player that it wishes to receive calls to
	 * it's listen() method, with various NoteSequences.
	 * @param listener A reference to the registering listener (normally *this*).
	 */
	public void addListener(Listener listener);
}
