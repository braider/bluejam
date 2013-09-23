package net.parallaxed.bluejam.evolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import ec.util.MersenneTwisterFast;
import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.Population;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.IndividualAddException;

/**
 * Performs a variant of tournament selection. Unlike the classic 
 * tournament, this function is optimised to use FitnessStacked, 
 * so the tournament takes place in two rounds. 
 * 
 * The first round is negative selection, where the weakest are
 * assigned proportionally greater probabilities, and a 
 * roulette-wheel selection is made to knock out competitors.
 * 
 * The second round is positive selection, where the individuals
 * are evaluated again, and the largest score wins.
 * 
 * Two are taken from each Tournament until the number of parents
 * required for breeding are selected.
 * 
 * This will still work on fitness methods other than FitnessStacked.
 * 
 * @see FitnessStacked
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class SelectTournament implements IndividualSelector {
	
	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	private static SelectTournament _instance = null;
	

	private SelectTournament() {} 
	
	/**
	 * @return An instance of SelectTournament
	 */
	public static final SelectTournament getInstance() {
		if (_instance == null)
			_instance = new SelectTournament();
		return _instance;
	}
	
	/**
	 * {@inheritDoc}}
	 * 
	 * Satisfies the implementation of IndividualSelector.
	 * 
	 * See the class documentation for more coverage on how we
	 * execute the selection.
	 */
	public Population select(Population pop, int numberOfIndividuals) {
		int tournamentSize = 15;
		// TODO Throw exception?
		if (tournamentSize * numberOfIndividuals > pop.memberCount)
			;
		ArrayList<Individual> winners = new ArrayList<Individual>(numberOfIndividuals);
		PopulationParameters popParams = pop.getParameters();
		Integer _pressure = popParams.getSelectionPressure();
		
		if (_pressure != null && _pressure > 1 && _pressure < pop.memberCount)
			tournamentSize = _pressure;
		
		int firstRoundKnockouts = tournamentSize/2;
		while (winners.size() < numberOfIndividuals)
		{
			// Sets up the tournament by selecting competitors.
			ArrayList<Individual> competitors = setUp(pop, winners, tournamentSize);
			// Knock out competitors using negative, fitness-proportional selection. 
			round1(competitors, firstRoundKnockouts);
			
			// Standard TS : May the best fitness win...
			winners.addAll(round2(competitors));
		}
		
		Population skeleton = new Population(pop);
		
		try {
			int i = 0;
			while (i < numberOfIndividuals)
				skeleton.addIndividual(winners.get(i++));
		}
		catch (IndividualAddException e) {
			// do nothing.
			ErrorFeedback.handle(e.getMessage(),e);
		}
		return skeleton;
		
	}
	
	/**
	 * Tournament set up. We add tournamentSize individuals from
	 * population into a List of competitors.
	 * @param population The population conducting the tournament.
	 * @param winners So we can check the same individuals don't compete again.
	 * @param tournamentSize The number of competitors in the tournament.
	 * @return A list of competitors for this tournament.
	 */
	private ArrayList<Individual> setUp(Population population, ArrayList<Individual> winners, int tournamentSize)
	{
		ArrayList<Individual> competitors =  new ArrayList<Individual>();		
		// Get the competitors.
		int i = tournamentSize;
		while (i > 0)
		{
			Individual randomInd = population.getIndividual(_mt.nextInt(population.memberCount));
			// Is this contender not in the tournament?
			if (!competitors.contains(randomInd) && !winners.contains(randomInd))
				i--;
			else
				// Can't the same tournament twice! Find another.
				continue;
			competitors.add(randomInd);
		}
		return competitors;
	}
	
	/**
	 * Round 1 performs a negative selection favouring the worst
	 * individuals. A total of <i>firstRoundKnockouts</i> individuals are
	 * selected and the passed competitors list is whittled down.
	 * 
	 * @param competitors The competitors in the tournament
	 * @param firstRoundKnockouts The number of competitors to knock out in the first round.
	 */
	protected void round1(ArrayList<Individual> competitors, int firstRoundKnockouts) {
		// Proportional knockout - Round 1.
		int i = 0;
		int competitorCount = competitors.size();
		double[] scores = new double[competitorCount];
		double scoreTotal = 0;
		// Track the number of individuals with score 0.
		int zeroScores = 0;
		for (i = 0; i < competitorCount; i++) {
			scores[i] = competitors.get(i).evaluate();
			scoreTotal += scores[i];
			if (scores[i] == 0)
				zeroScores++;
		}
		
		double[] probabilities = new double[competitorCount];
		double probabilityTotal = 0;
		for (i = 0; i < scores.length; i++)	{
			// Invert the meaning of the score.
			probabilities[i] = scoreTotal - scores[i];
			probabilityTotal += probabilities[i];			
		}
		
		// Sum up the probability matrix
		// This does the ordering automatically.
		for (i = 0; i < probabilities.length; i++)
		{
			probabilities[i] = probabilities[i] / probabilityTotal;
			if (i > 0)
				probabilities[i] = probabilities[i] + probabilities[i-1];
		}
		
		// TODO Worst case - all have zero

		ArrayList<Individual> KOs = new ArrayList<Individual>(firstRoundKnockouts);
		
		if (zeroScores < firstRoundKnockouts)
		{
			while (KOs.size() < firstRoundKnockouts)
			{	
				// +1 allows us to capture 0 scores.
				double rand = _mt.nextDouble();				
				i = 0;
				// TODO FIX: This is not stable where we have identical scores. 
				// Find where our randomly generated number belongs in the ordered list (selection)
				for (double probability : probabilities)				
					if (rand > probability)
						i++;
					else
						break;
				// Get knocked out individual.
				Individual KO = competitors.get(i);
				// We have our knockout - make sure it's not already in there.
				if (!KOs.contains(KO))
					KOs.add(KO);				
			}
		}
		else
		{
			i = 0;
			for (double score: scores)
			{
				if (KOs.size() < firstRoundKnockouts)
				{
					if (score == 0)
						KOs.add(competitors.get(i));
					continue;
				}
				// We've filled the knockouts.
				break;
			}
		}
		competitors.removeAll(KOs);
	}
	
	/**
	 * Further compares the fitness of the individuals.
	 * 
	 * This method is designed for FitnessStacked, but can still
	 * work on other fitness Functions - just becomes a 
	 * regular TournamentSelection from here on in (fittest one wins).
	 * 
	 * @see FitnessStacked
	 * @param competitors The competitors.
	 * @return The winners.
	 */
	protected ArrayList<Individual> round2(ArrayList<Individual> competitors)
	{
		ArrayList<Individual> winners = new ArrayList<Individual>(2);
		int competitorCount = competitors.size();
		double[] scores = new double[competitorCount];
		HashMap<Double, Individual> scoreMap = new HashMap<Double, Individual>();
		int i = 0;
		
		for (i = 0; i < competitorCount; i++ )
		{
			Individual competitor =  competitors.get(i);
			scores[i] = competitor.evaluate();
			scoreMap.put(scores[i],competitor);
		}
		
		Arrays.sort(scores);
		i = scores.length;
		while (i-- > scores.length - 2) {
			winners.add(scoreMap.get(scores[i]));			
		}
		competitors.removeAll(winners);
		return winners;
		
	}

}
