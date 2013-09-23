package net.parallaxed.bluejam.evolution;

/**
 * When initializing the population, the set of loaded heuristics
 * can be assigned randomly or evenly. HeuristicSelectionType.EVEN
 * will iterate through the list, whereas HeuristicSelectionType.RANDOM
 * will pick any from the list.
 *  
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public enum HeuristicSelectionType {
	EVEN,RANDOM;
}
