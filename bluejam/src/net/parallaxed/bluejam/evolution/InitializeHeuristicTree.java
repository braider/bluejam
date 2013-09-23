package net.parallaxed.bluejam.evolution;

import java.util.Iterator;

import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteLeaf;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.ScaledSet;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.SequenceException;

/**
 * Initializes note sequences using crossover on the heuristics, as
 * defined by the TreeBreeder method, breed(NoteTree,NoteTree).
 * 
 * This initializer performs crossover on the NoteSequences
 * passed to it (which are clones of the Heuristics, if present).
 * 
 * On the first call of the function, the passed NoteSequence is
 * stored in the Initializer. On the second call, crossover is
 * performed and the memory of the Initializer is erased.
 * 
 * @see NoteTree
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class InitializeHeuristicTree implements NoteSequenceInitializer {
	
	private static InitializeHeuristicTree _instance = null;
	private NoteSequence _nsBuffer = null;
	private TreeBreeder breeder = new TreeBreeder();
	
	private InitializeHeuristicTree() {
				
	}
	
	public static InitializeHeuristicTree getInstance() {
		if (_instance == null)
			_instance = new InitializeHeuristicTree();
		return _instance;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Satisfies implementation of NoteSequenceInitializer.
	 * 
	 */
	public void initialize(NoteSequence notes, PopulationParameters params)
	{
		if (notes.getClass() != NoteTree.class)
			ErrorFeedback.handle(new Exception("This class uses NoteTrees. The specified Genotype must match Genotype.NOTE_TREE"));
		
		NoteTree nt = (NoteTree) notes;
		NoteSequence[] ns = nt.getIncompleteReferences();
		ScaledSet s = params.getSequenceParameters().Jam.getScaledSet();
		
		if (ns.length > 0)
		{
			// Simply fill in the missing pitches, without growing them.
			for (NoteSequence incomplete : ns)
			{				
				NoteTree ntI = (NoteTree) incomplete;
				NoteTree ntP = (NoteTree) ntI.parent();
				
				Note refNote = null;
				if (ntP != null) {
					Iterator<Note> note = ntP.getNotes();
					if (note.hasNext())
						for (refNote = note.next(); note.hasNext(); refNote = note.next()) ;
				}
					
				NoteLeaf nl = new NoteLeaf(Pitch.R,ntI.acceptedRhythm(),5);
				
				if (s != null && refNote != null) {
					nl.octave(refNote.octave());
					nl.pitchClass(s.getRandom(refNote).pitchClass());
				}
				try {
					ntI.addNotes(nl);
				}
				catch (SequenceException e) {
					ErrorFeedback.handle("Error while filling out heuristic.",e);
				}
			}
		}
			
		if (_nsBuffer == null) {
			_nsBuffer = notes;
			return;
		}
		
		// We've been called before - combine the two.
		if (_nsBuffer != null) 	{
			breeder.recombine((NoteTree)_nsBuffer, (NoteTree)notes);
			_nsBuffer = null;
		}
	}
}
