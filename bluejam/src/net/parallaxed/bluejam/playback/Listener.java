package net.parallaxed.bluejam.playback;

import net.parallaxed.bluejam.Evolve;
import net.parallaxed.bluejam.NoteSequence;

/**
 * This interface can be implemented by classes that
 * wish to receive notifications from evolving populations.
 * 
 * @see Evolve
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface Listener {
	/**
	 * Called by the Evolving population when a candidate
	 * NoteSequence is ready to be played.
	 * 
	 * A null noteSequence should be passed when no more
	 * generations will be calculated.
	 * @param n The NoteSequence to be played.
	 */
	public void listen(NoteSequence n);
}
