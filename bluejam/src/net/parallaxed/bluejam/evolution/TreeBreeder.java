package net.parallaxed.bluejam.evolution;
import ec.util.MersenneTwisterFast;
import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.Mutable;
import net.parallaxed.bluejam.NoteLeaf;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.Population;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.Rhythm;

import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.exceptions.BreedException;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.IndividualAddException;
import net.parallaxed.bluejam.exceptions.SequenceException;
import net.parallaxed.bluejam.playback.MIDI;

/**
 * TreeBreeder is the default implementation of the Breeder
 * interface for recombining and mutating NoteTrees, given 
 * an initial skeletal population.
 * 
 * Unlike other algorithms, this is not a singleton, since 
 * there may be more than one breeder available at any
 * one time.
 * 
 * TODO Check working implementation of other NoteSequence representations.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class TreeBreeder implements Breeder {

	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	//////
	/**
	 * @return The maximum number of times we run breeding functions over any single or pair of individuals. 
	 */
	public int maxBreedCycles() { return _maxBreedCycles; }
	private int _maxBreedCycles = 5;
	private Population p = null;
	/**
	 * Sets the maximum number of times we run a breeding 
	 * process for each Individual pair. Default = 5.
	 * 
	 * @param cycles The number of times to runs
	 */
	public void maxBreedCycles(int cycles) {
		// limit to sensible values
		if (cycles > 0 && cycles < 50)
			_maxBreedCycles = cycles;
	}
	
	/**
	 * @return  A value between 0 and 1 for the probability of doing crossover on the individual.
	 */
	public double crossoverProbability() { return _crossoverProbability; }
	private double _crossoverProbability = 0.9;
	/**
	 * NOTE: Setting this variable also sets the probability for mutation
	 * in this breeder.
	 * 
	 * Sets the probability of crossover (between 0 an 1). The inverse 
	 * of this value sets the probability for mutation. Setting this to a
	 * value less than 0.5 is not recommended.
	 * 
	 * @param probability A value between 0-1 (inclusive).
	 */
	public void crossoverProbability(double probability) {
		if (probability <= 1 && probability >= 0)
			_crossoverProbability = probability;
	}
	/**
	 * Instantiates a TreeBuilder trivially.
	 */
	public TreeBreeder() {}
	/**
	 * Instantiates a TreeBreeder with the passed parameters.
	 * @param maxBreedCycles 
	 * @param crossoverProbability
	 */
	public TreeBreeder(int maxBreedCycles, double crossoverProbability) 
	{
		_maxBreedCycles = maxBreedCycles;
		_crossoverProbability = crossoverProbability;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void breed(Population population)
	{
		p = population; 
		/*
		 * So, population gets passed in.
		 * 
		 * From there, we select two random/arbitrary individuals, 
		 * get their crossover points.
		 * 
		 * Probability of Mutation/Crossover kicks in.
		 * We decide randomly how many cycles to do on this individual (1 < x < MAX)
		 * 
		 * Pick two random crossover points at the same level
		 * If none exist at the same level (or in a compatible level), we
		 * abandon and try again.
		 *  
		 * Add the new individual to the population
		 * 
		 * Keep going UNTIL we max out the population.
		 */
		int c = 0;
		Individual i1 = null;
		// Try a few times to get it right
		while (i1 == null && c++ < 5)
			i1 = population.getRandomIndividual();
		
		if (i1 == null)
		{
			ErrorFeedback.handle("ERROR: Population is returning null members - discontinuing", new BreedException(population));
			return;
		}
		
		// Make sure we get a different reference.
		Individual i2 = i1;
		
		while (i2 == i1)
			i2 = population.getRandomIndividual();
		
		// How many cycles will we do?
		int numCycles = _mt.nextInt(_maxBreedCycles);
		if (numCycles == 0)
			numCycles++;
		
		while (numCycles-- > 0)
		{
			// Are we going to crossover?			
			if (_mt.nextDouble() < _crossoverProbability)
				crossover(i1,i2);
			else
				// Select one individual randomly for mutation.
				mutate((_mt.nextBoolean() ? i1 : i2));
		}
		
		// Repeat until we're full.
		if (population.populationSize() < population.memberCount)
			 breed(population);
			
	}
	
	/**
	 * Performs crossover on two passed NoteTrees in-situ. 
	 * 
	 * This methods should receive two note trees that are
	 * to be crossed over. Nothing is returned, the trees are
	 * altered in-situ and should remain that way. If breeding
	 * NoteTrees through this method, always pass the clone().
	 * 
	 * @param nt1 A note tree to recombine
	 * @param nt2 The note tree to combine with.
	 */
	public void recombine(NoteTree nt1, NoteTree nt2) {
		try { _crossover(nt1, nt2); }
		catch (SequenceException e) { ErrorFeedback.handle(e); }
	}
	
	/**
	 * Performs a greedy search for all crossover points on a 
	 * note tree.
	 */
	private NoteSequence greedySelect(NoteTree selectedPoint) {
		NoteSequence[] subTrees = selectedPoint.getSubTrees();
		if (subTrees.length < 1)
			return selectedPoint;
		return subTrees[_mt.nextInt(subTrees.length)]; 
	}
		
	private NoteTree[] _crossover(NoteTree nt1, NoteTree nt2) throws SequenceException
	{
				
		// getCrossoverReferences always returns the minimal set of
		// points, we can get greedy later if this is too small.
		NoteSequence[] pointSet1 = nt1.getCrossoverReferences();
		NoteSequence[] pointSet2 = nt2.getCrossoverReferences();
		
		int maxAttempts = 3;
		
		for (int attempts = 0; attempts < maxAttempts; attempts++ )
		{
			NoteSequence selectedPoint = pointSet1[_mt.nextInt(pointSet1.length)];

			Rhythm acceptedRhythm = null;
			if (selectedPoint.getClass() == NoteTree.class)
				acceptedRhythm  = ((NoteTree)selectedPoint).acceptedRhythm();
			// TODO FIX THIS FOR NOTE LEAVES
			else if (selectedPoint.getClass() == NoteLeaf.class)
			;	//acceptedRhythm = ((NoteLeaf) selectedPoint)
			
			if (acceptedRhythm == null)
				continue;
			
			// Check the node we picked isn't too far up the tree
			int selectedPointRhythm = acceptedRhythm.eval();
			if (selectedPointRhythm <= 2)					
				// getGreedy
				selectedPoint = greedySelect((NoteTree) selectedPoint);
			
			NoteTree matchingPoint = null;
			
			int i = 0;

			// Find a point in pointSet2 with the same Rhythm or less.
			while (matchingPoint == null && i < pointSet2.length)
			{
				NoteTree point = (NoteTree) pointSet2[i++];
				Rhythm pointRhythm = point.acceptedRhythm();
				
				if (pointRhythm.eval() == selectedPointRhythm)
					matchingPoint = point;
				
				if (pointRhythm.eval() < selectedPointRhythm)
				{
					int depth = selectedPointRhythm / pointRhythm.eval();
					// Dirty c-style.
					// Search down the leftmost branch if there is a point
					// we can use - DO NOT SWAP NULL CHILDREN!!!
					while ((point = (NoteTree)point.getChild(i)) != null && --depth > 0) ;
				}
				
				if (matchingPoint != null)
					break;
				// If we're here, we didn't find one, so lets move on
			}
			// Check again
			if (matchingPoint != null)
			{
				nt1.swapNotes(selectedPoint, matchingPoint);
				nt2.swapNotes(matchingPoint, selectedPoint);

				return new NoteTree[] { nt1, nt2 };
			}
		}
		ErrorFeedback.handle("WARNING: No compatible crossover points found for "+nt1.toString()+" and " +nt2.toString()+"." , new BreedException());		
		return null;
	}
	
	private void crossover(Individual i1, Individual i2)
	{
		try {
			NoteTree nt1 = ((NoteTree) i1.getNoteSequence()).clone();			
			NoteTree nt2 = ((NoteTree) i2.getNoteSequence()).clone();
			
			NoteTree[] children = _crossover(nt1, nt2);
			// Failed to do crossover on the children.
			if (children == null)
				return;
			
			// Add the new individuals to the population
			p.addIndividual(new Individual(nt1,i1.getHeuristic()));
			p.addIndividual(new Individual(nt2,i2.getHeuristic()));
			
		}
		catch (ClassCastException e) {
			
		}
		catch (SequenceException e) {
			ErrorFeedback.handle("WARNING: Crossover failed on "+i1.toString()+" and "+i2.toString()+".", e);
		}
		catch (IndividualAddException e) {
			// population full - ignore this.
		}
	}
	
	/*
	 * Only a few operations we can do
	 * -add
	 * -remove
	 * -change pitch
	 * -change swing
	 * -rest
	 * 
	 * TODO Factor out mutation operators
	 */
	private void mutate(Individual i)
	{
		// What shall we do?
		NoteLeaf[] mutationRefs = ((NoteTree)i.getNoteSequence()).getMutationReferences();
		NoteLeaf nl = mutationRefs[_mt.nextInt(mutationRefs.length)];
		PopulationParameters popParams = i.getParameters();
		SequenceParameters sp = null;
				
		if (popParams != null)
			sp = popParams.getSequenceParameters();
		//ScaledSet scale = sp.Jam.getScaledSet();
		
		int maxAttempts = 3;
		for (int attempts = 0; attempts < maxAttempts; attempts++)
		{
			int mutClass = _mt.nextInt(2);
			// Discern possible operations.
			if (mutClass == 0 && (Mutable.PITCH & nl.mutable()) != 0)
			{
				// Which pitch op to perform?
				// Change pitch (up or down a whole note).
				// Change octave?
				if (nl.isRelative())
				{
					nl.pitchRelative((_mt.nextBoolean() ? nl.pitchRelative() + 2 : nl.pitchRelative()-2) );
					return;
				}
				else {
					float noteNum = MIDI.noteToNumber(nl);
					noteNum += (_mt.nextBoolean() ? 2 : -2);
					// TODO Fix this across octaves.
					nl.pitchClass(MIDI.numberToNote(noteNum).pitchClass());
					return;
				}
			}			
			if (mutClass == 1 && (Mutable.RHYTHM & nl.mutable()) != 0)
			{
				// Which Rhythm option to perform?
				// Change swing
				switch (_mt.nextInt(3)) {
					// Swing remove
					case 0:
						nl.swingNote(0, null);
						return;						
					case 1: 
						nl.toggleRest();
						return;
						// Add rest/new pitch.
					case 2: 
						// This shouldn't happen... but just in case.
						if (sp == null)
							break;
						// TODO Find out why this breaks everything
						// Rhythm is added automatically.
						//nl.addNotes(scale.getRandom((Note)nl));
						return;					
				}			
			}
		}
		try {
			p.addIndividual(i);
		}
		catch (IndividualAddException e) {
			
		}		
	}
}
