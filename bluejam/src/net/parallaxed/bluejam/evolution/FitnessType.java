package net.parallaxed.bluejam.evolution;

import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.PopulationParameters;

/**
 * Defines which types of fitness are available to
 * the system.
 * 
 * This parameter is also specified by default in
 * PopulationParameters. Different Individuals can use
 * distinct fitness measures if they wish.
 * 
 * @see Individual
 * @see PopulationParameters
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public enum FitnessType {
	
	/**
	 * @see FitnessStacked
	 */
	STACKED(FitnessStacked.class),
	/**
	 * @see FitnessRandom
	 */
	RANDOM(FitnessRandom.class),
	/**
	 * @see FitnessInterval
	 */
	INTERVAL(FitnessInterval.class),
	/**
	 * @see FitnessContour
	 */
	CONTOUR(FitnessContour.class),
	/**
	 * @see FitnessDistance
	 */
	DISTANCE(FitnessDistance.class);
	
	private Class<?> _impl;
	
	FitnessType(Class<?> implementation) {
		_impl = implementation;
	}
	
	/**
	 * @return The implementing class.
	 */
	public Class<?> eval() {
		return _impl;
	}
	
}
