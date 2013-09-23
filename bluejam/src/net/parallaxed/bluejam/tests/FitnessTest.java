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
import net.parallaxed.bluejam.evolution.FitnessDistance;
import net.parallaxed.bluejam.evolution.FitnessInterval;
import net.parallaxed.bluejam.evolution.FitnessType;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import junit.framework.TestCase;

public class FitnessTest extends TestCase {
	
	SequenceParameters _sp = new SequenceParameters(new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120));
	
	
	public void testDistanceOK() {
		NoteTree _nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\fitnessTestContour-1.tree").getNoteTree();
		NoteTree _nt2 = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\fitnessTestContour-2.tree").getNoteTree();
		PopulationParameters p = new PopulationParameters();
		FitnessDistance _fd = FitnessDistance.getInstance();
				
		try {
			p.setParameter(PopulationParameters.FITNESS_TYPE, FitnessType.DISTANCE);
			p.setParameter(PopulationParameters.SEQUENCE,_sp);
		}
		catch (Exception e) { ErrorFeedback.handle("Error setting test parameters", e); }
		Individual i = new Individual(_nt, p,_nt2);
		assertTrue(_fd.evaluate(i) > 0.4);
		
	}
	/**
	 * Test against an Individual with a heuristic identical to the sequence.
	 * 
	 * TODO Test against "best case" scenario.
	 */
	public void testDistanceBad() {
		NoteTree _nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\fitnessTestContour-1.tree").getNoteTree();
		PopulationParameters p = new PopulationParameters();
		FitnessDistance _fd = FitnessDistance.getInstance();
				
		try {
			p.setParameter(PopulationParameters.FITNESS_TYPE, FitnessType.DISTANCE);
			p.setParameter(PopulationParameters.SEQUENCE,_sp);
		}
		catch (Exception e) { ErrorFeedback.handle("Error setting test parameters", e); }
		Individual i = new Individual(_nt,p,_nt);
		
		
		assertEquals(0d,_fd.evaluate(i));
		
	}
	
	public void testIntervalBad() {
		// Should get a really low mark because of all the interval repetition
		NoteTree _nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\fitnessTestContour-1.tree").getNoteTree();
		FitnessInterval _fi = FitnessInterval.getInstance();
		PopulationParameters p = new PopulationParameters();
		
		try {
			p.setParameter(PopulationParameters.FITNESS_TYPE, FitnessType.CONTOUR);
			p.setParameter(PopulationParameters.SEQUENCE,_sp);
		}
		catch (Exception e) { ErrorFeedback.handle("Error setting test parameters", e); }
		_nt.setSequenceParameters(_sp);
		Individual i = new Individual(_nt,p);
		
		double fitness = _fi.evaluate(i);
		assertTrue(fitness < 0.3);
	}
	
	// TODO More Contour test cases
	public void testContour() {
		NoteTree _nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\fitnessTestContour-1.tree").getNoteTree();
		NoteTree _nt2 = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\fitnessTestContour-2.tree").getNoteTree();
		FitnessContour _fc = FitnessContour.getInstance();
		PopulationParameters p = new PopulationParameters();

		try {
			p.setParameter(PopulationParameters.FITNESS_TYPE, FitnessType.CONTOUR);
			p.setParameter(PopulationParameters.SEQUENCE,_sp);
		}
		catch (Exception e) { ErrorFeedback.handle("Error setting test parameters", e); }
		
		Individual i = new Individual(_nt,p);
		Individual i2 = new Individual(_nt2,p);
		
		double fitness = _fc.evaluate(i);
		double fitnessDown = _fc.evaluate(i2);
		
		assertEquals(0.5,fitness);
		assertEquals(0.5,fitnessDown);
		
	}
}
