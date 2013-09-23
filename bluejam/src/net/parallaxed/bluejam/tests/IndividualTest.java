package net.parallaxed.bluejam.tests;


import junit.framework.TestCase;
import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.JamParamters;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.Population;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.SequenceParameters;


import org.junit.Before;

public class IndividualTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}
	

	/**
	 * Test's an individual's ability to instantiate the configured
	 * genotype through reflection only.
	 */
	public void testLoadGenotype()
	{	
		SequenceParameters _sp = new SequenceParameters(new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120));
		Population p = new Population(_sp);
		p.initialize();
		Individual i = p.getIndividual(0);
		assertNotNull(i.getNoteSequence());
		assertTrue(i.getNoteSequence().getClass() == NoteTree.class);
	}

}
