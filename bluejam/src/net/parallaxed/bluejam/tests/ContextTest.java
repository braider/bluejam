package net.parallaxed.bluejam.tests;

import net.parallaxed.bluejam.JamParamters;

import net.parallaxed.bluejam.NoteLeaf;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.Rhythm;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.evolution.NoteContext;
import net.parallaxed.bluejam.evolution.NoteContext.Contour;
import junit.framework.TestCase;

public class ContextTest extends TestCase {
	
	NoteLeaf _nl = new NoteLeaf(Pitch.Eb,Rhythm.SEMIQUAVER,5);
	NoteLeaf _nl2 = new NoteLeaf(Pitch.F,Rhythm.SEMIQUAVER,5);
	NoteLeaf _nl3 = new NoteLeaf(Pitch.G,Rhythm.SEMIQUAVER,5);
	NoteLeaf _nl4 = new NoteLeaf(Pitch.Bb, Rhythm.SEMIQUAVER,5);
	
	public void setUp()
	{
		
	}
	public void testContexLimit()
	{
		SequenceParameters _sp = new SequenceParameters(new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120));

		NoteContext _nCon = new NoteContext(_sp);
		_nCon.add(_nl);
		_nCon.add(_nl2);
		_nCon.add(_nl3);
		assertTrue(_nCon.contour() == Contour.NONE);
	}
	public void testContexUp()
	{
		SequenceParameters _sp = new SequenceParameters(new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120));
		NoteLeaf _nl = new NoteLeaf(Pitch.Eb,Rhythm.SEMIQUAVER,5);
		NoteLeaf _nl2 = new NoteLeaf(Pitch.F,Rhythm.SEMIQUAVER,5);
		NoteLeaf _nl3 = new NoteLeaf(Pitch.G,Rhythm.SEMIQUAVER,5);
		NoteLeaf _nl4 = new NoteLeaf(Pitch.Bb, Rhythm.SEMIQUAVER,5);
		NoteContext _nCon = new NoteContext(_sp);
		_nCon.add(_nl);
		_nCon.add(_nl2);
		_nCon.add(_nl3);
		_nCon.add(_nl4);
		assertEquals( Contour.UP, _nCon.contour());
	}
	
	public void testContexDown()
	{
		SequenceParameters _sp = new SequenceParameters(new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120));

		NoteContext _nCon = new NoteContext(_sp);
		_nCon.add(_nl4);
		_nCon.add(_nl3);
		_nCon.add(_nl2);
		_nCon.add(_nl);
		assertTrue(_nCon.contour() == Contour.DOWN);
	}
	
	/**
	 * This method has a gay name.
	 * TODO Make sensetive to smaller contours?
	 */
	public void testConteXtreme()
	{
		SequenceParameters _sp = new SequenceParameters(new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120));

		NoteContext _nCon = new NoteContext(_sp);
		_nCon.add(_nl4);
		_nCon.add(_nl3);		
		_nCon.add(_nl);
		_nCon.add(_nl2);
		_nCon.add(_nl2);
		_nCon.add(_nl2);
		_nCon.add(_nl3);	
		_nCon.add(_nl4);
		assertEquals(Contour.UP, _nCon.contour());
	}
}
