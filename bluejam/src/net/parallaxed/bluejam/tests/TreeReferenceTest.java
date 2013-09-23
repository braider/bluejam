package net.parallaxed.bluejam.tests;


import net.parallaxed.bluejam.Mutable;
import net.parallaxed.bluejam.NoteLeaf;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.Rhythm;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.SequenceException;
import junit.framework.TestCase;

public class TreeReferenceTest extends TestCase {
	
	NoteLeaf _nlC = new NoteLeaf(Pitch.C,Rhythm.CROTCHET,5);
	NoteLeaf _nlD = new NoteLeaf(Pitch.D,Rhythm.CROTCHET,5);
	NoteLeaf _nlE = new NoteLeaf(Pitch.E,Rhythm.QUAVER,5);
	NoteLeaf _nlF = new NoteLeaf(Pitch.F,Rhythm.QUAVER,5);
	NoteLeaf _nlG = new NoteLeaf(Pitch.G,Rhythm.QUAVER,5);
	NoteLeaf _nlA = new NoteLeaf(Pitch.A,Rhythm.QUAVER,5);
	NoteLeaf _nlB = new NoteLeaf(Pitch.B,Rhythm.SEMIQUAVER,5);

	public void testNullRefs()
	{
		NoteTree _nt = new NoteTree();
		try {
			_nt.addNotes(_nlC);
			_nt.addNotes(_nlE);
			_nt.addNotes(_nlB);
			_nt.addNotes(_nlD);		
			_nt.addNotes(_nlF);
			NoteSequence[] _nullNS = _nt.getIncompleteReferences();
			assertEquals(5, _nullNS.length);
			int[] depth = new int[] { 0, 0, 0, 0, 0, 0 };
			for (NoteSequence _ns : _nullNS)
			{
				assertEquals(NoteTree.class, _ns.getClass());
				NoteTree nt = (NoteTree) _ns;
				depth[nt.depth()]++;				
			}
			assertEquals(3,depth[0]);
			assertEquals(1,depth[3]);
			assertEquals(1,depth[4]);			
		}
		catch (SequenceException e) {
			ErrorFeedback.handle(e.getMessage(), e);			
		}
	}
	
	public void testCrossoverRefs()
	{
		NoteTree _nt = new NoteTree();
		
		try {
			_nlA.lockMask(Mutable.NONE,true);
			_nt.addNotes(_nlC);
			_nt.addNotes(_nlD);
			_nt.addNotes(_nlE);
			_nt.addNotes(_nlF);
			_nt.addNotes(_nlG);
			_nt.addNotes(_nlA);
		}
		catch (Exception e) {
			ErrorFeedback.handle(e.getMessage(),e);			
		}
		
		NoteSequence[] _ns = _nt.getCrossoverReferences();
		
		assertTrue(_ns.length == 3);
		
		for (NoteSequence node : _ns)
		{
			if (node.getClass() == NoteTree.class)
			{
				NoteTree nt = (NoteTree) node;
				assertTrue((nt.depth() == 2) || (nt.depth() == 3));
			}
			else if (node.getClass() == NoteLeaf.class)
			{
				NoteLeaf nl = (NoteLeaf) node;
				assertTrue(nl.pitchClass() == Pitch.G);
			}
		}
	}
	
	public void testMutationRefs()
	{
		NoteTree _nt = new NoteTree();
		
		try {
			_nlA.lockMask(Mutable.NONE,true);
			_nlG.lockMask(Mutable.PITCH,true);
			_nlE.lockMask(Mutable.NONE,true);
			_nt.addNotes(_nlC);
			_nt.addNotes(_nlD);
			_nt.addNotes(_nlE);
			_nt.addNotes(_nlF);
			_nt.addNotes(_nlG);
			_nt.addNotes(_nlA);
			
			NoteLeaf[] _nl = _nt.getMutationReferences();
			assertEquals(4,_nl.length);
			for (NoteLeaf nl : _nl)
			{				
				assertTrue(nl.pitchClass() != Pitch.A);
				assertTrue(nl.pitchClass() != Pitch.E);
			}
		}
		catch (Exception e) {
			ErrorFeedback.handle(e.getMessage(),e);	
		}
	}
}
