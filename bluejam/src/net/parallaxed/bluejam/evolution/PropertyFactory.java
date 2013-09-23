package net.parallaxed.bluejam.evolution;

/**
 * Defines which class sets properties for an Individual.
 * 
 * Other implementations may be devised and placed in the enum.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 * @deprecated
 */
public enum PropertyFactory {
	
	RHYTHM_INITIALIZER(RhythmInitializer.class);
	
	private Class<?> _initImpl = null;
	
	PropertyFactory(Class<?> implementation) {
		_initImpl = implementation;
	}
	
	/**
	 * Returns the implementing class.
	 * @return
	 */
	public Class<?> eval() {
		return _initImpl;
	}
}
