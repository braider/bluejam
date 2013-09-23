package net.parallaxed.bluejam;

import java.util.HashMap;

/**
 * This enumeration represents the pitches and can be used to
 * determine enharmonic equivalence between pitch classes.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public enum Pitch {
	C,Cs,Db,D,Ds,Eb,E,F,Fs,Gb,G,Gs,Ab,A,As,Bb,B,
	/**
	 * R is a special kind of parameter, defining relative pitch. 
	 * It is used when loading up heuristic trees. 
	 * 
	 * Notes can be locked at intervals from a given root pitch and,
	 * scale, but these are not known until the Heuristic is paired 
	 * with an individual in a population and initialized.
	 */
	R;
	private static HashMap<String, Pitch>pitchString = new HashMap<String, Pitch>();
	private static HashMap<Pitch, String>stringPitch = new HashMap<Pitch, String>();
	static { 		
		Pitch.pitchString.put("C",Pitch.C);
		Pitch.pitchString.put("Cs",Pitch.Cs);
		Pitch.pitchString.put("Db",Pitch.Db);
		Pitch.pitchString.put("D",Pitch.D);
		Pitch.pitchString.put("Ds", Pitch.Ds);
		Pitch.pitchString.put("Eb",Pitch.Eb);
		Pitch.pitchString.put("E",Pitch.E);
		Pitch.pitchString.put("F",Pitch.F);
		Pitch.pitchString.put("Fs", Pitch.Fs);
		Pitch.pitchString.put("Gb",Pitch.Gb);
		Pitch.pitchString.put("G",Pitch.G);
		Pitch.pitchString.put("Gs", Pitch.Gs);
		Pitch.pitchString.put("Ab",Pitch.Ab);
		Pitch.pitchString.put("A",Pitch.A);
		Pitch.pitchString.put("As", Pitch.As);
		Pitch.pitchString.put("Bb",Pitch.Bb);
		Pitch.pitchString.put("B",Pitch.B);
		Pitch.pitchString.put("R", Pitch.R);
		
		Pitch.stringPitch.put(Pitch.C, "C");
		Pitch.stringPitch.put(Pitch.Cs, "Cs");
		Pitch.stringPitch.put(Pitch.Db, "Db");
		Pitch.stringPitch.put(Pitch.D, "D");
		Pitch.stringPitch.put(Pitch.Ds, "Ds");
		Pitch.stringPitch.put(Pitch.Eb, "Eb");
		Pitch.stringPitch.put(Pitch.E, "E");
		Pitch.stringPitch.put(Pitch.F, "F");
		Pitch.stringPitch.put(Pitch.Fs, "Fs");
		Pitch.stringPitch.put(Pitch.Gb, "Gb");
		Pitch.stringPitch.put(Pitch.G, "G");
		Pitch.stringPitch.put(Pitch.Gs, "Gs");
		Pitch.stringPitch.put(Pitch.Ab, "Ab");
		Pitch.stringPitch.put(Pitch.A, "A");
		Pitch.stringPitch.put(Pitch.As, "As");
		Pitch.stringPitch.put(Pitch.Bb, "Bb");
		Pitch.stringPitch.put(Pitch.B, "B");
		Pitch.stringPitch.put(Pitch.R, "R");
	}
	private Pitch()
	{
		
	}
	/**
	 * Determines enharmonic equivalence a given pitch.
	 * 
	 * @param p The pitch to compare with
	 * @return true if in the same pitch class i.e. (Cs,Db) = true.
	 */
	public final boolean equals(Pitch p) 
	{
		if (p == this)
			return true;
		if (this == Pitch.Cs && p == Pitch.Db)
			return true;
		if (this == Pitch.Ds && p == Pitch.Eb)
			return true;
		if (this == Pitch.Fs && p == Pitch.Gb)
			return true;
		if (this == Pitch.Gs && p == Pitch.Ab)
			return true;
		if (this == Pitch.As && p == Pitch.Bb)
			return true;
		return false;
	}
	
	/**
	 * Evaluates which accidental is present on a given pitch.
	 * @return Accidental.SHARP for all Pitch.Xs and Accidental.FLAT for all Pitch.Xb
	 */
	public final Accidental eval()
	{
		if (this == Cs || this == Ds || this == Fs || this == Gs || this == As)
			return Accidental.SHARP;
		
		if (this == Db || this == Eb || this == Gb || this == Ab || this == Bb)
			return Accidental.FLAT;
		
		return Accidental.NONE;
	}
	
	public static final Pitch getPitch(String pitch)
	{		
		return Pitch.pitchString.get(pitch);
	}
	
	public static final String getName(Pitch pitch)
	{
		return stringPitch.get(pitch);
	}
}
