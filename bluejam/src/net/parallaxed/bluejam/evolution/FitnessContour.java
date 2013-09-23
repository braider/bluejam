package net.parallaxed.bluejam.evolution;

import java.util.Iterator;

import ec.util.MersenneTwisterFast;

import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.evolution.NoteContext.Contour;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;

/**
 * Computes a fitness value for the contour of the 
 * supplied NoteSequence.
 * 
 * This is optimised to work with NoteTree, but will
 * return a value for sequences that are not NoteTrees.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class FitnessContour implements IndividualEvaluator {
	
	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	
	private static FitnessContour _instance = null;
	
	private FitnessContour() {	}
	
	public static FitnessContour getInstance()
	{
		if (_instance == null)
			_instance = new FitnessContour();
		return _instance;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public double evaluate(Individual individual)	{
		NoteSequence notes = individual.getNoteSequence();
		PopulationParameters parameters = individual.getParameters();
		
		NoteContext[] context = new NoteContext[] {
				new NoteContext(parameters.getSequenceParameters()),
				new NoteContext(parameters.getSequenceParameters())
		};
		
		// TODO Hash out the context maps and reference them with some unique hash over the note sequence.		
		// get this at the SEMIBREVE level - pick a pair?
		
		NoteSequence[] nodes = null;
		
		// Get two random nodes for contour checking.
		// Picks one of root[0-sequenceLength] (i.e. two of nodes under the root].
		if (notes.getClass() == NoteTree.class)
		{
			try {
				nodes = new NoteSequence[2];
				NoteTree _nt = (NoteTree) notes;
				int firstNode = _mt.nextInt(_nt.getNumChildren());
				int secondNode = _mt.nextInt(_nt.getNumChildren());			
				nodes[0] =  _nt.getChild(firstNode);
			
				// Select a different random node if it's the same.
				while (secondNode == firstNode)
					secondNode = _mt.nextInt(_nt.getNumChildren());
				nodes[1] =  _nt.getChild(secondNode);
				
				
				// If any of what we've picked is null, just forget about it.			
				if (nodes[0] == null || nodes[1] == null)
					nodes = null;
			}
			// catch the ClassCastException if our children are not NoteTrees
			catch (Exception e) {
				ErrorFeedback.handle("WARNING: FitnessContour failed while evaluating "+notes.toString(), e);
				nodes = null;
			}
		}
		
		// Gives an "Unchecked" warning - total balls.
		Iterator<Note>[] _notes = (Iterator<Note>[]) new Iterator[] { null, null };
		
		if (nodes == null)
			_notes[0] = notes.getNotes();		
		else 		
		{
			_notes[0] = nodes[0].getNotes();
			_notes[1] = nodes[1].getNotes();
		}
			
		for (int i = 0; i < context.length; i++)
			if (_notes[i] != null)
				while (_notes[i].hasNext()) 
					context[i].add(_notes[i].next());
		
		// TODO Factor out these Contour judgements?
		if (context[0].contour() == Contour.NONE) {
		
			if (context[1].contour() == Contour.UP)
				return 0.70;
			if (context[1].contour() == Contour.DOWN)
				return 0.70;
			if (_notes[1] == null) 
				// We can't give an informed opinion, so we bias the default.
				return 0.15d;
			// if (context[1].contour() == Contour.NONE)
			return 0;
		}
		
		else if (context[0].contour() == Contour.UP) {
			if (_notes[1] == null) 
				return 0.30d;
			if (context[1].contour() == Contour.NONE)
				return 0.70d;
			if (context[1].contour() == Contour.UP) 
				return 0.5d;
			if (context[1].contour() == Contour.DOWN)
				return 1;
		}
		
		else if (context[0].contour() == Contour.DOWN)	{
			if (_notes[1] == null) 
				return 0.30d;
			if (context[1].contour() == Contour.NONE)
				return 0.70d;
			if (context[1].contour() == Contour.DOWN) 
				return 0.5d;
			if (context[1].contour() == Contour.UP)
				return 1;
		}
		
		ErrorFeedback.handle("WARNING: Contour fitness escaped evaluation bounds", new Exception());
		return 0.0;
	}	
}
