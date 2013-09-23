package net.parallaxed.bluejam.tests;

import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.JamParamters;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.TreeParser;
import net.parallaxed.bluejam.evolution.FitnessContour;

import net.parallaxed.bluejam.evolution.FitnessInterval;
import net.parallaxed.bluejam.evolution.FitnessType;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import junit.framework.TestCase;

public class FitnessTestHeuristic extends TestCase {
	
	SequenceParameters _sp = new SequenceParameters(new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120));
	
	
	public void testInterval5() {
		// Should get a really low mark because of all the interval repetition
		NoteTree _nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\blues-5.heuristic").getNoteTree();
		FitnessInterval _fi = FitnessInterval.getInstance();
		PopulationParameters p = new PopulationParameters();
		
		try {
			p.setParameter(PopulationParameters.FITNESS_TYPE, FitnessType.INTERVAL);
			p.setParameter(PopulationParameters.SEQUENCE,_sp);
		}
		catch (Exception e) { ErrorFeedback.handle("Error setting test parameters", e); }
		_nt.setSequenceParameters(_sp);
		Individual i = new Individual(_nt,p);
		
		double fitness = _fi.evaluate(i);
		print(fitness);
		assertTrue(fitness > 0.5);
	}
	
	public void testContour5() {
		// Should get a really low mark because of all the interval repetition
		NoteTree _nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\blues-5.heuristic").getNoteTree();
		FitnessContour _fc = FitnessContour.getInstance();
		PopulationParameters p = new PopulationParameters();
		
		try {
			p.setParameter(PopulationParameters.FITNESS_TYPE, FitnessType.CONTOUR);
			p.setParameter(PopulationParameters.SEQUENCE,_sp);
		}
		catch (Exception e) { ErrorFeedback.handle("Error setting test parameters", e); }
		_nt.setSequenceParameters(_sp);
		Individual i = new Individual(_nt,p);
		
		double fitness = _fc.evaluate(i);
		print(fitness);
		assertTrue(fitness > 0.5);
	}
	
	public void print(double fitness)
	{
		System.out.println("Fitness: "+fitness);
	}
	
}
