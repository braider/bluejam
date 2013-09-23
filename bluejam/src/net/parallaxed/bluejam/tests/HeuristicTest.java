package net.parallaxed.bluejam.tests;

import java.io.File;
import java.util.HashMap;

import net.parallaxed.bluejam.HeuristicCollection;
import net.parallaxed.bluejam.JamParamters;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.Population;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.ScaledSet;
import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.TreeParser;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.grammar.ModelParser;
import net.parallaxed.bluejam.grammar.PitchModel;
import junit.framework.TestCase;

public class HeuristicTest extends TestCase {
	public void testHeuristic() {
		try {
			HeuristicCollection hCollection = new HeuristicCollection();
			HashMap<Pitch, PitchModel> models = new HashMap<Pitch, PitchModel>();
			
			File dir = new File("./config/");
			
			System.out.println("Looking for heuristics in: "+ dir.getCanonicalPath());
			File[] files = dir.listFiles();
			
			for (File f : files)
			{
				String name = f.getName();
				if (name.contains(".m"))
				{
					if (name.matches(".*?\\-[ABCDEF]+[sb]?\\..*?"))
					{
						String p = name.substring(name.indexOf('-')+1,name.indexOf('.'));
						Pitch pitch = Pitch.getPitch(p);
						models.put(pitch, new ModelParser(f).getModel());
					}
				}
				if (name.contains(".heuristic"))
					if (f.canRead())
						hCollection.add(new TreeParser(f).getNoteTree());
			}
			SequenceParameters _sp = new SequenceParameters(
					new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120)
				);
			ScaledSet s = _sp.Jam.getScaledSet();
			s.setModel(models.get(Pitch.C));
			Population p = new Population(_sp,50,hCollection);
			p.initialize();
			assertTrue(p.getIndividual(0).getHeuristic() != null);
			assertEquals(0,p.getEmptySlots().size());
		}
		catch (Exception e) {
			ErrorFeedback.handle(e.getMessage(), e);
		}		
	}
}
