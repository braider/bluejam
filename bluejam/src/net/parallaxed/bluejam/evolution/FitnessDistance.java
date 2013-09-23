package net.parallaxed.bluejam.evolution;

import java.util.Iterator;
import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteCollection;
import net.parallaxed.bluejam.NoteSequence;

/**
 * Implements a measure of fitness by comparing the result
 * of an individual to its original heuristic through 
 * means analysing each note in the sequence to see
 * that it is sufficiently displaced from those around
 * it. The bracket of notes that are examined is defined by the
 * DistanceThreshold, so for one note either side (the default)
 * that's 3.
 * 
 * The final fitness score is given by adding up the measure
 * of how similar the final output is, then taking the 
 * logarithm of that score before putting it into a function
 * to get the final fitness value.
 * 
 * To give an idea of numbers, a similarity score of about 
 * 300-400 is virtually identical, 200-300 is too similar,
 * 100-200 is similar, 50-100 is good, 10-50 is good but getting
 * dissimilar, 0-10 is unrecognisable from the original
 * heuristic. We should start generating new heuristics if this measure
 * is returning the maximum possible fitness. 
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class FitnessDistance implements IndividualEvaluator {
	
	private static FitnessDistance _instance = null;
	/**
	 * Must be an odd number representing the window of
	 * comparison for the distance.
	 * 
	 * The larger this number is, the longer the algorithm
	 * takes to run - we grow in linear time (O(n)).
	 */
	private static int DistanceThreshold = 3;
	private FitnessDistance() {	}
	
	/**
	 * @return The singleton instance of FitnessDistance
	 */
	public static FitnessDistance getInstance()
	{
		if (_instance == null)
			_instance = new FitnessDistance();
		return _instance;
	}
	
	/**
	 * Compare each note in the Sequence property for property.
	 * 
	 * Use a minimal threshold distance to check if the properties
	 * are shared either side.
	 * 
	 * If properties are shared, we increment the score. If the 
	 * properties are shared at the midpoint of the window defined 
	 * by DistanceThreshold, we increment the score again.
	 * 
	 */
	public double evaluate(Individual individual) {
		NoteSequence _notes = individual.getNoteSequence();
		NoteSequence _heuristic = (NoteSequence) individual.getHeuristic();
		Iterator<Note> notes = _notes.getNotes();
		Iterator<Note> hNotes = _heuristic.getNotes();
		int score = 0;
		
		NoteCollection heuristicNotes = new NoteCollection();
		while (hNotes.hasNext())
			heuristicNotes.add(hNotes.next());
		
		int window = 0;
		if (DistanceThreshold % 2 == 0)
			DistanceThreshold++;
		int comp = (int)Math.floor(DistanceThreshold/2);
		int midpoint = DistanceThreshold - comp -1;
		Note[] n = new Note[DistanceThreshold];
		int index = 0;
		while (notes.hasNext())
		{
			// continually reused index.
			int i;
			
			for (i=0;i<n.length;i++)
				n[i] = null;
			
			i = 0;
			int max = index + comp;
			for (window = index - comp; window <= max; window++)
			{
				if (window < 0)
					continue;
				if (window >= heuristicNotes.size())
					continue;
				n[i++] = heuristicNotes.get(window);				
			}
						
			Note sequenceNote = notes.next();
			i = 0;
			// Do we have shared properties?
			for (Note heuristicNote : n)
			{
				if (heuristicNote == null)
					continue;
				if (sequenceNote.pitchClass() == heuristicNote.pitchClass() &&
						sequenceNote.octave() == heuristicNote.octave() &&
						sequenceNote.pitchRelative() == heuristicNote.pitchRelative())
				{
					// are they at the midpoint? - increase threefold
					if (i == midpoint)
					{
						score = score + 3;
						continue;
					}
					score++;
				}
				if (i++ == midpoint)
				
				if (sequenceNote.rhythm() == heuristicNote.rhythm() && 
						sequenceNote.swingPercent() == heuristicNote.swingPercent())
					score++;			
			}
			index++;
		}	
		double scoreVal = Math.log(score);
		double fitness =(2*Math.PI*scoreVal-Math.pow(2,scoreVal)-5)/6;
		if (fitness > 1)
			fitness = 1;
		return (fitness < 0 ? 0 : fitness);
		
		// 2pix - 2^x -5 !!!!!!
	}
}
