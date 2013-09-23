package net.parallaxed.bluejam.evolution;

import net.parallaxed.bluejam.Population;

/**
 * Classes implementing this interface must fill a population
 * to it's maximum capacity through the use of crossover
 * and mutation to create new individuals.
 * 
 * The initial population passed to the function represents 
 * the mating pool, so the algorithm should extract initial
 * individuals from the referenced object.  If no population 
 * is passed, this throws an exception. Nothing is returned 
 * by implementing classes, the population referenced is 
 * filled with children in-situ.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface Breeder {
	/**
	 * Uses a skeletal population containing a collection
	 * of parents and fills up the population to the full
	 * memberCount quota using methods of crossover and mutation.
	 * 
	 * Nothing should be returned by this method.
	 * 
	 * @param population The population to breed.
	 */
	public void breed(Population population);
}
