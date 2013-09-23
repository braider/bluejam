package net.parallaxed.bluejam.evolution;

import java.util.Iterator;

import ec.util.MersenneTwisterFast;

import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.Rhythm;

/**
 * Initializes Rhythm and other contextual properties for a passed 
 * note sequence using a static probabilistic model.
 * 
 * This Initializer will track the context in which it is being 
 * initialized, which should aid the evolution by producing
 * programs which are more likely to follow a musically "fit"
 * pattern, rather than being completely random. This is because our
 * initial population size is comparatively quite small, and we 
 * need to guarantee a short termination time.
 * 
 * This implementation keeps track of context (i.e. tracks the
 * last notes that were added), assigning properties based
 * on notes that have been processed since the last clearContext()
 * call.
 * 
 * This implementation is BIASED towards returning Quavers.
 * Other implementations can take on any other desirable bias.
 * 
 * Specialised to NoteTree implementation - will call 
 * getAcceptedRhythm() to validate the added rhythm.
 * 
 * TODO Dynamic rhythm probabilities.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class RhythmInitializer implements NoteSequenceInitializer {
	
	/**
	 * For storing the context of the notes, getting contours
	 * etc.
	 */	
	private final int _maxSwingProbability = 70;
	private final int _minSwingProbability = 20;
	private final int _swingAmount = 10;

	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	
	/**
	 * Do NOT READ directly from this variable, use rhythmProbability
	 * instead.
	 */
	private static final Rhythm[] RHYTHM_ORDER = { Rhythm.SEMIBREVE, Rhythm.MINIM, Rhythm.CROTCHET, 
		Rhythm.QUAVER, Rhythm.SEMIQUAVER, Rhythm.DEMIQUAVER, Rhythm.HEMIQUAVER };	
	private static final double[] RHYTHM_PROBABILITY = { 
		// SEMIBREVE
		0.025d,
		// MINIM
		0.025d,
		// CROTCHET
		0.10d,
		// QUAVER
		0.525d,
		// SEMIQUAVER
		0.20d,
		// DEMIQUAVER
		0.10d,
		// HEMIQUAVER
		0.025d
	};
	private static final double[] rhythmProbability = new double[7];
	static {
			
		for (int i = 0;	i< RHYTHM_PROBABILITY.length; i++)
			rhythmProbability[i] = RHYTHM_PROBABILITY[i] + rhythmProbability[(i > 0 ? i-1 : 0)]; 
	}
	
	private static RhythmInitializer _instance = null;
	
	private RhythmInitializer() 
	{ 
		
	}
	
	/**
	 * @return An instance of the PropertyInitializer
	 */
	public static RhythmInitializer getInstance()
	{
		if (_instance == null)
			_instance = new RhythmInitializer();
		return _instance;
	}
	
	/**
	 * Initializes properties of Rhythm.
	 * 
	 * Default implementation initializes rhythm and swing on
	 * the passed NoteSequence.
	 */
	public void initialize(NoteSequence notes, PopulationParameters params)
	{
		NoteContext _notes = null;
		if (_notes == null)
			_notes = new NoteContext(params.getSequenceParameters());
		
		// Calculate the percent chance we have of swinging a note.
		int _swingThreshold = _mt.nextInt(100)*(_maxSwingProbability/100);
		_swingThreshold = (_swingThreshold > _minSwingProbability ? _swingThreshold : _minSwingProbability);
		
		Iterator<Note> note = notes.getNotes();
		boolean bSwung = false;
		Note n = null;
		// TODO Bi-directional swinging: Can only swing notes with subsequent partners at the moment.
		while (note.hasNext())
		{
			if (!bSwung)
				n = note.next();
			_notes.add(n);
			n.rhythm(getNextRhythm());
			
			// Don't swing notes that have a small rhythmic value
			// or if we don't have a next note
			// or if we're already swinging this note.
			if (!bSwung && n.rhythm().eval() >= Rhythm.QUAVER.eval() && note.hasNext())
			{				
				if (_mt.nextInt(100) < _swingThreshold)
				{
					bSwung = true;
					n.swingNote(_swingAmount, note.next());
					continue;
				}
			}
			// continue as normal.
			bSwung = false;
		}
	}
	
	/**
	 * This function does the same as a regular initialize() but does not
	 * assign arbitrary rhythms to the sequence.
	 * TODO Refactor duplicated code. 
	 * @param notes The notes to initialize
	 * @param params The PopulationParameters to use.
	 */
	public void initialize(NoteTree notes, PopulationParameters params)
	{
		NoteContext _notes = null;
		if (_notes == null)
			_notes = new NoteContext(params.getSequenceParameters());
				
		int _swingThreshold = _mt.nextInt(100)*(_maxSwingProbability/100);
		_swingThreshold = (_swingThreshold > _minSwingProbability ? _swingThreshold : _minSwingProbability);
		
		Iterator<Note> note = notes.getNotes();
		boolean bSwung = false;
		Note n = null;
		// TODO Bi-directional swinging
		while (note.hasNext())
		{
			if (!bSwung)
				n = note.next();
			_notes.add(n);
			
			if (!bSwung && n.rhythm().eval() >= Rhythm.QUAVER.eval() && note.hasNext())	{				
				if (_mt.nextInt(100) < _swingThreshold)	{
					bSwung = true;
					n.swingNote(_swingAmount, note.next());
					continue;
				}
			}
			bSwung = false;
		}
	}
	
	/**
	 * Returns a rhythm from the distribution specified by
	 * the static initializers of this class.
	 * @see Rhythm
	 * @return An instance of Rhythm
	 */
	public static Rhythm getNextRhythm()
	{	
		// TODO Further Work: Base this on context.
		
		double rand = _mt.nextDouble();
		int index = 0;
		for (double d : rhythmProbability)
			if (rand > d)
				index++;
			else break;
		
		return RHYTHM_ORDER[index];
	}
}
