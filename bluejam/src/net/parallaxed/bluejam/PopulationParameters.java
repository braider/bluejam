package net.parallaxed.bluejam;

import java.util.HashMap;

import net.parallaxed.bluejam.evolution.FitnessType;
import net.parallaxed.bluejam.evolution.Genotype;
import net.parallaxed.bluejam.evolution.HeuristicSelectionType;
import net.parallaxed.bluejam.evolution.InitializationType;
import net.parallaxed.bluejam.evolution.SelectTournament;

import net.parallaxed.bluejam.evolution.SelectionType;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.ParameterException;

/**
 * Provides a moderately strongly typed parameter collection
 * for the population properties and constants.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class PopulationParameters {
	
	/**
	 * Default SequenceParams
	 */
	public static final String SEQUENCE = "SequenceParameters";
	
	/**
	 * SelectionPressure parameter name.
	 * 
	 * Value of this parameter must be an integer. This also defines the
	 * tournament size (if present).
	 */
	public static final String SELECTION_PRESSURE = "SelectionPressure";
	/**
	 * SelectionType parameter name.
	 */
	public static final String SELECTION_TYPE = "SelectionType";
	/**
	 * SelectionType parameter name.
	 */
	public static final String HEURISTIC_SELECTION_TYPE = "HeuristicSelectionType";
	
	/**
	 * InitializationType parameter name.
	 */
	public static final String INITIALIZATION_TYPE = "InitializationType";

	/**
	 * InitializationType parameter name.
	 */
	public static final String FITNESS_TYPE = "FitnessType";

	
	/**
	 * Genotype (Individual representation) parameter name.
	 */
	public static final String GENOTYPE = "Genotype";
	
	protected HashMap<String,Object> _parameters = new HashMap<String,Object>();
	
	/**
	 * Trivial Constructor
	 */
	public PopulationParameters() { _setDefaults(); }
	
	private void _setDefaults()	{
		try {
			setParameter(PopulationParameters.GENOTYPE, Genotype.NOTE_TREE);
			setParameter(PopulationParameters.HEURISTIC_SELECTION_TYPE,HeuristicSelectionType.RANDOM);
			setParameter(PopulationParameters.INITIALIZATION_TYPE, InitializationType.GROW);
			/// Note: this is also the tournament size
			setParameter(PopulationParameters.SELECTION_PRESSURE, 15);
			setParameter(PopulationParameters.SELECTION_TYPE, SelectionType.TOURNAMENT);
			setParameter(PopulationParameters.FITNESS_TYPE, FitnessType.STACKED);
		}
		catch (Exception e) { ErrorFeedback.handle(e.getMessage(), e);} 
	}
	/**
	 * Sets a parameter using moderately strict type checking.
	 * 
	 * Value and name must both be non-null and supported by the
	 * Parameter collection type (either Individual or Population).
	 * 
	 * @param name The name of the parameter to set
	 * @param value The object value to set the parameter to.
	 * @throws ParameterException
	 */
	public void setParameter(String name, Object value) throws ParameterException {
		if (value == null || name == null || name.length() < 0)
			throw new ParameterException("Name and value must not be null.");
		_parameters.remove(name);
		if (_checkType(name,value))
			_parameters.put(name, value);
		else
			throw new ParameterException("Parameter "+name+" type invalid.");
	}
	
	protected boolean _checkType(String name, Object value) {
		if (name == SEQUENCE)
			return (value.getClass() == SequenceParameters.class);
		if (name == INITIALIZATION_TYPE)
			return (value.getClass() ==  InitializationType.class);
		if (name == FITNESS_TYPE)
			return (value.getClass() == FitnessType.class);
		if (name == SELECTION_TYPE)
			return (value.getClass() ==  SelectionType.class);
		if (name == HEURISTIC_SELECTION_TYPE)
			return (value.getClass() ==  HeuristicSelectionType.class);
		if (name == SELECTION_PRESSURE)
			return (value.getClass() ==  Integer.class);
		if (name == GENOTYPE)
			return (value.getClass() ==  Genotype.class);
		return false;
	}
	
	/**
	 * JamParameters can also be obtained through here.
	 * @return The SequenceParameters instance used in this population.
	 * 
	 */
	public SequenceParameters getSequenceParameters() {
		return (SequenceParameters) getParameter(SEQUENCE);
	}
	/**
	 * @return The SelectionType for this population.
	 * @see SelectionType
	 */
	public SelectionType getSelectionType() {
		return (SelectionType) getParameter(SELECTION_TYPE);
	}
	
	/**
	 * @return The SelectionPressure for this population.
	 * @see SelectTournament
	 */
	public Integer getSelectionPressure() {
		return (Integer) getParameter(SELECTION_PRESSURE);
	}
	
	/**
	 * @return The InitializationType for this population.
	 * @see InitializationType
	 */
	public InitializationType getInitializationType() {
		return (InitializationType) getParameter(INITIALIZATION_TYPE);	
	}
	
	/**
	 * @return The FitnessType for this population.
	 * @see FitnessType
	 */
	public FitnessType getFitnessType() {
		return (FitnessType) getParameter(FITNESS_TYPE);	
	}
	
	/**
	 * 
	 * @return The Genotype for this population
	 * @see Genotype
	 */
	public Genotype getGenotype() {
		return (Genotype) getParameter(GENOTYPE);
	}

	/**
	 * @return An object of the value of that parameter, or null if not found.
	 */
	public Object getParameter(String parameter) {
		return _parameters.get(parameter);
	}
}
