package net.parallaxed.bluejam.evolution;

import ec.util.MersenneTwisterFast;
import net.parallaxed.bluejam.Individual;

/**
 * Picks a random fitness evaluation method and returns
 * the result.
 * 
 * This is not used in the default implementation of BlueJam.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class FitnessRandom implements IndividualEvaluator {
	private static FitnessRandom _instance = null;
	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	private static final IndividualEvaluator[] _evaluators =  
	{ 	FitnessContour.getInstance(),
		FitnessDistance.getInstance(),
		FitnessInterval.getInstance()
	};
	
	
	private FitnessRandom() {	}
	
	public static FitnessRandom getInstance()
	{
		if (_instance == null)
			_instance = new FitnessRandom();
		return _instance;
	}
	
	public double evaluate(Individual individual)	{
		return _evaluators[_mt.nextInt(_evaluators.length)].evaluate(individual);
	}
}
