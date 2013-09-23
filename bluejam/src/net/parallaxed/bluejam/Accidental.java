package net.parallaxed.bluejam;

/**
 * Defines the possible accidentals.
 * 
 * KEY is a special accidental remarking that the accidental
 * should be the same as that of the root pitch.
 * 
 * This class can be used to mark accidentals in relation to
 * scale, but also in relation to a particular note. In null 
 * cases, the null accidental (NONE) is returned.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public enum Accidental {
	SHARP, FLAT, NATURAL, 
	/**
	 * The KEY accidental implies that if the note
	 * occurs inside a pitch class, the accidental
	 * should be the same as the accidental of the
	 * root pitch in that scale.
	 */
	KEY, 
	/**
	 * The NONE accidental is returned when the note
	 * is not inside a pitch class (on it's own). When used to 
	 * evaluate the presence of an accidental in a scale, 
	 * it behaves in a similar way to KEY, but in the case
	 * of a natural present on the note, NONE should always
	 * bias itself to remove the accidental on that NOTE.
	 */
	NONE;
}
