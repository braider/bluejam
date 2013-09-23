package net.parallaxed.bluejam.evolution;

import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.PopulationParameters;

/**
 * Initializers are singletons that can accept a reference
 * to a NoteSequence and fill it with notes using the algorithm
 * they define.
 * 
 * Use of this interface separates knowledge of the algorithm 
 * from the rest of the evolution.
 * 
 * ***
 * NB Since Initializers are singletons, they should all provide a 
 * getInstance() method, which the framework will call to get a
 * reference before it calls initialize()
 * ***
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface NoteSequenceInitializer {
	

	/**
	 * This method should fill up the note sequence, adding
	 * notes as appropriate. The algorithm implementation
	 * should be specific to the Genotype structure, so the
	 * implementing class should check that params
	 * supplies the right value for Genotype.
	 * 
	 * 
	 * 
	 * Note that this method does not return anything, the
	 * NoteSequence will be altered appropriately after method
	 * execution.
	 * 
	 * @see Genotype
	 * @see NoteSequence
	 * @see Note
	 * 
	 * @param notes The NoteSequence to initialize.
	 * @param params The parameters specified for the individual that calls this method.
	 */
	public void initialize(NoteSequence notes, PopulationParameters params);
}
