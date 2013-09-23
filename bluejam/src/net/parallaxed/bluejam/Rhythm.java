package net.parallaxed.bluejam;

import java.util.HashMap;

/**
 * This Enum contains all the rhythm's BlueJam supports.
 * 
 * Consequently, the maximum depth of any NoteTree (in the
 * default implementation) = the number of constants in this
 * enum, plus 1 (currently, 8).
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public enum Rhythm {
	
	SEMIBREVE,MINIM,CROTCHET,QUAVER,SEMIQUAVER,DEMIQUAVER,HEMIQUAVER;
	private static final String E_EVAL_FAIL = "Unrecognized rhythm";
	
	private static final HashMap<Integer,Rhythm> rhythmNumber = new HashMap<Integer,Rhythm>();
	
	static {
		rhythmNumber.put(1,SEMIBREVE);
		rhythmNumber.put(2,MINIM);
		rhythmNumber.put(4,CROTCHET);
		rhythmNumber.put(8,QUAVER);
		rhythmNumber.put(16,SEMIQUAVER);
		rhythmNumber.put(32,DEMIQUAVER);
		rhythmNumber.put(64,HEMIQUAVER);
	}
	
	/**
	 * Returns the rhythm matching the supplied name, e.g. "Semibreve",
	 * "Demiquaver" etc. 
	 * 
	 * Case Insensitive.
	 * @param s The name of the rhythm
	 * @return The rhythm enumeration for the given name
	 */
	public static Rhythm getRhythm(String s)
	{
		if (s.matches("(?i)semibreve"))
			return Rhythm.SEMIBREVE;
		if (s.matches("(?i)minim"))
			return Rhythm.MINIM;
		if (s.matches("(?i)crotchet"))
			return Rhythm.CROTCHET;
		if (s.matches("(?i)quaver"))
			return Rhythm.QUAVER;
		if (s.matches("(?i)semiquaver"))
			return Rhythm.SEMIQUAVER;
		if (s.matches("(?i)demiquaver"))
			return Rhythm.DEMIQUAVER;
		if (s.matches("(?i)hemiquaver"))
			return Rhythm.HEMIQUAVER;
		return null;
	}
	
	/**
	 * Gets a rhythm given it's eval() value.
	 * @param number The eval() value of the rhythm (1/x)
	 * @return The Rhythm enumeration for the given number.
	 */
	public static Rhythm getRhythm(int number) {
		Rhythm accepted = rhythmNumber.get(number);
		if (accepted == null)
		{
			@SuppressWarnings("unused")
			boolean b = true;
		}
		return accepted;
	}
	
	/**
	 * Return the decimal fraction of a bar represented by
	 * this note in regular notation and a 4/4 time signature.
	 * 
	 * Some hacks may be needed if using this to represent 
	 * an odd or irrational time signature, all others can 
	 * be calculated relative to these values.
	 * 
	 * @return A fraction representing that note's value.
	 * @throws RuntimeException if there is no hardcoded value for that rhythm
	 */
	public float evalR()
	{
		switch (this) {
			case SEMIBREVE: return 1;
			case MINIM: return 0.5f;
			case CROTCHET: return 0.25f;
			case QUAVER: return 0.125f;
			case SEMIQUAVER: return 0.0625f;
			case DEMIQUAVER: return 0.03125f;
			case HEMIQUAVER: return 0.015625f;		
		}
		throw new RuntimeException(E_EVAL_FAIL);		
	}
	
	/**
	 * Return the reciprocal of the fraction represented by
	 * this rhythmic value.
	 * 
	 * The reciprocal is 1/evalR().
	 * 
	 * @return The denominator of the rhythmic fraction 
	 */
	public int eval()
	{
		switch (this) {
			case SEMIBREVE: return 1;
			case MINIM: return 2;
			case CROTCHET: return 4;
			case QUAVER: return 8;
			case SEMIQUAVER: return 16;
			case DEMIQUAVER: return 32;
			case HEMIQUAVER: return 64;		
		}
		throw new RuntimeException(E_EVAL_FAIL);		
	}
	
	/**
	 * @param steps The factor to increase the rhythm by (i.e. 1 doubles the rhythm, 2 triples, etc)
	 * @return a new (augmented) rhythm, based on the number of steps.
	 */
	public Rhythm increase(int steps)
	{
		if (steps == 0)
			return this;
		int number = this.eval();
		int rhythmFrac = number * ((int)Math.pow(2,steps));
		if (rhythmFrac > 64 || rhythmFrac < 1)
			return null;
		return rhythmNumber.get(rhythmFrac);
	}
}
