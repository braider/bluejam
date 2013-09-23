package net.parallaxed.bluejam;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.parallaxed.bluejam.evolution.NoteSequenceInitializer;
import net.parallaxed.bluejam.evolution.IndividualEvaluator;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.ParameterException;

/**
 * This class defines an individual. Also could be termed a
 * chromosome in GP, but our individuals represent instances of 
 * NoteTrees based around a given Heuristic present in the
 * population.
 * 
 * An individual implements Function in the sense that it has
 * access to the "play()" function in the NoteSequence
 * represented by the individual. Theoretically, this permits
 * individuals and their sequences to be chained together.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class Individual implements Function
{
	private boolean evaluated = false;
	private double _fitness = 0;
	private Heuristic _heuristic = null;
	/**
	 * Returns a reference to the Heuristic used to create
	 * the individual, or null if no Heuristic was used.
	 * @return This individual's Heuristic, or null.
	 */
	public Heuristic getHeuristic() {
		return _heuristic;
	}
	/**
	 * Reassigns this individual's population membership.
	 * @param population The population to place this individual in.
	 */
	public void population(Population population) {
		_population = population;
		_popParams = population.getParameters();		
	}
	private Population _population = null;
	
	private NoteSequence _notes = null;
	private PopulationParameters _popParams = null;	
	private IndividualEvaluator _sequenceEvaluator = null;
	private static final String E_PARAMS_GENOTYPE = "Unable to instantiate default genotype - did you define one in PopulationParameters?";
	private static final String E_PARAMS_INIT = "Unable to instantiate default initilializer - did you define one in PopulationParameters?";
	
	/**
	 * For creating an individual outside a population in crossover
	 * @param notes The NoteSequence to initialize the Individual with
	 * @param heuristic The heuristic used to build this individual.
	 */
	public Individual(NoteSequence notes, Heuristic heuristic) {
		_notes = notes;
		_heuristic = heuristic;		
	}
	
	/**
	 * For creating an individual outside a population (mostly
	 * for testing)
	 * @param notes The NoteSequence to initialize the Individual with
	 * @param popParams The parameters the individual should take.
	 */
	public Individual(NoteSequence notes, PopulationParameters popParams) {
		_notes = notes;
		_popParams = popParams;
	}
	
	/**
	 * For creating an individual outside a population (mostly
	 * for testing) with a heuristic.
	 * @param popParams The parameters the individual should take.
	 * @param heuristic The heuristic to use for initialize()
	 */
	public Individual(PopulationParameters popParams, Heuristic heuristic) {
		_popParams = popParams;
		_heuristic = heuristic;		
	}
	
	/**
	 * For creating an individual outside a population (mostly
	 * for testing) with a heuristic.
	 * @param notes The NoteSequence to initialize the Individual with
	 * @param popParams The parameters the individual should take.
	 * @param heuristic The heuristic to use for initialize()
	 */
	public Individual(NoteSequence notes, PopulationParameters popParams, Heuristic heuristic) {
		_notes = notes;
		_popParams = popParams;
		_heuristic = heuristic;		
	}
	
	/**
	 * Creates an individual with no given heuristic.
	 * 
	 * @param population The population that this individual belongs to.
	 */
	public Individual(Population population) 
	{ 
		this(population,null);
	}
	
	/**
	 * Creates this individual with a given Heuristic.
	 * 
	 * The heuristic passed will be used to create the base 
	 * note sequence for this individual.
	 * 
	 * @param population The population that this individual belongs to.
	 * @param heuristic A heuristic for evolving this individual's NoteSequence.
	 */
	public Individual(Population population, Heuristic heuristic) {
		_population = population;
		_popParams = _population.params;
		_heuristic = heuristic;	
	}
	
	/**
	 * Initialises the individual by parsing the heuristic into a 
	 * note tree. For optimisation reasons this might not always
	 * occur at instantiation.
	 */
	public void initialize() {
		try {
			if (_heuristic != null)
			{
				try {
					_notes = _heuristic.clone();
					((Heuristic)_notes).setSequenceParameters(_popParams.getSequenceParameters());
				}
				catch (CloneNotSupportedException e) { throw new RuntimeException("Unable to build individual from heuristic, must support interface Cloneable"); }
							
			}
			else {
				try {
	
					Class<?> genotype = _popParams.getGenotype().eval();					
					Constructor<?> _cons = genotype.getConstructor(new Class[] { SequenceParameters.class });
					_notes = (NoteSequence) _cons.newInstance(new Object[] { _popParams.getSequenceParameters() });
				}
				catch (NullPointerException e) { ErrorFeedback.handle(E_PARAMS_GENOTYPE, e); }
			}
			
			Class<?> initializer = _popParams.getInitializationType().eval();
			Method _cons = initializer.getMethod("getInstance", new Class[] {});
			// static, we don't care about args.
			NoteSequenceInitializer n = (NoteSequenceInitializer) _cons.invoke(null, new Object[] {});
			if (n == null)
				throw new NullPointerException(E_PARAMS_INIT);
			try {
				n.initialize(_notes, _popParams);
			}
			catch (Exception e) { ErrorFeedback.handle(e.getMessage(), e); }
		}
		catch (NullPointerException e) { ErrorFeedback.handle(E_PARAMS_INIT, e); }
		catch (Exception e) { ErrorFeedback.handle(e.getMessage(), e); }
	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public NoteSequence getNoteSequence() {
		return _notes;
	}
	
	/**
	 * Gives the fitness of this individual or throws an exception
	 * if the individual is not evaluated yet.
	 * @return The fitness of the individual
	 */
	public double evaluate()
	{
		try {			
			if (evaluated)
				return _fitness;
			else if (_sequenceEvaluator == null) {
				// We set this now so the fitness function can unset/ignore it.
				
				Class<?> _se = _popParams.getFitnessType().eval();
				Method _cons = _se.getMethod("getInstance", new Class[] {});
				// static, we don't care about args.
				_sequenceEvaluator = (IndividualEvaluator) _cons.invoke(null, new Object[] {});
			}
			evaluated = true;
			return (_fitness = _sequenceEvaluator.evaluate(this));
		}
		catch (Exception e) {  
			ErrorFeedback.handle("Error evaluating "+toString(), e);
			evaluated = false;
		}	
		return 0;
	}
	
	/**
	 * Sets the internal state of this individual such that the next
	 * call to evaluate() will re-evaluate the fitness of the
	 * Individual.
	 */
	public void invalidate()
	{
		evaluated = false;
	}
	
	/**
	 * Will create and IndividualParameters instance for this object
	 * if it doesn't already have one, and set the given 
	 * individual parameter.
	 * 
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * @see IndividualParameters
	 */
	public void setParameter(String name, Object value) throws ParameterException {
		_popParams = new IndividualParameters(_popParams);
		_popParams.setParameter(name, value);
	}
	
	/**
	 * @return This population's parameter object.
	 */
	public PopulationParameters getParameters()
	{
		return _popParams;
	}
}
