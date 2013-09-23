package net.parallaxed.bluejam.tests;

import java.util.ArrayList;

import net.parallaxed.bluejam.Heuristic;
import net.parallaxed.bluejam.Individual;
import net.parallaxed.bluejam.JamParamters;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.NoteTree;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.Population;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.TreeParser;
import net.parallaxed.bluejam.evolution.InitializationType;
import junit.framework.TestCase;

public class InitializeTest extends TestCase 
{
	/*
	 * 
	 * Just used to test we're calling the right methods.
	public void testRhythmInitializer() {
		NoteTree _nt = new NoteTree();
		RhythmInitializer rhythmInitializer = RhythmInitializer.getInstance();
		rhythmInitializer.initialize(_nt, new PopulationParameters());
	}
	 */
	/**
	 * Simply tests that the population is filled, not
	 * individuals.
	 */
	public void testInitializePopulation()
	{
		Population p = new Population(new SequenceParameters(new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120)));
		p.initialize();
		ArrayList<Integer> emptySpace = p.getEmptySlots();
		assertEquals(0,emptySpace.size());
	}
	
	/**
	 * Test that initialization of one individual does not 
	 * leave any gaps.
	 * 
	 * @throws Exception
	 */
	public void testInitializeGrowInd() throws Exception
	{
		PopulationParameters params = new PopulationParameters();
			params.setParameter(PopulationParameters.SEQUENCE, 
					new SequenceParameters(new JamParamters(
							Pitch.C,Scale.BLUES.getInstance(),120)));
			Heuristic heuristic = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\blues-5.heuristic").getNoteTree();
			Individual i = new Individual(params, heuristic);
			i.initialize();
			NoteTree _nt = (NoteTree) i.getNoteSequence();
			NoteSequence[] emptySlots = _nt.getIncompleteReferences();
			assertEquals(0, emptySlots.length);

	}
	
	public void testInitializeHeuristic() throws Exception
	{
		PopulationParameters params = new PopulationParameters();
		params.setParameter(PopulationParameters.INITIALIZATION_TYPE, InitializationType.HEURISTIC);
			params.setParameter(PopulationParameters.SEQUENCE, 
					new SequenceParameters(new JamParamters(
							Pitch.C,Scale.BLUES.getInstance(),120)));
			Heuristic heuristic1 = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\blues-5.heuristic").getNoteTree();
			Heuristic heuristic2 = new TreeParser("C:\\Documents and Settings\\Administrator.SERAPH\\My Documents\\CO620\\bluejam\\config\\blues-6.heuristic").getNoteTree();
			Individual i = new Individual(params, heuristic1);
			Individual i2 = new Individual(params, heuristic2);
			
			i.initialize(); i2.initialize();
			
			NoteTree _nt = (NoteTree) i.getNoteSequence();
			NoteSequence[] emptySlots = _nt.getIncompleteReferences();
			assertEquals(0, emptySlots.length);

	}
	
	public void testInitializeRandom()
	{
		
	}
}
