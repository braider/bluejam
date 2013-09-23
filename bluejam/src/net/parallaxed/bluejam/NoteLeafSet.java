package net.parallaxed.bluejam;

import java.util.Iterator;

import ec.util.MersenneTwisterFast;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.PitchException;
import net.parallaxed.bluejam.grammar.PitchModel;


/**
 * The note set of all possible pitch classes in the octave
 * range supplied to the constructor.
 * 
 * TODO Unit test for this...
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class NoteLeafSet extends TerminalSet
{
	private static final String E_TYPE = "The terminal is of the wrong type";
	private static final String W_PITCH_NULL = "Warning the pitch on the passed note sequence was null ";
	/**
	 * Records the minimum range for the octave
	 */
	protected int minOctave = 4;
	/**
	 * Records the maximum range for the octave
	 */
	protected int maxOctave = 5;

	/**
	 * Trivial constructor does nothing if we're using one of
	 * the subclasses.
	 */
	public NoteLeafSet()
	{
	}
	
	/**
	 * Overrides the superclass' add function to ensure
	 * only NoteLeaf instances are added to this set.
	 */
	public boolean add(Terminal t) {
		if (t instanceof NoteLeaf)
			return super.add(t);
		
		ErrorFeedback.handle(E_TYPE, new Exception(E_TYPE));
		return false;
	}
	private PitchModel _model = null;
	private MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	
	/**
	 * Constructs a NoteLeafSet constrained by octave range.
	 * 
	 * Each range starts on A and ends on Ab, so a range of 
	 * 4-5 yields all notes between A4-Ab5.
	 * 
	 * @param minOctave
	 * @param maxOctave
	 */	
	public NoteLeafSet(int minOctave, int maxOctave)
	{
		setRange(minOctave,maxOctave);
		generateNotes();
	}
	
	/**
	 * Sets the maximum range of the notes being played.
	 * 
	 * maxOctave must be >= minOctave
	 * 
	 * @param minOctave
	 * @param maxOctave
	 */
	public void setRange(int minOctave, int maxOctave)
	{
		// constrain to 1 < x < 7
		if ((minOctave > -1 || minOctave < 8) && (maxOctave >= minOctave)) 
		{
			this.minOctave = minOctave;
			this.maxOctave = maxOctave;			
			return;
		}
		throw new RuntimeException("Octave range min: "+minOctave+", max: "+maxOctave+" is invalid.");
	}
	
	private void generateNotes()
	{
		while (minOctave <= maxOctave)
		{
			add(new NoteLeaf(Pitch.A, minOctave));
			add(new NoteLeaf(Pitch.As, minOctave));
			add(new NoteLeaf(Pitch.Bb, minOctave));
			add(new NoteLeaf(Pitch.B, minOctave));
			add(new NoteLeaf(Pitch.C, minOctave));
			add(new NoteLeaf(Pitch.Cs, minOctave));
			add(new NoteLeaf(Pitch.Db, minOctave));
			add(new NoteLeaf(Pitch.D, minOctave));
			add(new NoteLeaf(Pitch.Ds, minOctave));
			add(new NoteLeaf(Pitch.Eb, minOctave));
			add(new NoteLeaf(Pitch.E, minOctave));
			add(new NoteLeaf(Pitch.F, minOctave));
			add(new NoteLeaf(Pitch.Fs, minOctave));
			add(new NoteLeaf(Pitch.Gb, minOctave));
			add(new NoteLeaf(Pitch.G, minOctave));
			add(new NoteLeaf(Pitch.Gs, minOctave));
			add(new NoteLeaf(Pitch.Ab, minOctave++));
		}
	}
	
	/**
	 * Returns a random terminal that is likely to fit with
	 * the current model, knowing that the last terminal is 
	 * of a given pitch.
	 * 
	 * Note that this only takes into consideration
	 * the very last note of the note sequence.
	 * 
	 * If no model has been set, simply returns a terminal 
	 * from vanilla getRandom().
	 * 
	 * @param n The NoteSequence preceding the current note.
	 */
	public NoteLeaf getRandom(NoteSequence n)
	{
		if(_model != null)
		{
			Note _note = null;
			Iterator<Note> i = null;
			Pitch p = null;
			
			// Get the pitch class of the last note in n.
			for (_note = (i = n.getNotes()).next(); i.hasNext(); _note = i.next()) 
				if (!i.hasNext())
					p = _note.pitchClass();
			
			if (p != null)
				return new NoteLeaf(getRandom(p),_note.octave());
			
			ErrorFeedback.handle(W_PITCH_NULL, new PitchException(_note));
			return null;
		}
		return ((NoteLeaf) getRandom()).clone();
	}
	
	public NoteLeaf getRandom(Note n)
	{
		if (_model != null && n != null)
			return new NoteLeaf(getRandom(n.pitchClass()),n.octave());
		else
			return ((NoteLeaf) getRandom()).clone();
	}
	
	/*
	 * Do not call this method if no model is loaded, or you will
	 * get a terminal warning.
	 */
	private Pitch getRandom(Pitch p)
	{
		if (p != null && _model != null) 
		{
			double[] model = _model.get(p);
			if (model == null)
				// This note isn't in our model
				return ((NoteLeaf) getRandom()).pitchClass();
			
			double d = _mt.nextDouble();
				
			for (int c = 0; c< model.length; c++)
			{
				if (d < model[c])
					// TODO Refactor: return a note from the set?
					return _model.getPitch(c);
			}
		}
		ErrorFeedback.handle("WARNING: No model loaded when getRandom() was called on the terminal set", new Exception()); 
		return ((NoteLeaf) getRandom()).pitchClass();
	}
	/**
	 * Sets the model that this NoteLeaf set should
	 * use.
	 * 
	 * @param model
	 */
	public void setModel(PitchModel model) throws Exception {
		if (model != null)
			model.validateModel();
		_model = model;		
	}
	
}
