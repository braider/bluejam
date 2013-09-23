package net.parallaxed.bluejam;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import ec.util.MersenneTwisterFast;

/**
 * Wraps an ArrayList to provide set-like functionality 
 * (one unique instance per set).
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public abstract class TerminalSet extends AbstractSet<Terminal>
{
	private static MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	private ArrayList<Terminal> terminals = new ArrayList<Terminal>();
	
	/**
	 * Returns an Iterator over the elements in this TerminalSet.
	 * @see java.util.AbstractCollection#iterator()
	 */
	public Iterator<Terminal> iterator() {
		return terminals.iterator();
	}
	
	/**
	 * @return A random terminal from the set.
	 */
	public Terminal getRandom() {
		return terminals.get(_mt.nextInt(terminals.size()));
	}
	
	/**
	 * @return True if this set contains the passed Object
	 * @param o The Terminal to search for in this TerminalSet
	 */
	public boolean contains(Object o)
	{
		if (super.contains(o))
			return true;
		
		int terminalValue = 0;
		try {
			Terminal t = (Terminal) o;
			terminalValue = t.getValue();
		}
		catch (Exception e) { return false; }
				
		Iterator<Terminal> iter = this.iterator();		 
		while (iter.hasNext())	{
			if (iter.next().getValue() == terminalValue)
				return true;
		}
		return false;
	}
	
	/**
	 * Returns false if either the terminal supplied is null
	 * or the collection already contains the terminal.
	 * 
	 * Otherwise, adds the item to the collection.
	 */
	@Override
	public boolean add(Terminal terminal) 
	{	
		if (terminal != null)
			if (!this.contains(terminal))
			{
				terminals.add(terminal);
				return true;
			}
		
		return false;
	}
	/**
	 * @return a terminal based on it's index in the set
	 * @param index The index at which to look for the terminal. 
	 *
	 */
    public Terminal get(int index)  {
    	return terminals.get(index);
    }
    
    /**
     * @return The number of terminals in this set.
     */
	public int size() {
		return terminals.size();
	}
}
