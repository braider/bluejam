package net.parallaxed.bluejam;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import ec.util.MersenneTwisterFast;

/**
 * Provides a class to store population heuristics and select them
 * using different modes/distributions. 
 * 
 * Calling selectHeuristic() returns a heuristic from the collection.
 * 
 * This class uses the ECJ implementation of MersenneTwisterFast.
 * 
 * @see MersenneTwisterFast
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class HeuristicCollection extends ArrayList<Heuristic> 
{
	
	private static final long serialVersionUID = 1L;
	/**
	 * This Enum provides defines the possible methods by which 
	 * heuristics are selected from the collection
	 * 
	 * @author Ciarán Rowe (csr2@kent.ac.uk)
	 */
	
	public static enum SELECTION_TYPE {
	/**
	 * An even distribution will iterate over the contents of the
	 * collection, then return to normal, so each heuristic
	 * is selected evenly.
	 */
	EVEN,
	/**
	 * A random selection uses an instance of MersenneTwisterFast to 
	 * pick out a heuristic.
	 */
	RANDOM
	}
	
	// used by SELECTION_TYPE.EVEN
	private int _index = 0;
	// used by SELECTION_TYPE.RANDOM
	private MersenneTwisterFast _mt = net.parallaxed.bluejam.util.MersenneTwisterFast.getInstance();
	
	/**
	 * Specifies which selection type to use in this collection.
	 * @see SELECTION_TYPE
	 */
	public SELECTION_TYPE MODE = SELECTION_TYPE.EVEN;
	
	private HashMap<String,Heuristic> _byName = new HashMap<String, Heuristic>(); 
	
	/**
	 * Initialises a HeuristicCollection (trivial)
	 */
	public HeuristicCollection()
	{
		super();		
	}
	
	public boolean add(Heuristic heuristic)
	{		
		return add(heuristic,heuristic.toString());	
	}
	
	public boolean add(Heuristic heuristic, String name)
	{
		if (!_byName.containsKey(name))
		{
			super.add(heuristic);		
			_byName.put(name, heuristic);
			return true;
			
		}
		return false;
		
	}
	
	/**
	 * Returns a heuristic by name if it is present in the collection.
	 * @param name The name of the desired heuristic
	 * @return A reference to the named heuristic, or null.
	 */
	public Heuristic getHeuristic(String name)
	{
		return _byName.get(name);
	}
	
	/**
	 * Returns a heuristic in line with the method selected
	 * by MODE.
	 * 
	 * @see SELECTION_TYPE
	 * @return A heuristic from the collection
	 */
	public Heuristic selectHeuristic()
	{
		switch (MODE) {
			case EVEN:
				return getEvenHeuristic();
			case RANDOM:
				return getRandomHeuristic();				
		}
		throw new RuntimeException("Invalid Selection Mode");
	}
	
	public static HeuristicCollection loadHeuristics(String path) {
		try {
			/*
			 * Collect heuristics and models.
			 */
			HeuristicCollection hCollection = new HeuristicCollection();
			File dir = new File(path);
			
			System.out.println("Looking for heuristics in: "+ dir.getCanonicalPath());
			File[] files = dir.listFiles();
			
			for (File f : files)
			{
				String name = f.getName();
				if (name.contains(".heuristic"))
					if (f.canRead())
						hCollection.add(new TreeParser(f).getNoteTree());
			}

			return hCollection;

		}
		catch (Exception e) {
			ErrorFeedback.handle(e.getMessage(), e);
		}		
		return null;
	}
	
	private Heuristic getRandomHeuristic()
	{
		return get(_mt.nextInt(size()));
	}

	private Heuristic getEvenHeuristic()
	{
		if (_index >= size())
			_index = 0;
		return this.get(_index++);	
	}
}
