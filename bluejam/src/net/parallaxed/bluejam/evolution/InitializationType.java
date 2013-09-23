package net.parallaxed.bluejam.evolution;


/**
 * Defines the possible initialization types for the evolution
 * process.
 * 
 * To add a new Initialization algorithm, create the class
 * and define it here, then you can specify the enum value in
 * PopulationParameters.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public enum InitializationType {

	/**
	 * Random initialization
	 * @see InitializeRandom
	 */
	RANDOM(InitializeRandom.class),
	
	/**
	 * Grow initialization
	 * @see InitializeGrow
	 */
	GROW(InitializeGrow.class),
	
	/**
	 * Contoured initialization
	 * @see InitializeHeuristicTree
	 */
	HEURISTIC(InitializeHeuristicTree.class);
	
	private Class<?> _initImpl = null;
	
	InitializationType(Class<?> implementation) {
		_initImpl = implementation;
	}
	
	/**
	 * @return The implementing class.
	 */
	public Class<?> eval() {
		return _initImpl;
	}
}
