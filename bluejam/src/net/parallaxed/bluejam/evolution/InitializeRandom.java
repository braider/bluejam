package net.parallaxed.bluejam.evolution;

import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.PopulationParameters;

/**
 * Implements a random initialization. This initially fills the
 * tree out to crotchet depth, then randomly selects 
 * 
 * This is not a true FULL method, it is adapted to function 
 * better given the musical domain.
 * 
 * In this case we fill out to a LIMIT max-depth (normally 
 * Rhythm.CROTCHET), and grow the rest.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class InitializeRandom implements NoteSequenceInitializer {
	
	private static InitializeRandom _instance = null; 
	
	private InitializeRandom() {
		
	}
	
	public static InitializeRandom getInstance() {
		if (_instance == null)
			_instance = new InitializeRandom();
		return _instance;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void initialize(NoteSequence notes, PopulationParameters params)
	{
		
	}
}
