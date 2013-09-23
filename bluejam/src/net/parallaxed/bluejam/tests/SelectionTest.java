package net.parallaxed.bluejam.tests;


import net.parallaxed.bluejam.JamParamters;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.Population;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.evolution.Breeder;
import net.parallaxed.bluejam.evolution.SelectTournament;
import net.parallaxed.bluejam.evolution.TreeBreeder;
import junit.framework.TestCase;

public class SelectionTest extends TestCase {
	Population q = null;
	public void testSelectTournament()
	{
		Population p = new Population(new SequenceParameters(new JamParamters(
				Pitch.C,Scale.BLUES.getInstance(),120
				)));
		p.initialize();
		SelectTournament _st = SelectTournament.getInstance();
		q = _st.select(p, 10);
		assertEquals(40, q.getEmptySlots().size());
	}
	
	public void testBreeder()
	{
		Population p = new Population(new SequenceParameters(new JamParamters(
				Pitch.C,Scale.BLUES.getInstance(),120
				)));
		p.initialize();
		SelectTournament _st = SelectTournament.getInstance();
		q = _st.select(p, 10);
		assertEquals(40, q.getEmptySlots().size());
		
		assertEquals(50,q.memberCount);
		Breeder b = new TreeBreeder();
		b.breed(q);
		assertEquals(0, q.getEmptySlots().size());
	}
}
