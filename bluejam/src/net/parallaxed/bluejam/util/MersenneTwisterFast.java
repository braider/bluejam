package net.parallaxed.bluejam.util;

/**
 * Singleton wrapper for ec.util.MersenneTwisterFast
 * FIXME Update to use MersenneTwister class, since MersenneTwisterFast is not threadsafe.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class MersenneTwisterFast {
	
	public static ec.util.MersenneTwisterFast _instance = null;
	private MersenneTwisterFast() {
		
	}
	public static ec.util.MersenneTwisterFast getInstance()
	{
		if (_instance == null)
			_instance = new ec.util.MersenneTwisterFast();
		return _instance;		
	}
}
