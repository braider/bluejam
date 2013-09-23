package net.parallaxed.bluejam.playback;

import java.util.HashMap;

import net.parallaxed.bluejam.Accidental;
import net.parallaxed.bluejam.playback.MIDI;
import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.Pitch;

/**
 * This is a static class used primarily for converting
 * internal note representations into MIDI numbers
 * and vice versa.
 * 
 * This class can discover if a particular pitch is relatively
 * higher or lower than another given pitch, in the context
 * of a single MIDI defined octave
 * 
 * (this does not work for notes spanning multiple octaves
 * ...yet)
 * 
 * These values can be used at playback time.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public final class MIDI 
{
	/**
	 * Defines the pitches relative to C, as per MIDI standard.
	 * 
	 * @see http://www.midi.org/about-midi/specshome.shtml
	 */
	private final static HashMap<Integer,Pitch> pitchRelative = new HashMap<Integer,Pitch>();
	private final static HashMap<Pitch,Integer> numberRelative = new HashMap<Pitch,Integer>();
	private final static Pitch[][] pitchIndex = new Pitch[12][];
	
	static {
		pitchRelative.put(0, Pitch.C);
		pitchRelative.put(1, Pitch.Db);
		pitchRelative.put(2, Pitch.D);
		pitchRelative.put(3, Pitch.Eb);
		pitchRelative.put(4, Pitch.E);
		pitchRelative.put(5, Pitch.F);
		pitchRelative.put(6, Pitch.Gb);
		pitchRelative.put(7, Pitch.G);
		pitchRelative.put(8, Pitch.Ab);
		pitchRelative.put(9, Pitch.A);
		pitchRelative.put(10, Pitch.Bb);
		pitchRelative.put(11, Pitch.B);
		
		numberRelative.put(Pitch.C, 0);
		numberRelative.put(Pitch.Db, 1);
		numberRelative.put(Pitch.Cs, 1);
		numberRelative.put(Pitch.D, 2);
		numberRelative.put(Pitch.Ds, 3);
		numberRelative.put(Pitch.Eb, 3);
		numberRelative.put(Pitch.E, 4);
		numberRelative.put(Pitch.F, 5);
		numberRelative.put(Pitch.Fs, 6);
		numberRelative.put(Pitch.Gb, 6);
		numberRelative.put(Pitch.G, 7);
		numberRelative.put(Pitch.Gs, 8);
		numberRelative.put(Pitch.Ab, 8);
		numberRelative.put(Pitch.A, 9);
		numberRelative.put(Pitch.As, 10);
		numberRelative.put(Pitch.Bb, 10);
		numberRelative.put(Pitch.B, 11);	

		pitchIndex[0] = new Pitch[1];
		pitchIndex[0][0] = Pitch.C;
		pitchIndex[1] = new Pitch[2];
		pitchIndex[1][0] = Pitch.Cs;
		pitchIndex[1][1] = Pitch.Db;
		pitchIndex[2] = new Pitch[1];
		pitchIndex[2][0] = Pitch.D;
		pitchIndex[3] = new Pitch[2];
		pitchIndex[3][0] = Pitch.Ds;
		pitchIndex[3][1] = Pitch.Eb;
		pitchIndex[4] = new Pitch[1];
		pitchIndex[4][0] = Pitch.E;
		pitchIndex[5] = new Pitch[1];
		pitchIndex[5][0] = Pitch.F;
		pitchIndex[6] = new Pitch[2];
		pitchIndex[6][0] = Pitch.Fs;
		pitchIndex[6][1] = Pitch.Gb;
		pitchIndex[7] = new Pitch[1];
		pitchIndex[7][0] = Pitch.G;
		pitchIndex[8] = new Pitch[2];
		pitchIndex[8][0] = Pitch.Gs;
		pitchIndex[8][1] = Pitch.Ab;
		pitchIndex[9] = new Pitch[1];
		pitchIndex[9][0] = Pitch.A;
		pitchIndex[10] = new Pitch[2];
		pitchIndex[10][0] = Pitch.As;
		pitchIndex[10][1] = Pitch.Bb;
		pitchIndex[11] = new Pitch[1];
		pitchIndex[11][0] = Pitch.B;	
	}
	
	/**
	 * Currently used by the Configure class in the PD
	 * implementation to tell what note is being set as the
	 * root pitch.
	 * 
	 * @see Note
	 * @param noteNumber
	 * @return A Note instance configured to reflect the passed number.
	 */
	public static Note numberToNote(float noteNumber)
	{
		float note = noteNumber;
		int octave = -1;
		
		// Subtract 12 until less than 12. Incrementing octave each time
		// gives us the right range
		// This leaves us with the offset index, relative to C.
		while (note >= 12) {			
			note = note - 12;
			octave++;
		} 		
		return new Note(pitchRelative.get(((Float)note).intValue()),octave);
	}
	
	/**
	 * This method will return a MIDI number for the given 
	 * note. This method DOES NOT validate that MIDI number
	 * (should be 0 <= n <= 127).
	 * 
	 * @param n The note to calculate for.
	 * @return A MIDI number for the note.
	 */
	public static float noteToNumber(Note n)
	{		
		// Octave must be valid.
		Pitch p = n.pitchClass();
		if (p == Pitch.R)
			p = n.evaluatedPitch();
		
		float noteOffset = numberRelative.get(p);
		int o = -1;
		int octave = n.octave(); 
		// Count up the octaves until we're in the right one.
		// Multiply the number of octave jumps by 12 (notes in an octave)
		while (o < octave)
			o++;
				
		// Add the offset to this number.
		return (o * 12 + noteOffset);
	}
	
	public static class LESS_THAN
	{
		public static boolean eval(Pitch pitch1, Pitch pitch2)	{
			return numberRelative.get(pitch1) < numberRelative.get(pitch2);
		}		
	};
	
	public static class GREATER_THAN 
	{ 
		public boolean eval(Pitch pitch1, Pitch pitch2) {
			return numberRelative.get(pitch1) > numberRelative.get(pitch2);
		}
	}
	
	public static Pitch relative(Pitch rootPitch, int relativeIndex)
	{
		if ((relativeIndex % 12) == 0)
			return rootPitch;

		if (relativeIndex < 0)
			relativeIndex = 12-relativeIndex;
		
		int index = numberRelative.get(rootPitch) + relativeIndex;
		// Count up the scale to try and find the pitch.
		relativeIndex  = (index > 11 ? index - 12 : index);
		// Return the pitch according to the root's accidental
		if (pitchIndex[relativeIndex].length > 1) {
			if (rootPitch.eval() == Accidental.FLAT)
				return pitchIndex[relativeIndex][1];			
		}
		return pitchIndex[relativeIndex][0];		
	}
	
	public static int position(Pitch pitch1)
	{
		return numberRelative.get(pitch1);
	}
}
