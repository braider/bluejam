package net.parallaxed.bluejam.tests;

import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.playback.MIDI;
import junit.framework.TestCase;

public class MIDITest extends TestCase {
	public void testNumberToNote()
	{
		Note n = MIDI.numberToNote(60f);
		String name = Pitch.getName(n.pitchClass());
		assertEquals(name,"C");
	}
}
