package net.parallaxed.bluejam;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import ec.util.MersenneTwisterFast;

import net.parallaxed.bluejam.evolution.IndividualSelector;
import net.parallaxed.bluejam.evolution.TreeBreeder;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.IndividualAddException;
import net.parallaxed.bluejam.playback.Listener;
import net.parallaxed.bluejam.playback.Player;

/**
 * The evolve class carries out the main evolution cycles and
 * outputs "winning" musical candidates by notifying listeners. Instances
 * of this object are meant to be run in their own thread.
 * 
 * As the generations are run, registered listeners will receive
 * references to NoteSequences. Feedback can be given on these
 * sequences by calling the feedback() method with a score
 * and a reference back to the NoteSequence that achieved that score.
 * In the case of positive feedback, the system can choose to
 * make that candidate and other high-scoring candidates from the
 * population "elite", which means they get passed in to 
 * subsequent generations.
 * 
 * This class implements the Player interface, a primitive events
 * pattern.
 * 
 * @see Listener
 * @see Player
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class Evolve implements Runnable, Player
{
	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	private HashMap<NoteSequence, Population> played = new HashMap<NoteSequence, Population>();
	private int playedMemorySize = 10;
	
	private ArrayList <Listener> listeners = new ArrayList<Listener>();
	private ArrayList <NoteSequence> elites = new ArrayList<NoteSequence>();
	private int maxElites = 10;
	
	private SequenceParameters  _sequenceParameters = null;	
	private HeuristicCollection _heuristics = null;
	private Population population = null;
	
	public boolean running = true;
	private volatile boolean waiting = false;
	/**
	 * This is the maximum number of outputs we make before we
	 * wait on feedback from listeners.
	 * TODO Optimize for multiple listeners.
	 */
	private int listenBufferSize = 5;
	
	/**
	 * Keep our track of the properties we need to update in the next
	 * generation.
	 */
	private int _memberCount = 50;
	

	protected int generations = 25;
	/**
	 * The number of generations we're going to run.
	 */
	public int generations() { return generations; }
	/**
	 * Sets the number of generations. Limit: 0 < x < 100
	 * @param numberOfGenerations The number of generations to complete
	 */
	public void generations(int numberOfGenerations) 
	{
		if (generations > 0 && generations < 200)
			generations = numberOfGenerations;
	}	
	
	// Increased from 8 to halve selection pressure.
	private int _matingPoolSize = 15;
	
	/**
	 * Sets the size of the mating pool. Limit: 1 < x < 21
	 * @param matingPoolSize The new size of the pool
	 */
	public void matingPoolSize(int matingPoolSize) { 
		if (matingPoolSize >= 2 && matingPoolSize <= 20)
			_matingPoolSize = matingPoolSize;
	}
	private int _maxBreedCycles = 10;
	
	private double _crossoverProbability = 0.9;
	
	/** 
	 * Adds a listener to this player.
	 */
	public void addListener(Listener listener) 
	{
		listeners.add(listener);
	}
	
	/**
	 * Accepts a positive or negative feedback value (usually
	 * -1 or 0 or 1), and turns on elitism for that NoteSequence
	 * making it appear in subsequent generations.
	 */
	public void feedback(int feedback, NoteSequence notes)
	{
		// Should be garbaged later.			
		
		// Hold on to this one.
		if (feedback == 1) {
			System.out.println("Recieved positive feedback! Enabling elitism for "+notes.toString());
			if (elites.size() >= maxElites)
				elites.remove(0);
			if (!elites.contains(notes))
				elites.add(notes);
		}
		
		listenBufferSize++;
		
		/**
		 * Should restart the sleeping evolution thread running this
		 * object if there is one. 
		 */
		synchronized (this) {
			waiting = false;
			notify();
		}
		played.remove(notes);
	}
	
	/**
	 * Pauses the evolution from outside this thread.
	 */
	public void togglePause() {
		if (waiting)
			synchronized (this) {
				waiting = false;
				System.out.print("\nPausing...");
				notify();				
			}		
		else
			waiting = true;
	}
	
	
	public PopulationParameters getPopulationParameters() {
		return population.getParameters();
	}
	
	
	/**
	 * Runs the main evolution thread.
	 */
	public void run() {
		if (population == null)
			throw new IllegalArgumentException("No population found");
		/*
		 * Initialize everything, then run evoluton cycles.
		 * Run evolution cycle.
		 */
		try {
			
			population.initialize();
			Class<?> selection = population.getParameters().getSelectionType().eval(); 
			Method _cons = selection.getMethod("getInstance", new Class[] {});				
			IndividualSelector _indS =  (IndividualSelector) _cons.invoke(null, new Object[] {});
			
			// TODO Associate this with the genotype			
			while(running && generations-- > 0) {
				//populations.add(population);
				if (waiting || listenBufferSize == 0)
				{
					try {
						synchronized (this) {
							waiting = true;
							System.out.println("Evolution waiting for listener...");
							wait();
							// Double check we've not been awakened prematurely from togglePause();
							if(listenBufferSize == 0) {
								System.out.println("Resumed.");
								continue;
							}								
						}
					}
					catch (InterruptedException e) { ErrorFeedback.handle("Interrupted.", e); }
				}
				
				
				// TODO Make parameters generic.
				Population p = _indS.select(population, _matingPoolSize);
				
				// QUIRKY - Almost 2/3 of the time we select the fittest.
				Individual winner = (_mt.nextInt(22) < 14) ? p.getFittestIndividual() : p.getRandomIndividual();
				NoteSequence play = winner.getNoteSequence();
				
				// Remove one if we're over budget on retention.
				if (played.size() > playedMemorySize)
					played.remove(played.keySet().iterator().next());
				
				/*
				 * Record this, and play it to the listeners.
				 */
				played.put(play, population);				
				for (Listener l : listeners)
					l.listen(play);
				
				// Decrement the listen buffer.
				listenBufferSize--;
				try {
					// Re-insert heuristic individuals before breeding.
					if (_heuristics != null)
						for (int i = 0; i < _heuristics.size(); i++) {
							Individual h = new Individual(p, _heuristics.get(i));
							h.initialize();
							p.addIndividual(h);
						}
					
					// Re-insert elites.
					if (elites.size() > 0)
						for (NoteSequence n : elites)
						{
							Individual elite = null;
							// This encourages the individual to evolve.
							if (n instanceof Heuristic)
								elite = new Individual(n,(Heuristic)n);
							else
								elite = new Individual(n,(Heuristic) null);							
							p.addIndividual(elite);
						}
				}
				catch (IndividualAddException e) {
					ErrorFeedback.handle(e);
				}
				
				p.memberCount = _memberCount;
				TreeBreeder pool = new TreeBreeder(_maxBreedCycles,_crossoverProbability);
				pool.breed(p);
				
				population = p;
				System.gc();
			}
		}
		catch (Exception e) {
			ErrorFeedback.handle(e.getMessage(), e);
		}
		for (Listener l : listeners)
			l.listen(null);		
	}
	
	public void setPopulationCount(int memberCount)
	{
		if (memberCount >= 25 &&  memberCount <= 250)
			_memberCount = memberCount;
	}
	/**
	 * Creates an instance of the Evolve class
	 * @see SequenceParameters
	 * @param sequenceParameters The sequenceParameters to use.
	 */
	public Evolve(SequenceParameters sequenceParameters, int populationCount) {
		this(sequenceParameters, populationCount, null);
	}
	
	/**
	 * Creates an instance of the Evolve class.
	 * @see SequenceParameters
	 * @see HeuristicCollection
	 * @param sequenceParameters The sequenceParameters to use.
	 * @param heuristics The HeuristicCollection to use.
	 */
	public Evolve(SequenceParameters sequenceParameters, int populationCount, HeuristicCollection heuristics)
	{
		if (sequenceParameters == null)
			throw new IllegalArgumentException("SequenceParameters must not be null.");
		_sequenceParameters = sequenceParameters;	
		_heuristics = heuristics;
		population = new Population(_sequenceParameters,populationCount,_heuristics);
	}
}
