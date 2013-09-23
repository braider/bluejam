package net.parallaxed.bluejam;
import java.util.ArrayList;
import java.util.List;
import ec.util.MersenneTwisterFast;

import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.IndividualAddException;
import net.parallaxed.bluejam.exceptions.ParameterException;

/**
 * The population class holds a collection of individuals and
 * evolves them with or without a set of heuristics.
 * 
 * Each evolution produces a new population of individuals,
 * which are the product of mating that takes place between
 * selected individuals in the prior population.
 * 
 * Each new population can be thought of as another "generation".
 * The best individuals (as evaluated by the fitness algorithms)
 * should survive and in the final stages, producing a small set
 * of candidates. One should be selected for output, and the others
 * will be placed into the mating pool to produce the next generation.
 * 
 * In the case of increased evaluation time on the second
 * generation, another of the chosen solos from the first
 * can be chosen. 
 * 
 * A population may be instructed to destroy itself, in which case
 * it returns a reference to the parent population and continues
 * from there (see documentation on the interfaces). 
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class Population 
{
	//////
	private static final String E_SEQUENCE_PARAMETERS_MISSING = "A population cannot be instatiated without SequenceParameters";
	private static final String E_SEQUENCE_PARAMETERS = "There was an error instantiating this population with the given SequenceParameters";
	//////
	
	/**
	 * Limit on the number of individuals in this population.
	 */
	public int memberCount = 0;
	
	/**
	 * Tracks the actual number of members in the population.
	 */
	private int _populationSize = 0;
	public int populationSize() { return _populationSize; }
	/**
	 * An array of fixed length, with a slot for each
	 * individual in the population.
	 */
	public Individual[] populous;
	
	/**
	 * A reference to this population's parameters.
	 */
	protected PopulationParameters params;
	
	/**
	 * Indicates whether or not the parameter configuration has
	 * changed or not since the last "evolution" was executed.
	 * 
	 * Individuals must update references to the new configuration
	 * before executing another evolution cycle.
	 */
	protected boolean _changed = false;
	
	/**
	 * The HeuristicCollection for the population provides a set
	 * of heuristics to be distributed among individuals according 
	 * to the SELECTION_TYPE defined in the given instance.
	 */
	protected HeuristicCollection heuristics = null; 
	
	// The Mersenne twister random number generator for this population
	private MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	
	/**
	 * Constructs a skeleton population given the passed population
	 * as a template. The constructed population will contain no
	 * individuals, but will reflect the parameters of the passed
	 * population.
	 * 
	 * NB: Does not preserve the _changed status of the given population.
	 * @param p The population to take as a template.
	 */
	public Population(Population p)
	{
		this.params = p.params;
		this.memberCount = p.memberCount;
		this.heuristics = p.heuristics;
	}
	/**
	 * Constructs a population.
	 * @param sequenceParameters The sequenceParameters to use in construction.
	 */
	public Population(SequenceParameters sequenceParameters)
	{		
		this(sequenceParameters,50);
	}
	
	/**
	 * Constructs a population.
	 * @param memberCount Maximum number of individuals in this population
	 */
	public Population(SequenceParameters sequenceParameters, int memberCount)
	{		
		if (sequenceParameters == null)
			ErrorFeedback.handle(E_SEQUENCE_PARAMETERS_MISSING, new ParameterException());
		
		this.memberCount = memberCount;
		params = new PopulationParameters();
		try {
			params.setParameter(PopulationParameters.SEQUENCE, sequenceParameters);
		}
		catch (ParameterException e) {
			ErrorFeedback.handle(E_SEQUENCE_PARAMETERS, e);
		}
	}

	/**
	 * Constructs a population.
	 * @param memberCount Maximum number of individuals in this population
	 * @param heuristics Collection of heuristics to use while evolving this population
	 */
	public Population(SequenceParameters sequenceParameters, int memberCount, HeuristicCollection heuristics) {
		this(sequenceParameters,memberCount);
		this.heuristics = heuristics;		
	}	
	
	/**
	 * Adds a heuristic to the population.
	 * @param heuristic The heuristic to be added
	 */
	public void addHeuristic(Heuristic heuristic) {
		heuristics.add(heuristic);
		_changed = true;
	}
	
	/**
	 * Initialises the population by creating the individuals
	 * and setting them with random values.
	 * 
	 * Before calling this method, you must set the parameters
	 * for this population through setParameter(), or the hardcoded
	 * defaults will be used.
	 * 
	 * @see PopulationParameters
	 */
	public void initialize()
	{ 
		populous = new Individual[memberCount];		
		if (heuristics != null)
			for (int i = 0; i < memberCount; i++)		
				populous[i] = new Individual(this,heuristics.selectHeuristic());
		else
			for (int i = 0; i < memberCount; i++)		
				populous[i] = new Individual(this);
				
		for (Individual i : populous)
			i.initialize();
		
		_populationSize = memberCount;
	}
	
	/**
	 * Returns an individual given it's index in the
	 * population.
	 * @param i The index of this individual
	 * @return The individual at index i, or null.
	 */
	public Individual getIndividual(int i) {
		if (i > -1 && i < populous.length)
			return populous[i];
		return null;
	}
	
	public Individual getRandomIndividual() {
		return populous[_mt.nextInt(_populationSize)];
	}
	/**
	 * Adds a list of individuals to the population.
	 * 
	 * Throws IndividualAddException with any failed adds.
	 * @param individuals The individuals to add, in the order to add them.
	 * @throws IndividualAddException
	 */
	public void addIndividuals(List<Individual> individuals) throws IndividualAddException {
		
		if (populous == null)
			populous = new Individual[memberCount];
		ArrayList<Integer> empties = getEmptySlots();
		
		int indCount = individuals.size();
		int i;
		for (i = 0; i < indCount; i++)
			if (empties.size() > 1)
				populous[empties.remove(0)] = individuals.get(i);
			else
				throw new IndividualAddException(individuals.subList(i, individuals.size()));
		
		_changed = true;
		_populationSize += i; 
	}
	
	public void addIndividual(Individual i) throws IndividualAddException {
		
		int index = getEmptySlot();
		if (index > -1)
		{
			populous[index] = i;
			i.population(this);
			_populationSize++;
			_changed = true;
			return;
		}
		throw new IndividualAddException(i);			
	}
	/**
	 * Returns an ArrayList of integers with one entry for
	 * every empty slot in the populous array.
	 * 
	 * @return An ArrayList of [i] gaps in the populous, where populous[i] = null. 
	 */
	public ArrayList<Integer> getEmptySlots() {
		ArrayList<Integer> empties = new ArrayList<Integer>();
		
		for (int i = 0; i< populous.length; i++)
			if (populous[i] == null)
				empties.add(i);
		empties.trimToSize();
		return empties;
	}
	
	private int getEmptySlot()
	{
		if (populous == null)
		{
			populous = new Individual[memberCount];
			return 0;
		}
			
		for (int i = 0; i< populous.length; i++)
			if (populous[i] == null)
				return i;
		return -1;
	}
	
	public Individual getFittestIndividual()
	{
		Individual fittest = populous[0];
		for (int i = 0; i < _populationSize; i++)
		{
			if (populous[i] == null)
				continue;
			if (populous[i].evaluate() > fittest.evaluate())
				fittest = populous[i];
		}
		return fittest;
	}
	
	/**
	 * TODO Some method to copy out references to the mating pool
	 * For use when referencing populations that did well
	 * - reintroduce those individuals that rate highly? 
	 */
	
	/**
	 * Returns a reference to the evolved population
	 * @return A reference to the evolving thread.
	 */
	public Population evolve()
	{
		return null;
	}
	
	/**
	 * Should be refactored to a utility method in EvolveHeuristic.
	 * 
	 * Will take the MatingPool of candidates, and cycle through each
	 * candidate, taking a node from each.
	 * - Extend to first crossover point in the first tree, grab and add it 
	 * - Extend to second crossover point in the second tree, grab it ...
	 * - ... 
	 * 
	 * Alternatively, just take the highest fitness individual in the
	 * pool.
	 * 
	 * Finally, all notes should be changed to RELATIVE pitches, before 
	 * serializing out the file.
	 * 
	 * @return A new Heuristic.
	 */
	public Heuristic buildHeuristic() 
	{
		return null;
	}
	
	/**
	 * Retrieves a reference to the parameters of this population
	 * @see JamParamters
	 * 
	 * @return A ParameterCollection for this population
	 */
	public PopulationParameters getParameters()	{
		return params;
	}
	
	/**
	 * Sets a parameter for this population.
	 * @see PopulationParameters
	 * 
	 * @param name The name of the parameter to set
	 * @param value An object of the correct type for this parameter
	 * @throws ParameterException
	 */
	public void setParameter(String name, Object value) throws ParameterException {
		params.setParameter(name, value);
	}
}	
