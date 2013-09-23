package net.parallaxed.bluejam;

import ec.util.MersenneTwisterFast;


/**
 * Given a set of rules for the scale, ScaledSet can work out 
 * the set of notes that fit that scale, over a given number
 * of octaves. A set of the chromatic notes in that scale 
 * can also be produced.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class ScaledSet extends NoteLeafSet
{
	private static final MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	/**
	 * This array describes pitch classes. Pitch classes encapsulate
	 * enharmonic notes and divide the pitches into orders of 12,
	 * which can then be used to calculate the given scale.
	 */
	public Pitch[][] pitchClass = new Pitch[12][];
	private int _octaveChangeProbability = 20;
	/**
	 * If we're at the cusp of the next octave, here's the
	 * chance that we have of going up or down.
	 * TODO Include threshold for cusp.
	 * 
	 * @param probability The probability of changes
	 */
	public void octaveChangeProbability(int probability)
	{
		if (probability >= 0 && probability <= 100)
			_octaveChangeProbability = probability;
	}
	/**
	 * @return The probability we will change from the current octave.
	 */
	public int octaveChangeProbability() {
		return _octaveChangeProbability;
	}
	
	private Pitch pitch = null;
	private Scale scale = null;
	// Pitch 6 - at the cusp.
	private Pitch VI = null;
	
	/**
	 * Constructs a set of all notes of the right pitch over
	 * a given octave range and scale.
	 * 
	 * Limits are always exclusive, so for Pitch.A on octaves 3-5
	 * will include A3, and all subsequent notes up to A6 (but not A6).
	 * 
	 * @param rootPitch The root pitch of the scale
	 * @param scale The scale to use for generating notes
	 * @param minOctave The lower 8ve limit
	 * @param maxOctave The upper 8ve limit.
	 */
	public ScaledSet(Pitch rootPitch, Scale scale, int minOctave, int maxOctave)
	{
		this.pitch = rootPitch;
		this.scale = scale;
		setRange(minOctave,maxOctave);
		pitchClass[0] = new Pitch[1];
		pitchClass[0][0] = Pitch.A;
		pitchClass[1] = new Pitch[2];
		pitchClass[1][0] = Pitch.As;
		pitchClass[1][1] = Pitch.Bb;
		pitchClass[2] = new Pitch[1];
		pitchClass[2][0] = Pitch.B;
		pitchClass[3] = new Pitch[1];
		pitchClass[3][0] = Pitch.C;
		pitchClass[4] = new Pitch[2];
		pitchClass[4][0] = Pitch.Cs;
		pitchClass[4][1] = Pitch.Db;
		pitchClass[5] = new Pitch[1];
		pitchClass[5][0] = Pitch.D;
		pitchClass[6] = new Pitch[2];
		pitchClass[6][0] = Pitch.Ds;
		pitchClass[6][1] = Pitch.Eb;
		pitchClass[7] = new Pitch[1];
		pitchClass[7][0] = Pitch.E;
		pitchClass[8] = new Pitch[1];
		pitchClass[8][0] = Pitch.F;
		pitchClass[9] = new Pitch[2];
		pitchClass[9][0] = Pitch.Fs;
		pitchClass[9][1] = Pitch.Gb;
		pitchClass[10] = new Pitch[1];
		pitchClass[10][0] = Pitch.G;
		pitchClass[11] = new Pitch[2];
		pitchClass[11][0] = Pitch.Gs;
		pitchClass[11][1] = Pitch.Ab;
		generateNotes();
	}
	
	/**
	 * This generates the notes of the given scale and adds
	 * them to the set.
	 */
	private void generateNotes()
	{
		if ((this.scale == null) || (this.pitch == null))
			return;
		
		int[] stepSize = scale.eval();
		
		// discover which pitch is the root pitch
		int i;
		for (i=0; i < pitchClass.length; i++)
			if (pitchClass[i][0].equals(pitch))
				break;
		
		
		
		// until we've covered the entire octave range
		
		// go up in steps as defined in the Scale enum.
		for (int m = 0; (m < stepSize.length) && (minOctave <= maxOctave); m++)
		{
			
			// 0 is *always* sharp or natural
			Pitch p = pitchClass[i][0];
			
			// if there's a flat/sharp choice, decide if we should be flat instead
			if (pitchClass[i].length > 1)
				if (scale.eval(pitchClass[i], pitch, m) == Accidental.FLAT)
					// 1 is flat
					p = pitchClass[i][1];
			
			add(new NoteLeaf(p, minOctave));
			i += stepSize[m];
			/*
			 * Protect against ill-defined scale parameters
			 * This will come out weird though
			 * TODO Throw a warning about ill-defined scales.
			 */
			if (i > 11)	{
				minOctave++;
				i = i - 12;					
			}
		}

	
		// Set the cusp pitch.
		VI = ((NoteLeaf)get(size()-1)).pitchClass();		
	}
	
	@Override
	public NoteLeaf getRandom(NoteSequence n)
	{
		NoteLeaf note = super.getRandom(n);
		if (note == null)
			return null;
		
		if (note.pitchClass() == pitch)
			if (_mt.nextInt(100) < _octaveChangeProbability)
				note.octave(note.octave()-1);

		if (note.pitchClass() == VI)
			if (_mt.nextInt(100) < _octaveChangeProbability)
				note.octave(note.octave()+1);
		
		return note;			
	}
	/**
	 * Adds a chromatic note leaf to the set.
	 * TODO NOT IMPLEMENTED
	 * @param n The Note to add to this scaled set
	 * @return Whether the operation succeded or not.
	 */
	public boolean addChromaticNote(NoteLeaf n)	{
		return false;
	}
	
}
