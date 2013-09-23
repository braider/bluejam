package net.parallaxed.bluejam.evolution;

import net.parallaxed.bluejam.NoteTree;

/**
 * Names and stored reference to the underlying implementations
 * of Genomes available in the system.
 * 
 * The default is a NoteTree.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public enum Genotype {
	
	/**
	 * The default genome implementation.
	 */
	NOTE_TREE(NoteTree.class);
	
	private Class<?> _genotypeImpl = null;
	
	/**
	 * Stores a genome representation and a reference to it's
	 * implementing class.
	 * 
	 * Support for new classes has to be compiled, but it's a
	 * quick and easy job.
	 * 
	 * @param implementation A reference to the class of the implementation.
	 */
	Genotype(Class<?> implementation) {
		_genotypeImpl = implementation;
	}
	
	/**
	 * Returns a refernece to the class for the implementing
	 * genome type. This type *must* implement NoteSequence, or
	 * undefined behaviour will occur.
	 * @return A parameterizable Class instance for the enumerable genotype.
	 */
	public Class<?> eval() {
		return _genotypeImpl;
	}
}
