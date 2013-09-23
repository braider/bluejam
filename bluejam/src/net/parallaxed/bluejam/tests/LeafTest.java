package net.parallaxed.bluejam.tests;


import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.Before;
import net.parallaxed.bluejam.*;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.SequenceException;
import net.parallaxed.bluejam.exceptions.ValidationException;

public class LeafTest extends TestCase {
	
	NoteTree _nt = null;
	@Before
	public void setUp() throws Exception {
		
	}
	
	public void testRemoveLeaf()
	{
		NoteTree _nt = new NoteTree();
		NoteLeaf _nl2 = new NoteLeaf(Pitch.C,Rhythm.MINIM,5);
		NoteLeaf _nl = new NoteLeaf(Pitch.C,Rhythm.MINIM,5);
		try {
			_nt.addNotes(_nl);
			_nt.addNotes(_nl2);
			assertTrue(_nt.contains(_nl2));
			assertTrue(_nt.contains(_nl));
			
			_nt.removeNotes(_nl2);
			assertTrue(_nt.contains(_nl));
			assertFalse(_nt.contains(_nl2));
			
		}
		catch(SequenceException e) { ErrorFeedback.handle("Error.",e); }
	}
	
	public void testSwapNotes()
	{
		_nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\test\\blues-2.heuristic").getNoteTree();
		Iterator<Note> i = _nt.getNotes();
		int c = 0;
		NoteLeaf changedNote = null;
		NoteTree swaptree = new NoteTree();
		try {			
			swaptree.addNotes(new NoteLeaf(Pitch.C,Rhythm.DEMIQUAVER,5));
			swaptree.addNotes(new NoteLeaf(Pitch.C,Rhythm.DEMIQUAVER,5));
			while (i.hasNext()) {
				Note n = i.next();
				c++;
				if (c == 3) {
					changedNote = (NoteLeaf) n;
					_nt.swapNotes(changedNote, swaptree);
					break;
				}
			}
			
			assertTrue(_nt.contains(swaptree));				
		}
		catch (SequenceException e) { ErrorFeedback.handle(e.getMessage(),e); }
		catch (ClassCastException e) { ErrorFeedback.handle(e.getMessage(),e); }
		assertTrue(true);
	}
	/**
	 * Tests that changes in the configuration will propagate
	 * to all note leaves after a subsequent call to 
	 * validateNotes().
	 */

	/**
	 * Tests that relative notes are being evaluated correctly.
	 */
	public void testEvaluateRelative() {
		
		_nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\test\\blues-3.heuristic").getNoteTree();
		JamParamters _jp = new JamParamters(Pitch.A,Scale.BLUES.getInstance(),120);
		
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
			assertTrue(n.duration() > 0);
			assertTrue(n.noteValue() > 55);
		}
	}
	
	public void testEvaluateRelativeChange() {
		
		_nt = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\test\\blues-3.heuristic").getNoteTree();
		JamParamters _jp = new JamParamters(Pitch.A,Scale.BLUES.getInstance(),120);
		
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
			assertTrue(n.duration()  >55);
		}
		
		/// All change...
		
		_jp = new JamParamters(Pitch.B,Scale.BLUES.getInstance(),120);
		_nt.sequenceParameters().Jam = _jp;
		_nt.sequenceParameters().Changed = true;
		
		
		try {
			_nt.validateNotes();
		}
		catch (ValidationException e) {
			ErrorFeedback.handle(e.getMessage(),e);
		}
		
		_notes = _nt.getNotes();
		int i = 0;
		while (_notes.hasNext())
		{
			Note n = _notes.next();
			if (i++ == 0)
				assertTrue(n.evaluatedPitch() == Pitch.B);
			assertTrue(n.duration() > 0);
			assertTrue(n.noteValue() > 45);
		}
		
	}
		
	public void testLeafAdd()
	{
		NoteTree _nt = new NoteTree();
		
		NoteLeaf _nl = new NoteLeaf(Pitch.C,Rhythm.SEMIQUAVER,4);
		NoteLeaf _nl1 = new NoteLeaf(Pitch.D,Rhythm.CROTCHET,4);
		
		try { 
			_nt.addNotes(_nl);
			_nl.addNotes(_nl1);
		}
		catch (SequenceException e) { }
		assertTrue(_nt.contains(_nl) && _nt.contains(_nl1));
	}	
	
	
}