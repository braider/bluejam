package net.parallaxed.bluejam;


/**
 * A heuristic is any class that can provide a pattern template
 * upon which a terminal set can evolve.
 * 
 * In BlueJam, the default implementation is the HeuristicTree.
 * 
 * The heuristic tree is 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public interface Heuristic extends Function, NoteSequence
{	
	/**
	 * This defines a name for the heuristic, such
	 * that a particular instance can be pulled from a
	 * HeuristicCollection by name.
	 * 
	 * Note this does not have to be unique globally, only
	 * within the collection if you wish to address the 
	 * heuristic individually.
	 * 
	 * @return The name of this heuristic.
	 */
	public String toString();
	
	/**
	 * Since NoteSequences should by default have some reference to
	 * sequenceParamaters, and the Genome (i.e. NoteTree) by default accepts this
	 * only in the constructor, we should have some method for
	 * overriding the sequenceParameters after construction on the 
	 * heuristic.
	 */
	public void setSequenceParameters(SequenceParameters sequenceParameters);
}
