package net.parallaxed.bluejam;

import net.parallaxed.bluejam.evolution.FitnessType;
import net.parallaxed.bluejam.evolution.Genotype;
import net.parallaxed.bluejam.evolution.HeuristicSelectionType;
import net.parallaxed.bluejam.evolution.InitializationType;

/**
 * If the individual desires different parameters from those
 * defined in the population by default, it can override the
 * population's decision by creating a class of 
 * IndividualParameters.
 * 
 * This only works for some parameters.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class IndividualParameters extends PopulationParameters {
	
	/**
	 * A reference to our PopuplationParameters instance.s
	 */
	private PopulationParameters _popParams = null;
	
	/**
	 * If and individual can't resolve a parameter, it will 
	 * retrieve it from the parent.
	 * 
	 * @param params
	 */
	public IndividualParameters(PopulationParameters params) {
		_popParams = params;
	}	
	
	/**
	 * Ensures that only parameters specific to individuals can
	 * be overridden.
	 */
	protected boolean _checkType(String name, Object value) {
		if (name == INITIALIZATION_TYPE)
			return (value instanceof InitializationType);
		if (name == FITNESS_TYPE)
			return (value instanceof FitnessType);
		if (name == HEURISTIC_SELECTION_TYPE)
			return (value instanceof HeuristicSelectionType);
		if (name == GENOTYPE)
			return (value instanceof Genotype);
		return false;

	}
	/**
	 * If the value is not found in the individual parameters,
	 * will return them from the parent. 
	 */
	@Override
	public Object getParameter(String name) {
		Object value = null;
		if ((value = _parameters.get(name)) != null)
			return value;
		return _popParams.getParameter(name);
	}
}
