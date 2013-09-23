package net.parallaxed.bluejam.evolution;

import net.parallaxed.bluejam.PopulationParameters;

/**
 * An enum containing the possible selection algorithms
 * that can be used. These can be set on a per-population 
 * basis using PopulationParameters
 * @see PopulationParameters
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public enum SelectionType {
	/**
	 * A proportional selection algorithm
	 */
	PROPORTIONAL(SelectProportional.class),
	/**
	 * A Tournament-based Selection selection Algorithm
	 */
	TOURNAMENT(SelectTournament.class);
	
	private Class<?> _impl = null;
	
	/**
	 * Registers the given enumeration with a class file.
	 * @param implementation The implementing class
	 */
	SelectionType(Class<?> implementation) {
		_impl = implementation;
	}
	
	/**
	 * @return The implementing class.
	 */
	public Class<?> eval() {
		return _impl;
	}
}
