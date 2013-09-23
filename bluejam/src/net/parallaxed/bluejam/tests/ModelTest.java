package net.parallaxed.bluejam.tests;

import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.grammar.PitchModel;
import net.parallaxed.bluejam.grammar.ModelParser;
import junit.framework.TestCase;

public class ModelTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testModelParser()
	{
		PitchModel m = new ModelParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\bluesModel-C.m").getModel();
		double[] cModel = m.get(Pitch.C);
		double[] BbModel = m.get(Pitch.Bb);
		
		assertEquals(0.10, cModel[0]);		
		assertEquals(1.0,BbModel[5]);
	}
}
