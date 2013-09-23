package net.parallaxed.bluejam;
import java.io.File;
import java.util.HashMap;

/**
 * The ParameterCollection class is used by BlueJam to specify 
 * the properties of a cycle of evolution. 
 * 
 * Each ParameterCollection is passed down to a population, which
 * uses it to configure it's individuals. Parameter collections are
 * immutable, since the individuals need access to read the
 * values, but are prohibited from changing them.
 * 
 * Separate configurations may evolve in different populations
 * simultaneously to produce different solos.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class JamParamters 
{	
	int _minOctave = 5;
	public int minOctave() { return _minOctave; }
	
	int _maxOctave = 6;
	public int maxOctave() { return _maxOctave; }
	
	/**
	 * Defines the typed configuration values in a ParameterCollection
	 */
	public enum Config {
		/**
		 * (Pitch) The root pitch around which to evolve.
		 * Default = A440
		 */
		ROOT_PITCH, 
		/**
		 * (Integer) The tempo (in beats per minute - BPM).
		 * Default = 120bpm
		 */		
		TEMPO,
		
		/**
		 * (Scale) One of the supported "Scales" enumeration values.
		 * Default = Blues.
		 */
		SCALE;
		
		/**
		 * The string value of each enum.
		 */
		public String toString()
		{
			switch (this) {
				case ROOT_PITCH: return "rootPitch";
				case TEMPO: return "tempo";
				case SCALE: return "scale";
			}
			return null;
		}
	}
	
	// The store for typed configuration values.
	private HashMap<Config,Object> params = new HashMap<Config, Object>();
	
	// The store for untyped configuration values.
	private HashMap<String,String> stringParams = new HashMap<String,String>();
	
	private ScaledSet _scale = null;
	/**
	 * Initialises a parameter collection with defaults.
	 * 
	 */
	public JamParamters()
	{		
		this(Pitch.A, Scale.BLUES.getInstance(), 120);
	}
	/**
	 * Initializes a Jam with the given parameterss
	 * @param pitch The root pitch (one of the Pitch enum)
	 * @param scale The scale to use (one of the Scale enum)
	 * @param tempo An integer representing the beats per minute (BPM)
	 */
	public JamParamters(Pitch pitch, Scale scale, Integer tempo)
	{
		params.put(Config.ROOT_PITCH, pitch);
		params.put(Config.SCALE, scale);
		params.put(Config.TEMPO, tempo);
		_buildScale();
	}
	
	/**
	 * Sets the range of the Jam, in octaves.
	 * 
	 * Both values must be -1 < x < 10.
	 * 
	 * Also, (minOctave <= maxOctave) must be true, otherwise
	 * no action will be taken.
	 * 
	 */
	public void setRange(int minOctave, int maxOctave)
	{
		if (minOctave > -1 && minOctave < 10)
			if (maxOctave > -1 && maxOctave < 10)
				if (maxOctave >= minOctave)
				{
					_minOctave = minOctave;
					_maxOctave = maxOctave;
				}
		_buildScale();		
	}
	
	/**
	 * Reads in the supplied file and configures the ParameterCollection
	 * 
	 * @param file A java.io.File object to read.
	 */
	public void readConfig(File file)
	{
		if (!file.canRead())
			throw new RuntimeException("Can't read supplied config file");		
	}
	/**
	 * Reads in a config file at the specified (absolute) path.
	 *
	 * @param path The path to the config file.
	 */
	public void readConfig(String path)
	{
		try {			
			readConfig(new File(path));
		}
		catch (Exception e) {
			throw new RuntimeException("Can't read supplied config file");
		}
	}
	

	////// String parameters
	/**
	 * Retrieves a parameter stored in stringParams, or throws
	 * an exception if the parameter is not found.
	 * 
	 * @param s The parameter name to get
	 * @return The value of parameter s
	 * @throws Exception
	 */
	public String getParameter(String s) throws Exception
	{
		String value = stringParams.get(s);
		if (value == null)
			throw new Exception("String Parameter Not Found");
		
		return value;
	}
	
	public void setParameter(String s, String value) throws Exception
	{
		stringParams.remove(s);
		stringParams.put(s,value);
	}
	//////
	
	////// Typed parameters
	/**
	 * This method retrieves a typed parameter from the collection.
	 * 
	 * @param c The configuration key to return
	 * @return An untyped object that can be casted to an expected type
	 * @throws Exception If the parameter does not exist in this ParameterCollection
	 */
	public Object getParameter(Config c) throws RuntimeException
	{
		Object value = params.get(c);
		if (value == null)
			throw new RuntimeException ("Parameter Not Found");
		
		return value;
	}
	
	/**
	 * Returns the root pitch of this configuration as a type
	 * Pitch.
	 * 
	 * @return The root Pitch.
	 */
	public Pitch rootPitch()
	{
		try {
			return (Pitch) getParameter(Config.ROOT_PITCH); }
		catch (Exception e) {		
			throw new RuntimeException("Pitch Not Set",e); }		
	}

	/**
	 * Sets the root pitch of this Jam
	 * @param rootPitch The rootPitch of the Jam (cannot be Pitch.R)
	 */
	public void rootPitch(Pitch rootPitch) 
	{
		if (rootPitch == null || rootPitch == Pitch.R)
			return;

		params.put(Config.ROOT_PITCH, rootPitch);	
		_buildScale();
	}
	
	/**
	 * Sets the tempo of this Jam
	 * @param tempo A BPM value between 1-240
	 */
	public void tempo(int tempo) 
	{
		if (tempo < 1 || tempo > 240)
			return;
		params.remove(Config.TEMPO);
		params.put(Config.TEMPO,new Integer(tempo));
	}
	
	/**
	 * @return The scale being used in this Jam
	 */
	public Scale scale() {
		return (Scale)  getParameter(Config.SCALE);
	}
	
	/**
	 * Sets the scale of this Jam
	 * @param scale A reference to the singleton Scale instance.
	 */
	public void scale(Scale scale)
	{
		if (scale == null)
			return;
				
		params.put(Config.SCALE, scale);
		_buildScale();
	}

	//////
	
	/**
	 * Terminals for the current Jam can be pulled from this
	 * ScaledSet.
	 * @return A reference to this JamParameters ScaledSet.
	 */
	public ScaledSet getScaledSet()	{
		return _scale;
	}
	
	private void _buildScale() {
		_scale = new ScaledSet(rootPitch(),scale(),_minOctave,_maxOctave);
	}
}
