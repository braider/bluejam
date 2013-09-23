package net.parallaxed.bluejam.evolution;

import net.parallaxed.bluejam.Population;

/**
 * Implementing classes should design selection algorithms
 * that can accept a population as input, and produce another
 * (skeleton) population as output. These can be of an 
 * arbitrary size, but a method to alter the size of the
 * skeletal population should be provided. 
 * 
 * Various breeding methods can be called over this subpopulation
 * to flesh it out, before the selection procedure is
 * called again to produce another generation of individuals.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface IndividualSelector {
	/**
	 * This function performs the selection, returning a
	 * smaller population as set by newPopulationCount(int).
	 * 
	 * @param pop The population to perform selection on.
	 * @param numberOfIndividuals The number of individuals to select from the population.
	 * @return A new population, ready to go into breeding.
	 */
	public Population select(Population pop, int numberOfIndividuals);

}
