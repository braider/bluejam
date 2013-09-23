package net.parallaxed.bluejam.evolution;

import java.util.ArrayList;

import ec.util.MersenneTwisterFast;
import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.Population;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.IndividualAddException;

/**
 * Implements a proportional (Roulette Wheel) selection algorithm
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class SelectProportional implements IndividualSelector {
	
	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	
	public Population select(Population pop, int numberOfIndividuals) {
		int size = pop.populationSize();
		double[] probabilities = new double[size];
		double[] fitness = new double[size];
		double totalFitness = 0;
		for (Individual i : pop.populous) {
			double indFitness = i.evaluate();			
			totalFitness += indFitness;			
		}
		for (int i = 0; i < size; i++)		
			probabilities[i] = fitness[i] / totalFitness;
		
		Population skeleton = new Population(pop);
		ArrayList<Integer> winners = new ArrayList<Integer>();
		outer:
		while (numberOfIndividuals > 0)
		{
			double random = _mt.nextDouble();
		
			for (int i = 0; i < size; i++)
				if (random > probabilities[i])
					continue;
				else
					try { 
						if (!winners.contains(i))
						{
							skeleton.addIndividual(pop.getIndividual(i));
							winners.add(i);
							numberOfIndividuals--; 
						}
					}
					catch (IndividualAddException e) { ErrorFeedback.handle(e); continue outer; }			
		}
		return skeleton;
	}
	

}
