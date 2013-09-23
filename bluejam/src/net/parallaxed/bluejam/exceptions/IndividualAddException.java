package net.parallaxed.bluejam.exceptions;
import java.util.List;
import java.util.ArrayList;

import net.parallaxed.bluejam.Individual;

/**
 * Thrown when an individual cannot be added to a population
 * (Normally because a population is full).
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class IndividualAddException extends PopulationException {
	public static final long serialVersionUID = 1L;
	private ArrayList<Individual> _failed = new ArrayList<Individual>();
	/**
	 * @return An ArrayList of the Individuals that failed the add
	 */
	public ArrayList<Individual> getFailed() { return _failed; }
	
	/**
	 * Creates an IndividualAddException and associates it with a single
	 * individual.
	 * @param i
	 */
	public IndividualAddException(Individual i)
	{
		super("Failed to add individual "+i.toString()+" to population");
		_failed.add(i);
	}
	
	/**
	 * Allows this exception to be constructed with a list
	 * of failed individuals.
	 * 
	 * This is a list rather than a collection to preserve the order
	 * of failure.
	 * 
	 * @param individuals The individuals that failed to be added.
	 */
	public IndividualAddException(List<Individual> individuals)
	{
		super("Failed to add some individuals to the population");
		_failed.addAll(individuals);		
	}
}
