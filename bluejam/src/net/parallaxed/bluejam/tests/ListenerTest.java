package net.parallaxed.bluejam.tests;

import java.util.Iterator;

import net.parallaxed.bluejam.playback.Listener;
import net.parallaxed.bluejam.Evolve;
import net.parallaxed.bluejam.JamParamters;
import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.SequenceParameters;
import junit.framework.TestCase;

public class ListenerTest extends TestCase implements Listener {
	
	private Evolve evolution = null; 
	private Thread evolve = null;
	
	public void listen(NoteSequence n) {
		try {
			n.validateNotes();
			Iterator<Note> sequence = n.getNotes();
			while (sequence.hasNext())
			{
				Note note = sequence.next();
				System.out.println("Note: "+note.noteValue()+", Duration: "+note.duration());
				
			}	

			evolution.feedback(0,n);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void testListener() {
		SequenceParameters _sp = new SequenceParameters(
				new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120)
				);
		evolution = new Evolve(_sp,50);
		evolution.addListener(this);
		evolve = new Thread(evolution);
		evolve.start();
	}
}
