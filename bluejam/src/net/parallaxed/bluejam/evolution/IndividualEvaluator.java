package net.parallaxed.bluejam.evolution;

import net.parallaxed.bluejam.Individual;

/**
 * Describes what arguments an implementing class
 * (fitness function) must take. Like the NoteSequenceInitializer interface, the evaluators
 * assume a singleton implementation, whereby the NoteSequences
 * to be evaluated are passed in through the evaluate() function.
 * Evaluators are singletons and should not store any volatile 
 * state in the class for ThreadSafety. NoteSequenceEvaluators may be optimized for different 
 * dataTypes, but the default implementation of evaluate() should
 * return <i>some</i> value for fitness, no matter what the 
 * NoteSequence type is. In the default implementation, all fitness values are
 * normalised (in the range0-1).
 * 
 * The implementing should throw an exception if required
 * knowledge of the dataType is not found, or output a warning
 * if knowledge was expected but not present.The Selector function 
 * however should be strictly compatible with the type of value 
 * returned.  
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface IndividualEvaluator {
	
	/**
	 * Evaluates the given NoteSequence in an individual, providing a double value
	 * for the fitness of that sequence.
	 * @see Individual
	 * @param individual The Individual to Evaluate
	 * @return A value for the fitness of the NoteSequence
	 */
	public double evaluate(Individual individual);
}
