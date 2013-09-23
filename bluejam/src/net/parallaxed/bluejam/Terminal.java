package net.parallaxed.bluejam;

/**
 * All terminals must have unique identifiers to satisfy
 * uniqueness in a TerminalSet.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface Terminal extends Cloneable
{
	/**
	 * Returns this terminal's value.
	 * @return A unique identifier assigned by the implementing object.
	 */
	
	public int getValue();
}
