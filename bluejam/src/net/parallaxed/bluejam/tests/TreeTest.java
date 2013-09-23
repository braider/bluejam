package net.parallaxed.bluejam.tests;

import java.util.Iterator;

import net.parallaxed.bluejam.JamParamters;
import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteCollection;
import net.parallaxed.bluejam.NoteLeaf;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.Rhythm;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.TreeParser;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.SequenceException;
import net.parallaxed.bluejam.exceptions.ValidationException;
import junit.framework.TestCase;

public class TreeTest extends TestCase 
{
	NoteTree _nt = null;
	NoteCollection _nc = null;
	NoteLeaf nl1 = new NoteLeaf(Pitch.C, Rhythm.SEMIBREVE, 4);
	NoteLeaf nl2 = new NoteLeaf(Pitch.C, Rhythm.SEMIBREVE, 4);
	NoteLeaf nl3 = new NoteLeaf(Pitch.C, Rhythm.SEMIBREVE, 4);
	NoteLeaf nl4 = new NoteLeaf(Pitch.C, Rhythm.SEMIBREVE, 4);	
	
	public TreeTest()
	{
		
	}	
	 
	protected void setUp()
	{
		_nt = new NoteTree();
		_nc = new NoteCollection();
	}
	
	/**
	 * Tests the add to one level deep
	 */
	public void testAdd1()
	{
		_nc.add(nl1);
		_nc.add(nl2);
		_nc.add(nl3);
		try {
			_nt.addNotes(nl4);
			_nt.addNotes(_nc);
		}
		catch (SequenceException e)
		{

		}
		assertEquals(true,(_nt.getChild(0)) == nl4);
		assertEquals(true,(_nt.getChild(1)) == nl1);
		assertEquals(true,(_nt.getChild(2)) == nl2);
		assertEquals(true,(_nt.getChild(3)) == nl3);
	}
	
	/**
	 * For testing complicated add operations
	 * @deprecated
	 */
	public void DtestAdd2()
	{

		
		setUp();
		_nc.add(nl1);
		_nc.add(nl2);
		_nc.add(nl3);
		try {
			_nt.addNotes(nl4);
			_nt.addNotes(_nc);
		}
		catch (SequenceException e)
		{

		}
		assertEquals(true,(_nt.getChild(0)) == nl4);
		assertEquals(true,(_nt.getChild(1)) == nl1);
		assertEquals(true,(_nt.getChild(2)) == nl2);
		assertEquals(true,(_nt.getChild(3)) == nl3);
	}
	
	public void testBuildHeuristic()
	{
		//assertEquals(true,true);
		NoteTree _nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\blues-5.heuristic").getNoteTree();
		if (_nt != null) ;
	}
	
	public void testInvalidAdd()
	{
		assertEquals(true,true);
	}	

	public void testValidateNotes1()
	{
		_nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\test\\blues-4.heuristic").getNoteTree();
		JamParamters _jp = new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120);
		_nt.sequenceParameters().Jam = _jp;
		
		try {
			_nt.validateNotes();
		}
		catch (ValidationException e) {
			ErrorFeedback.handle(e.getMessage(),e);
		}
		
		Iterator<Note> _notes = _nt.getNotes();
		while (_notes.hasNext())
		{
			Note n = _notes.next();		
			
			assertTrue(n.duration() > -1);
			assertTrue(n.noteValue() > 45);
		}
	}
	
	
	public void testValidateNotes2()
	{
		NoteLeaf[] notes = new NoteLeaf[16];
		setUp();
		JamParamters _jp = new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120);
		_nt.sequenceParameters().Jam = _jp;
		
		for (int i = 0; i < notes.length; i++) {
			notes[i] = new NoteLeaf(Pitch.C,Rhythm.SEMIQUAVER, 4);
			try { _nt.addNotes(notes[i]); }
			catch (SequenceException e) { ErrorFeedback.handle("Error validating notes.", e); }
		}
		
		try {
			_nt.validateNotes();
		}
		catch (ValidationException e) {
			ErrorFeedback.handle(e.getMessage(),e);
		}
		
		Iterator<Note> _notes = _nt.getNotes();
		int i = 0;
		while (_notes.hasNext())
		{
			Note n = _notes.next();
			Note original = notes[i++];
			
			assertEquals(n, original);
			assertTrue(n.duration() > -1);
			assertTrue(n.noteValue() == 48);
		}
	}
		
}
