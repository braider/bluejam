/**
 * This package contains classes relating to the Markov models
 * used for Initialization of NoteSequences in BlueJam. BlueJam
 * uses these models to bias the random selection of notes
 * in favour of clustering particular notes together, rather
 * than selecting them from an even distribution.
 * 
 * Please see additional documentation for a description of the
 * model form (or just look in the sample .m files).
 */
package net.parallaxed.bluejam.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;

/**
 * The PitchModel class represents a 1-order Markov model 
 * describing a probability matrix of Pitches. The model is a 
 * lookup table that will return P(X|Y), where X and Y are a set 
 * of notes in a given context. For more complex models, this 
 * class could be written to include support for cases where Y 
 * is a series of notes, increasing the order of the model
 * up to the number of notes in Y. 
 * 
 * Models must be locked after loading before use, such that no further
 * changes can be made to a model, and the model is deemed valid.
 * 
 * Please see additional documentation and the technical report
 * for information regarding pitch models.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class PitchModel extends HashMap<Pitch, double[]>{
	private static final String E_NOT_EDITABLE = "This model is not editable";
	private static final String E_MODEL_INCONSISTENT = "WARNING: Model inconsistent";
	private String _name = "Model"+Math.round(Math.random()*100);
	private static final long serialVersionUID = 1L;
	private boolean _editable = true;
	private ArrayList<Pitch> _pitchOrder = new ArrayList<Pitch>();
	private PitchModel _original = null;
	
	/**
	 * Creates a PitchModel instance.
	 * @param name
	 */
	public PitchModel(String name)
	{
		_name = name;
	}
	
	/**
	 * Adds a single pitch, giving the probability series for other
	 * notes in the model following that pitch. This function will
	 * replace any existing pitches matching the passed pitch. The model
	 * must be editable to make changes.
	 * 
	 * @param pitch The pitch to add.
	 * @param model The matrix of probabilities to append to the model for that pitch.
	 * @return The model matrix that was replaced, if any.
	 */
	public double[] put(Pitch pitch, double[] model)
	{
		if (_editable)
		{
			_pitchOrder.add(pitch);
			return super.put(pitch,model);
		}
		ErrorFeedback.handle(E_NOT_EDITABLE, new Exception(E_NOT_EDITABLE));
		return null;
	}
	
	/**
	 * Puts a Map of notes into the model. 
	 * @param m The map to add to the model
	 * @return The last model replaced, if any.
	 */
	public double[] putAll(Map<Pitch,double[]> m)
	{
		if (_editable)
			return putAll(m);
		ErrorFeedback.handle(E_NOT_EDITABLE, new Exception(E_NOT_EDITABLE));
		return null;
	}
		
	private void  _validateModel() throws Exception
	{
		if (_original == null)
			_original = (PitchModel) this.clone();
		_original._editable = false;
		
		Set<Pitch> pitches = keySet();
		Iterator<Pitch> i = pitches.iterator();
		int length = -1;
				
		while (i.hasNext())
		{
			Pitch p = i.next();
			double[] probabilities = _original.get(p);
			if (length > -1) {
				if (probabilities.length != length)
					throw new Exception("Model length inconsistent");
			}
			else {
				length = probabilities.length;							
			}
			double[] _aggProbabilities = new double[probabilities.length];
			double _aggregate = 0;
			for (int n = 0; n < probabilities.length; n++)
			{
				_aggregate += probabilities[n];
				_aggProbabilities[n] = _aggregate;					
			}
			if (_aggregate == 1.0)
				super.put(p,_aggProbabilities);
			else 
				throw new Exception("Definition for "+p.toString()+" does not total 1.0");
		}
		
		_editable = false;
	}
	
	/**
	 * Protected call to the internal _validateModel()
	 * 
	 * This method locks the model if validation is successful.
	 */
	public void  validateModel()
	{
		try {
			if (_editable)
				_validateModel();
		}
		catch (Exception e) {
			ErrorFeedback.handle(E_MODEL_INCONSISTENT + "- "+_name, e);
		}
	}
	
	/**
	 * @param key The pitch to return the model for.
	 * @return The model for the given pitch.
	 */
	public double[] get(Pitch key)
	{
		if (_editable)
			validateModel();
		
		return super.get(key);
	}
	/**
	 * Pitch order is preserved when adding to the model. Models 
	 * remain square matrices, for every new pitch, a new row and
	 * new column is appended to the table.
	 * 
	 * @param index The index sought.
	 * @return The pitch at the passed index of the model
	 */
	public Pitch getPitch(int index) {
		return _pitchOrder.get(index);
	}
}
