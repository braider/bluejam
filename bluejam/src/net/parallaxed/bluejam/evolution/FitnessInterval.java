package net.parallaxed.bluejam.evolution;

import java.util.ArrayList;
import java.util.Iterator;

import ec.util.MersenneTwisterFast;

import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteSequence;

import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.playback.MIDI;

/**
 * This class selects a random fitness method to apply.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class FitnessInterval implements IndividualEvaluator {
	
	
	private static FitnessInterval _instance = null;
	
	/**
	 * How many repetitions before we start to reduce the fitness?
	 */
	public static int IntervalWindow = 4;
	/**
	 * Defines how many cases of repetition should be "let through"
	 */
	public static int BackoffPercent = 20;
	
	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	
	private FitnessInterval() {	}
	
	/**
	 * @return An instance of the FitnessInterval Evaluator
	 */
	public static FitnessInterval getInstance()
	{
		if (_instance == null)
			_instance = new FitnessInterval();
		return _instance;
	}
	/**
	 * {@inheritDoc}
	 * 
	 * The way to evaluate intervals is simply to skip over the
	 * noteSequence and find out how many notes have interval > 3.
	 * 
	 * This is acceptable in a few contexts, and octave jumps
	 * are permitted (in few amounts)
	 */
	public double evaluate(Individual individual)	
	{
		NoteSequence notes = individual.getNoteSequence();
		
		try {
			// TODO REWORK This is dirty and inefficient.
			notes.validateNotes();
			
			Iterator<Note> _notes = notes.getNotes();
			float lastNote = -1;
			ArrayList<Integer> intervals = new ArrayList<Integer>();
			int largeIntervals = 0;
			int octaveIntervals = 0;
			
			while (_notes.hasNext())
			{
				Note n = _notes.next();
				float thisNote = MIDI.noteToNumber(n);
				// the second condition is evaluated if the last note was -1
				if (lastNote < 0 && (lastNote = thisNote ) > -1)
					continue;
				// if our assignment failed, carry on.
				else if (lastNote < 0)
					continue;
				
				
				// Interval will be a signed integer indicating the direction of the progression
				float interval = thisNote - lastNote;
				intervals.add((int)((interval < 0) ? 0-interval : interval));

				if (interval > 3 || interval < -3)				
					if (interval == 12)
						octaveIntervals++;
					else
						largeIntervals++;
				lastNote = thisNote;
			}
			
			// Two-pass :: Find repeated intervals
			Iterator<Integer> _intervals = intervals.iterator();
			int repetitionCount = 0;
			int matched = 0;
			int lastInterval = 0;
			while (_intervals.hasNext())
			{
				if (_intervals.next() == lastInterval)
					matched++;
				if (matched > IntervalWindow)
				{
					if (_mt.nextInt(100) > BackoffPercent)
						repetitionCount++;
					matched = 0;
				}
			}
			// Half the time we're harsh, the other half, not so much.
			return (_mt.nextBoolean() ? _smoothedFitness(repetitionCount,largeIntervals) : _harshFitness(repetitionCount,largeIntervals));
		}
		catch(Exception e)
		{
			ErrorFeedback.handle("WARNING: Caught exception in interval fitness evaluation", e);
			return 0d;		
		}		
	}
	
	private double _smoothedFitness(int repetitionCount, int largeIntervals)
	{
		return 1/(Math.log(Math.pow(2, repetitionCount)+Math.pow(2,Math.sqrt(largeIntervals))));
	}
	
	private double _harshFitness(int repetitionCount, int largeIntervals)
	{
		return 3/(Math.pow(2, repetitionCount) + Math.pow(2, Math.sqrt(largeIntervals)));
	}
}
