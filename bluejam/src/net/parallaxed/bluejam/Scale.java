package net.parallaxed.bluejam;

/**
 * This Enum describes how to produce each scale using stepped 
 * jumps over pitch classes.
 * 
 * Each encoding is minimal, and describes how to navigate
 * from the root (I) through II-IV in the scale. VIII is the
 * root repeated. 
 * 
 * In the blues (I, bIII, IV, bV, V bVII), the flattened
 * fifth seems traditionally noted as a "sharp" or natural 
 * note. I don't know why this occurs but I see it everywhere.
 * Therefore the accidental map for the blues notes the fourth
 * as flat.
 *  
 * Since bV (flat fifth) is enharmonic to IV# (sharp fourth)
 * in all cases I see no problem with re-implementing this 
 * using flats only if desired. 
 * 
 * This enum can also map accidentals.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public abstract class Scale {
	
	/**
	 * Singleton instance returning method which all subclasses
	 * must implement and make visible.
	 * @return A scale of the type that has been instantiated.
	 */
	protected static Scale getInstance() { return null; }
	
	
	/**
	 * Subclasses of scale must have a representation of
	 * jumps around the pitch wheel (see included documentation
	 * on pitch definition), which allows the scale
	 * to produce a terminal set in any key.
	 */
	protected int[] scale = new int[] {};
	
	/**
	 * This defines the accidental map in subclasses.
	 * 
	 * Each scale has a scale map, which charts the note
	 * jumps throughout the pitch circle. The accidentals
	 * describe the scale map in relation to another scale,
	 * which is by default the diatonic major key.
	 * 
	 * When a note falls in a pitch class with multiple 
	 * members. This allows BlueJam to decide
	 */
	protected final Accidental[] accidents = new Accidental[] {};
		
	public final int[] eval() { return scale; } 
	/**
	 * This method provides a definite function call to find out
	 * which accidental should be present on a note.
	 * 
	 * noteNumber is not strictly needed for this.
	 * 
	 * @param pitchClass The pitch class of the note.
	 * @param noteNumber The note number, n, corresponds to accident[n]. The nth note in that scale.
	 * @param rootPitch Passing in the root pitch reduces the calculation time.
	 * @return The accidental of the note.
	 */
	public abstract Accidental eval(Pitch[] pitchClass, Pitch rootPitch, int noteNumber);	
	
	/**
	 * Defines a blues scale with hexatonic pitch representation.
	 * 
	 * @author Ciarán Rowe (csr2@kent.ac.uk)
	 */
	public static final class BLUES extends Scale {	
		private static Scale _instance = null;
		
		public static final Scale getInstance()
		{
			if (_instance == null)
				_instance = new BLUES();
			return _instance;
		}
		
		private BLUES() { scale = new int[] { 3, 2, 1, 1, 3, 2 }; }
				
		protected final Accidental[] accidents = 
			new Accidental[] { Accidental.NONE, Accidental.NONE, Accidental.NONE, 
				Accidental.SHARP, Accidental.NONE, Accidental.NONE };
		
		public final Accidental eval(Pitch[] pitchClass, Pitch rootPitch, int noteNumber)
		{			
			if (pitchClass.length < 1)
				throw new RuntimeException("Invalid pitch class");
						
			Accidental defaultKey = rootPitch.eval();
			
			if (noteNumber > 7 || noteNumber < 0)
				throw new RuntimeException("Invalid note class");
			
			if (pitchClass.length < 2 && (accidents[noteNumber] != Accidental.NONE))
				return Accidental.NATURAL;
			
			if (pitchClass.length < 2)
				return Accidental.NONE;
			
			if (pitchClass.length == 2 && defaultKey == Accidental.NONE && accidents[noteNumber] == Accidental.NONE)
				return Accidental.FLAT;
			
			// SHOULD THIS BE 4 IF M STARTS AT 0?
			if (pitchClass.length == 2 && noteNumber == 4)
				return Accidental.SHARP;
			
			return defaultKey;
		}
	}
	
	/**
	 * Defines a standard minor scale.
	 * 
	 * @author Ciarán Rowe (csr2@kent.ac.uk)
	 *
	 */
	public static final class MINOR extends Scale {
		private static Scale _instance = null;
		
		public static final Scale getInstance()
		{
			if (_instance != null)
				_instance = new MINOR();
			return _instance;
		}
		protected final int[] scale = new int[] { 2, 2, 1, 2, 2, 2, 1 };
		protected final Accidental[] accidents = 
			new Accidental[] { Accidental.NONE, Accidental.NONE, Accidental.KEY, 
				Accidental.NONE, Accidental.NONE, Accidental.KEY, Accidental.KEY };
		public final Accidental eval(Pitch[] pitchClass, Pitch rootPitch, int noteNumber)
		{
			throw new RuntimeException("Not implemented");
		}
	}
	
	/**
	 * Defines a standard major scale.
	 * 
	 * @author Ciarán Rowe (csr2@kent.ac.uk)
	 *
	 */
	public static final class MAJOR extends Scale {
		private static Scale _instance = null;
		
		public static final Scale getInstance()
		{
			if (_instance != null)
				_instance = new MAJOR();
			return _instance;
		}
		protected final int[] scale = new int[] { 2, 1, 2, 2, 1, 2, 2};
		protected final Accidental[] accidents = 
			new Accidental[] { Accidental.NONE, Accidental.NONE, Accidental.NONE, 
				Accidental.NONE, Accidental.NONE, Accidental.KEY, Accidental.KEY };
		public final Accidental eval(Pitch[] pitchClass, Pitch rootPitch, int noteNumber)
		{
			throw new RuntimeException("Not implemented");
		}
	}	
		
	
	
	
}
