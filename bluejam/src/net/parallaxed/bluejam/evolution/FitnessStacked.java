package net.parallaxed.bluejam.evolution;

import java.util.ArrayList;

import net.parallaxed.bluejam.Individual;

/**
 * Tracks which individuals have already been measured
 * by the function, and applies different fitness measures
 * each time.
 * 
 * This class compounds various fitness methods.
 * 
 * INTERVALS
 * ->
 * HEURISTIC-DIFFERENCE
 * ->
 * CONTOUR
 * 
 * One problem with this might be that individuals
 * with a propensity for only one thing will be discarded,
 * and never make it to the mating pool, where they 
 * may have very good genes for accomplishing a
 * particular task.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class FitnessStacked implements IndividualEvaluator {
	
	private static FitnessStacked _instance = null;
	private volatile ArrayList<Individual> _measured = new ArrayList<Individual>();
	private FitnessInterval _fi = FitnessInterval.getInstance();
	private FitnessDistance _fd = FitnessDistance.getInstance();
	private FitnessContour _fc = FitnessContour.getInstance();	
	private FitnessStacked() {	}
	
	/**
	 * @return An instance of FitnessStacked.
	 */
	public static FitnessStacked getInstance()
	{
		if (_instance == null)
			_instance = new FitnessStacked();
		return _instance;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public double evaluate(Individual individual)	{
		if (!_measured.contains(individual))
		{
			_measured.add(individual);
			// This call is crucial - invalidate it so we can
			// get called again later.
			individual.invalidate();
			return _fi.evaluate(individual);
		}		
		// We've been called again, the individual should already think
		// it's evaluated - so this *won't* recurse.
		double _currentFitness = individual.evaluate();
		
		double _distanceFitness = -1;
		if (individual.getHeuristic() != null)
			_fd.evaluate(individual);
		
		double _contourFitness = _fc.evaluate(individual);
		_measured.remove(individual);
		// Return a combined weighted fitness for the individual.
		if (_distanceFitness != -1) 
			return (_currentFitness * 1/2) + (_distanceFitness * (1/4)) + (_contourFitness * (1/4));
		else
			return (_currentFitness * 2/3) + (_contourFitness * 1/3);
	}
}
