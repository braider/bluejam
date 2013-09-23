package net.parallaxed.bluejam.evolution;

import java.util.HashMap;

/**
 * This class follows a generic parameter pattern where the
 * user can pass in parameters for the selection algorithm.
 * 
 * In the default BlueJam implementation this is only used to
 * pass in TournamentSize to SelectTournament.
 * 
 * @see SelectTournament
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */

public class SelectionParameters extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	
	public SelectionParameters()
	{
		
	}
}
