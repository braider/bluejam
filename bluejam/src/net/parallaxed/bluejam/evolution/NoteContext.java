package net.parallaxed.bluejam.evolution;

import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteCollection;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.playback.MIDI;

/**
 * The NoteContext class is a wrapper for NoteCollection
 * that gathers some information about that particular 
 * collection of notes.
 * 
 * NoteContext also provides the Contour enum.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class NoteContext extends NoteCollection {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Has this context been changed?
	 */
	private boolean _changed = false;
	//private int _currentOctave = 5;
	private int _contourThreshold = 4;
	
	/**
	 * Contour defines the overall progression of a
	 * NoteSequence. This is calculated using a minimum
	 * number of notes called the "contour threshold".
	 * 
	 * If the calculating context has less notes than
	 * the contour threshold, no contour can be assigned.
	 * 
	 * @author Ciarán Rowe (csr2@kent.ac.uk)
	 *
	 */
	public enum Contour {
		/**
		 * A NoteSequence that predominantly goes upward
		 */
		UP, 
		/**
		 * A NoteSequence that predominantly goes downward
		 */
		DOWN, 
		/**
		 * A NoteSequence that does not have a predominant direction.
		 */
		NONE;
	}
	
	private Contour _contour = Contour.NONE; 
	
	/**
	 * Calculates the contour based on the last notes
	 * added.
	 */
	private Contour _deriveContour()
	{
		_changed = false;
		if (size() < _contourThreshold)
			return Contour.NONE;
			
		float differenceSum = 0;
		float lastNote = -1;
		Note rootNote = new Note(sequenceParameters().Jam.rootPitch(),5);
		for (Note n : subList(size()-_contourThreshold, size()))
		{
			float newNote = 0;
			// Measure the collective differences between each note.
			if (n.pitchClass() == Pitch.R)
			{
				rootNote.octave(n.octave());
				newNote = MIDI.noteToNumber(rootNote) + n.pitchRelative();
			}
			else
				newNote = (MIDI.noteToNumber(n));
			
			if (lastNote != -1)
				differenceSum += (newNote - lastNote);
			lastNote = newNote;
		}
		if (differenceSum < 0 - _contourThreshold)
			return Contour.DOWN;
		if (differenceSum > _contourThreshold)
			return Contour.UP;
		
		return Contour.NONE;
	}
	
	public Contour contour() {
		if (_changed)
			_contour = _deriveContour();
		return _contour;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(Note element) {
		return (_changed = super.add(element) | _changed);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(int index, Note element) {
		_changed = true;
		super.add(index, element);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object o) {
		return (_changed = super.remove(o) | _changed);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Note remove(int index) {
		_changed = true;
		return super.remove(index);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		_changed = false;
		super.clear();
	}
	
	/**
	 * Instantiates a NoteContext.
	 */
	public NoteContext(SequenceParameters sequenceParameters) {
		super(sequenceParameters);
		//_currentOctave = (int) Math.floor((sequenceParameters.Jam.minOctave() + sequenceParameters.Jam.maxOctave()) / 2);
	}
}
