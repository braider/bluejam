package net.parallaxed.bluejam.tests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import net.parallaxed.bluejam.HeuristicCollection;
import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.TreeParser;
import net.parallaxed.bluejam.grammar.ModelParser;
import net.parallaxed.bluejam.grammar.PitchModel;
import net.parallaxed.bluejam.playback.Listener;
import junit.framework.TestCase;

public class EvolutionTest extends TestCase implements Listener {
	
	private net.parallaxed.bluejam.Evolve evolution = null;
	
	public void listen(NoteSequence n)
	{
		if (n == null)
			synchronized (this) {
				System.out.println("Done.");
				notify();				
			}
		
		try {
			System.out.println("\n\nListening\n\n");
			Iterator<Note> notes = n.getNotes();
			if (notes.hasNext())
				for (Note note = notes.next(); notes.hasNext(); note = notes.next())
						System.out.println(
								"Pitch: "+note.pitchClass()+note.octave()+
								"("+note.pitchRelative()+")"+", Duration: "+note.duration());			
			Thread.sleep(3000);
			evolution.feedback(0, n);
		}
		catch (InterruptedException e) { }		
	}
	

	
	public void testEvolve()
	{
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
					new net.parallaxed.bluejam.JamParamters(Pitch.C,Scale.BLUES.getInstance(),120)
					);
			evolution = new net.parallaxed.bluejam.Evolve(_sp,50,hCollection);	
			evolution.addListener(this);
			new Thread(evolution).start();
			try { 
				synchronized (this) {
					wait();	
				}
				
			}
			catch (InterruptedException e)
			{
				
			}
		}
		catch (IOException e) {}
	}
}
