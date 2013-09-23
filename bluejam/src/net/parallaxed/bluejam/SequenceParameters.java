package net.parallaxed.bluejam;

/**
 * Currently, custom tree parameters aren't supported, so this
 * default instantiation should cover the basics.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class SequenceParameters 
{
	/**
	 * Has this SequenceParameters object been changed recently?
	 */	
	public volatile boolean Changed = false;
	
	/**
	 * The JamParameters for the current sequence.
	 * TODO Work on dynamic changing at runtime.
	 */
	public JamParamters Jam = null;
	
	/**
	 * The number of whole notes in this Sequence.
	 * 
	 * i.e. 4 whole notes = 4 bars, so length=4
	 */
	public int length = 4;
	/////
	private int _beatUnit = 4;
	
	/**
	 * The beat unit is the denominator of the time signature.
	 * @return The note we measuring in (usually quarter-note, or Rhythm.CROTCHET)
	 */
	public int beatUnit() { return _beatUnit; }
	//////
	
	//////
	private int _beatCount = 4;
	/**
	 * The nominator of the time signature - the number of beat
	 * units per bar.
	 * @return The number of beats per beat unit
	 */
	public int beatCount() { return _beatCount; }
	//////

	private String _timeSignature = "4/4";
	/**
	 * Time signature is parsed to set the private variables
	 */
	public void timeSignature(String timeSignature)
	{		
		try { 
			int sep = timeSignature.indexOf('/');
			_beatCount = Integer.parseInt(timeSignature.substring(0,sep++));
			_beatUnit = Integer.parseInt(timeSignature.substring(sep));
			_timeSignature = _beatCount +"/" + _beatUnit;			
		}
		catch (Exception e)	{
			throw new RuntimeException("Invalid time signature: "+timeSignature);
		}
	}
	
	/**
	 * @return The time signature of the sequence in string "x/y" format
	 */
	public String timeSignature() { return _timeSignature; }
	/**
	 * Initializes a set of SequenceParameters with the passed JamParameters 
	 * @param jam The JamParameters defining rootPitch, scale, etc.
	 */
	public SequenceParameters(JamParamters jam) { 
		Jam = jam;
	}
	
	/**
	 * Trivial SequenceParameters constructor.
	 */
	public SequenceParameters() { }
	
	
}
