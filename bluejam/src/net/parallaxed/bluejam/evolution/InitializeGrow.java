package net.parallaxed.bluejam.evolution;

import java.util.Iterator;

import ec.util.MersenneTwisterFast;

import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteLeaf;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.Rhythm;
import net.parallaxed.bluejam.ScaledSet;
import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.SequenceException;

/**
 * This form of the grow algorithm psuedorandomly selects 
 * a rhythm with which to assign the note before adding
 * it to the tree (having the effect of choosing all 
 * functions down to that depth, and the final terminal).
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class InitializeGrow implements NoteSequenceInitializer {
	
	private static final String E_TYPE_ERROR = "This class expects note sequences of type NoteTree";
	private static final int REST_PROPORTION = 5;
	private static final RhythmInitializer rhythmInitializer = RhythmInitializer.getInstance();
	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	private static InitializeGrow _instance = null; 

	/*
	 * Trivial.
	 */
	private InitializeGrow() {
				
	}
	
	/**
	 * @return An Instance of the "Grow" initialization algorithm.
	 */
	public static InitializeGrow getInstance() {
		if (_instance == null)
			_instance = new InitializeGrow();
		return _instance;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void initialize(NoteSequence notes, PopulationParameters params)
	{		
		if (params.getGenotype() != Genotype.NOTE_TREE)
			ErrorFeedback.handle(new Exception("This class uses NoteTrees. The specified Genotype must match Genotype.NOTE_TREE"));
		
		
		Iterator<Note> i = notes.getNotes();
		if (i.hasNext())
			_fromHeuristic(notes,params);
		else
			_fromScratch(notes, params);
		
	}
	
	/**
	 * Fill out tree.
	 * 
	 * Get mutable nodes.
	 * Mutate.
	 * 
	 * @param notes
	 * @param params
	 */
	private void _fromHeuristic(NoteSequence notes, PopulationParameters params) {		
		
		try { 
			SequenceParameters _sp = params.getSequenceParameters();
			ScaledSet s = params.getSequenceParameters().Jam.getScaledSet();
			NoteTree _nt = (NoteTree) notes;
			int workingOctave = 5;
			
			// Should return all nodes with one or more empty children.
			NoteSequence[] incompleteNodes = _nt.getIncompleteReferences();
			
			for (NoteSequence n : incompleteNodes)
			{
				if (n.getClass() == NoteTree.class)
				{
					NoteTree node = (NoteTree) n;
					/*
					 * We have a node, which already has an accepted rhythmic value.
					 * We can generate a noteLeaf collection to fill the gap(s)
					 * - getNextRhythm() until acceptable.
					 * - generate as many notes of that Rhythm as required
					 * - call initialize(NoteTree with the filled children :)
					 * 
					 */
					Rhythm accepted = node.acceptedRhythm();
					Rhythm r = accepted;
					// one shot generate a smaller rhythm
					Rhythm rand = RhythmInitializer.getNextRhythm();
					if (rand.eval() > accepted.eval())
						r = rand;
					
					int numChildren = node.getNumChildren();
					
					for (int i= 0; i < numChildren; i++)
					{
						// Try and get some kind of context
						NoteSequence referenceNode = node.getChild(i);								
						Note _refNote = null;
						Iterator<Note> iter = null;
						
						if (referenceNode == null)
						{
							if (i == 0)
							{								
								// Are we filling before a subsequent one?
								// TODO Model not symmetrical, fix this.
								while (i++ < numChildren)
									if ((referenceNode = node.getChild(i)) != null)
										 break;
								i = 0;
								// Did we find one? Make one up if not.
								if (referenceNode == null)
									referenceNode = new NoteLeaf(_sp.Jam.rootPitch(),workingOctave);
								// So we found a reference _after_ the current one, get it's first note.
								_refNote = referenceNode.getNotes().next();
							}
							else
								for (_refNote = (iter = node.getNotes()).next(); iter.hasNext(); _refNote = iter.next()) ;
						}
						else
							// We found a NoteSequence before this one.
							// Seek to the end of the sequence at referenceNode
							for (_refNote = (iter = referenceNode.getNotes()).next(); iter.hasNext(); _refNote = iter.next()) ;
												
						// Calculate the number of notes we need to add at that rhythm
						int numberOfNotes = accepted.eval() / node.acceptedRhythm().eval();
						
						/*
						 * We don't want to repeat the addition of a particular
						 * rhythm too many times. 
						 * 
						 * If this happens, we call this function again later for this node.
						 */						
						// TODO Some MAX_CLUSTER variable?
						// TODO Investigate increasing the granularity
						// TODO Some "backoff" variable to force choosing a different rhythm
						if (numberOfNotes > 3)
							numberOfNotes = 4;							
						
						NoteLeaf nl = null;
						// Add that number of notes to our sequence.
						while (numberOfNotes-- > 0)
						{
							// TODO FIX Dynamically change reference node.
							
							nl = s.getRandom(_refNote);
							
							// Correct the octave if anything went wrong.
							if (nl.octave() < -1)
								nl.octave(workingOctave);
							else
								workingOctave = nl.octave();
							
							// Apply rhythm
							nl.rhythm(r);
							
							// Maybe rest the note
							if (_mt.nextInt(100) < REST_PROPORTION)  
								nl.toggleRest();
							
							node.addNotes(nl);
							
							// Update the reference note.
							_refNote = nl;
						}				
					}
					// Rinse, lather, and - repeat?	
					// Being recursive isn't ideal, but it works. This should
					// also keep it constrained to all nodes under the current one
					// only.
					_fromHeuristic(node, params);
					
					// Apply swing to the finished product.
					rhythmInitializer.initialize(node, params);
									
				}
				else
				{
					ErrorFeedback.handle(E_TYPE_ERROR, new Exception());
				}
			}
						

			/*
			 * Initializer tactic:
			 * Take all notes in the heuristic that are mutable (getMutationPoints())
			 * add a fraction of them to _addedNotes before passing to propertyInit.
			 */
		}
		catch (ClassCastException e) {
			ErrorFeedback.handle(E_TYPE_ERROR, e);
		}
		catch (SequenceException e) {
			// tree full
		}
	}
	
	/**
	 * Initializes the passed NoteSequence using the grow
	 * method, without following a heuristic.
	 * 
	 * @param notes
	 * @param params
	 */
	private void _fromScratch(NoteSequence notes, PopulationParameters params)
	{
		SequenceParameters _sp = params.getSequenceParameters();
		NoteLeaf _root = new NoteLeaf(_sp.Jam.rootPitch(),Rhythm.QUAVER,5);
		try {
			notes.addNotes(_root);
			_fromHeuristic(notes, params);
			
		}
		catch (SequenceException e) { ErrorFeedback.handle(e.getMessage(), e); }
	}
}
