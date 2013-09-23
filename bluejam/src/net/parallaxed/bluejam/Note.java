package net.parallaxed.bluejam;

import net.parallaxed.bluejam.JamParamters.Config;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.PitchException;
import net.parallaxed.bluejam.exceptions.RhythmException;
import net.parallaxed.bluejam.exceptions.ValidationException;
import net.parallaxed.bluejam.playback.MIDI;

/**
 * This class is currently under consideration for the "abstract"
 * modifier.
 * 
 * This class fully abstracts the properties of a note from 
 * the possible implementations of notes in the evolution
 * framework.
 * 
 * Since BlueJam is entirely tree based, the default extension
 * of this class is NoteLeaf. Notes can belong to more than one
 * collection. A NoteLeaf can also belong to more that one
 * collection, but only one NoteTree.
 * 
 * Once stored in a structure, the only safe way to manipulate
 * the note is through the methods defined on the type
 * specific to that data structure -i.e. it's not recommended
 * to typecast to Note in order to bypass things like locking
 * protection.
 * 
 * All subclasses of Note involved in a GP algorithm should
 * also implement Terminal.
 * 
 * TODO Event-driven pattern for Note validation to inform children.
 * 
 * @see Terminal
 * @see NoteLeaf
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class Note
{	
	/**
	 * This requires post-processing by an output class.
	 */

	public static final long serialVersionUID = 1L;
	////// EXCEPTIONS
	/**
	 * Exception message when the note is set out of range.
	 * 
	 */
	private static final String E_RANGE = "Note out of range";
	private static final String E_EVALUATION_PARAMS = "Unable to evaluate the relative pitch of this note: No Jam parameters specified";
	private static final String E_EVALUATION_PARAMS_RHYTHM = "Unable to evaluate the duration of this note: No Sequence parameters specified";
	private static final String E_EVALUATION_RHYTHM_SWING = "WARNING: Unable to evaluate swing on this note - check partner variables are intact";	
	//private static final String E_EVALUATION_PARAMS_TEMPO = "Please check the tempo parameter";
	/**
	 * This warning will be fired if the pitchRelative field
	 * is out of bounds. The default behaviour then is to
	 * resolve to the root pitch of the scale.
	 */
	private static final String W_EVALUATION_INDEX_INVALID = "WARNING: Relative pitch index is invalid, resolving to root pitch instead";
	//////
	
	////// VALIDATION
	/**
	 * Notes need a primitive validation model to insure they
	 * are of a MIDI-playable pitch and rhythm.
	 */
	protected boolean validated = false;

	
	/**
	 * This method validates the note and throws the 
	 * right kind of exception if anything is awry.
	 * 
	 * For some reason this method uses lazy evaluation to
	 * optimize screen real-estate.
	 * 
	 * @throws ValidationException
	 */
	public void validateNotes() throws ValidationException {
		ValidationException ex = new ValidationException(this);
		boolean invalid = false;
		if (!validatePitch() && (invalid |= true))			
			ex.append(new PitchException(this));		
		if (!validateRhythm() && (invalid |= true))
			ex.append(new RhythmException(this));
		
		validated = !invalid;
		if (invalid)
			throw ex;		
	}
	
	/**
	 * Checks the duration of this note (in milliseconds) is > 0.
	 * 
	 * @return True if the criteria are met, false otherwise.
	 */
	protected boolean validateRhythm() {
		if (duration > -1)
			return true;
		return false;
	}
	
	/**
	 * Checks that the pitchClass field has a value and that
	 * the value is not relative.
	 * 
	 * @return True if the criteria are met, false otherwise.
	 */
	protected boolean validatePitch()	{
		
		if (pitchClass != null && noteValue > -1 && octave > -2)
			return true;
		
		return false;
	}
	
	/**
	 * Marks this note as invalid.
	 */
	protected void invalidate() {
		validated = false;
	}
	//////

	
	////// DURATION
	/**
	 * Duration of this note in milliseconds.
	 * 
	 */
	protected double duration = -1;
	/**
	 * Sets the duration of the note. This value is sent out
	 * along the MIDI line to tell the ouput device how long
	 * to hold the note for.
	 * 
	 * Can only be called by it's subclasses that deal with
	 * how long the note should be played for.
	 * 
	 * @param milliseconds The new duration in milliseconds.
	 */
	protected void duration(double milliseconds) {		
		this.duration = milliseconds;
	}
	
	/**
	 * Retrieves the duration of the note.
	 * 
	 * Logs an exception if the note was not validated
	 * prior to playback.
	 * 
	 * @return The length of this note in milliseconds.
	 */
	public double duration() {
		if (!validated)
			ErrorFeedback.handle("The duration of this note was not validated", new ValidationException(this));
		return duration;
	}
	//////
	
	////// OCTAVE
	/**
	 * The octave number of this note.
	 * 
	 * Private because this should be set with the validation
	 * present in octave(int x).
	 * 
	 * -2 = Not Set.
	 */	
	private int octave = -2;

	/**
	 * Returns the octave number.
	 * @return The octave the note is in
	 */
	public int octave() {
		return octave; 
	}
	
	/**
	 * Sets the octave of this note if it can be 
	 * arbitrarily changed.
	 * 
	 * Subclasses should override this to account for locks
	 * if necessary.
	 * 
	 * @param octave The new octave number
	 */
	public void octave(int octave) {
		// make sure we're within MIDI note range.
		if (octave >= -1 && octave <= 9)
		{		
			// Cannot have > G9
			if (octave < 9 || MIDI.LESS_THAN.eval(pitchClass, Pitch.Gs))
			{
				this.octave = octave;
			}			
			invalidate();
			return;
		}
		ErrorFeedback.handle(E_RANGE, new ValidationException(this));
	}
	//////
	
	////// Rhythm
	/**
	 * Once evaluated, this value stores the current 
	 * rhythm of the note. In the NoteLeaf implementation,
	 * this is based on it's position in the tree.
	 * 
	 * This can only be set once by the constructor,
	 * subsequently, it must be re-evaluated.
	 */
	protected Rhythm rhythm = null;
	
	/**
	 * Only accessed when initially adding this NoteLeaf to
	 * a NoteTree.
	 * 
	 * @return The rhythm of the note
	 */
	public Rhythm rhythm()	{ return rhythm; }
	
	/**
	 * Only accessed when initially adding this NoteLeaf to
	 * a NoteTree.
	 * 
	 * This method should be overridden (not just hidden) by classes
	 * that use locking.
	 * 
	 */
	public void rhythm(Rhythm r)	{
		this.rhythm = r;
		invalidate();
	}
	//////
	
	/**
	 * Whether or not this note is a rest.
	 */
	protected boolean rest = false;
	public boolean rest() { return rest; }	
	////// PITCH
	/**
	 * The pitchClass of this note.
	 */
	protected Pitch pitchClass = null;
	/**
	 * A positive or negative integer indicating the offset
	 * from the root note if the pitch is Pitch.R (relative).
	 * 
	 * (used only when evaluating heuristic trees)
	 */
	protected int pitchRelative = 0;
	
	/**
	 * Set the pitchClass of this note.
	 * 
	 * Subclasses should override this fully to account for mutability
	 * of note properties.
	 * 
	 * @param p The desired pitchClass
	 */
	public void pitchClass(Pitch p) {
		this.pitchClass = p;
		invalidate();
	}
	
	/**
	 * 
	 * @return The Pitch class of this note.
	 */
	public Pitch pitchClass() {		
		return pitchClass; 
	}
	
	/**
	 * Sets the pitch relative to the given root note + octave.
	 * 
	 * Used when evaluating heuristic trees.
	 * 
	 * If this number is negative, the root note is above the note
	 * If this number is positive, the root note is below the note
	 * @param offset The number of steps (semitones) between the root note and this note.
	 */
	public void pitchRelative(int offset) {		
		if (offset > 12 || offset < -12)
			return;
		invalidate();
		pitchRelative = offset;
	}
	
	/**
	 * Used when pitch = Pitch.R to calculate the absolute pitch of the note.
	 * 	@return The number of steps to this note from the root
	 */
	public int pitchRelative() { return this.pitchRelative; }
	
	/**
	 * Stores the actual pitch this note was evaluated at
	 * if pitchClass = Pitch.R.
	 * TODO Move this to NoteLeaf as Pitch.R classes cannot be defined without context.
	 */
	protected Pitch _evaluatedPitch;
	
	/**
	 * @return A value for the calculated pitch.	
	 */
	public Pitch evaluatedPitch() { return _evaluatedPitch; }
	
	//////
	
	////// SWING
	protected int _swingPercent = 0;
	/**
	 * @return The percentage swing on this note (if any).
	 */
	public int swingPercent() { return _swingPercent; }
	
	protected Note _swingPartner = null;
	/**
	 * @return The note partnered with this one.
	 */
	public Note swingPartner() { return _swingPartner; } 
	/**
	 * Adds swing to a given note.
	 * 
	 * A swing of 100% is equivalent to a tied note.
	 * 
	 * Swing is the act of pairing off two notes and then
	 * assigning a ratio to control their rhythmic value.
	 * 
	 * This function will automatically assign the right 
	 * swing value to the partner note.
	 * 
	 * @param swingPercent The amount of swing to assign to this note
	 * @param swingPartner The swing partner of this note
	 * 
	 */
	public void swingNote(int swingPercent, Note swingPartner)
	{
		if (swingPartner == null && swingPercent > -1)
			return;
		
		if ((swingPercent < 101) && (swingPercent > -1))		
		{
			if (this._swingPartner != swingPartner) {
				invalidate();
				this._swingPercent = swingPercent;
				this._swingPartner = swingPartner;
				swingPartner.swingNote(100 - this._swingPercent,this);
			}
			return;
		}
		
		if (swingPercent == -1) 		
			clearSwing();			
	}
	
	/**
	 * Clears swing from this note and it's partner if
	 * applicable.
	 */
	private void clearSwing()
	{
		invalidate();
		Note swingPartner = this._swingPartner;
		this._swingPartner = null;
		if (swingPartner != null)
			swingPartner.clearSwing();
		this._swingPercent = 0;
	}
	//////
	
	//////
	/**
	 * This value is evaluated once the note is imported into
	 * a playback buffer.
	 * 
	 * The float value is the MIDI number of the note, usually
	 * used to pass into one of the playback classes.
	 * 
	 * If noteValue > -1, then this note has been evaluated.
	 * 
	 * noteValid = (-1 < noteValue < 128)
	 */
	protected float noteValue = -1;
	
	/**
	 * The MIDI note number of this note.
	 * @return A float between 0 and 127
	 */
	public float noteValue() {
		return noteValue;
	}
	//////
	
	/**
	 * Trivial Constructor for later application of properties.
	 */
	public Note() {	}
	
	/**
	 * Constructs a note with the given pitch class and octave
	 * @param pitchClass
	 * @param octave
	 */
	public Note(Pitch pitchClass, int octave) 
	{
		this.pitchClass = pitchClass;
		this.octave = octave;
	}
	
	public Note(float noteValue) {
		this.noteValue = noteValue;
	}
	
	
	/**
	 * Sets this note to be a rest given the passed boolean.
	 * 
	 * No information about the note is lost by turning it into
	 * a rest note. This can be undone at a later stage by calling
	 * toggleRest() again.
	 * 
	 * @param rest True turns this into a rest note, false reverts.
	 */
	public void toggleRest(boolean rest) {
		invalidate();
		this.rest = rest;
		// REMOVE TIE
	}
	
	/**
	 * Toggles rest on/off.
	 */
	public void toggleRest() {
		invalidate();
		this.rest = !this.rest;	
	}
	
	/**
	 * Returns true if this note is a rest
	 */	
	public String toString() {
		return "Note"+this.hashCode();
	}
	
	/**
	 * A note can be defined as "relative", in the context of 
	 * a given pitch and scale.
	 * 
	 * If a note is relative, it will remain invalid until this
	 * method is called with some notion of context (i.e. a root
	 * pitch and a scale).
	 * 
	 * This method will resolve the relative position of this note
	 * into an absolute pitch value.
	 * 
	 * @param rootPitch The root pitch to evaluate against
	 */
	public boolean evaluatePitch(Pitch rootPitch) {
		if (pitchClass != null && pitchClass != Pitch.R) {
			noteValue = MIDI.noteToNumber(this);
			return true;
		}
		try {
			_evaluatedPitch = MIDI.relative(rootPitch, pitchRelative);
			noteValue = MIDI.noteToNumber(this);			
			return true;
		}
		catch (RuntimeException e) {
			ErrorFeedback.handle(W_EVALUATION_INDEX_INVALID, e);			
		}
		return false;	
	}
	
	/**
	 * Evaluates the rhythm of this note given the passed 
	 * SequenceParameters. These need to be passed in from 
	 * an implementation of Note.
	 * 
	 * Error's will be sent to the default ErrorFeedback.
	 *  
	 * @param params The given SequenceParameters ("context")
	 * @return Whether the evaluation succeeded or not.
	 */
	public boolean evaluateRhythm(SequenceParameters params) 
	{
		if (params != null)
		{
			JamParamters _jp = params.Jam;
		
			if (_jp != null)
			{
				/** 
				 * Hasten the evaluation if we can.
				 */
				if (_swingPartner != null && _swingPercent == 0) {
					duration = 0;
					return true;
				}
				try
				{
					double bps = ((Integer)params.Jam.getParameter(Config.TEMPO)).doubleValue() / 60;
					double beatLength = 1000 / bps;
					
					// NB: Fractions are stored relative to Rhythm.SEMIBREVE
					// but this calculates relative to the beat (Rhythm.CROTCHET)
					// so we multiply by 4...
					duration = (beatLength * rhythm.evalR()) * 4;
					
					if (_swingPartner != null) {
						Rhythm r = _swingPartner.rhythm();
						if (r == null)
							ErrorFeedback.handle(new Exception(E_EVALUATION_RHYTHM_SWING));
						double prePartnerDuration = (beatLength * r.evalR()) * 4;
						double totalDuration = prePartnerDuration + duration;
						duration = (double) _swingPercent/100 * totalDuration;
					}
					return true;
				}
				catch (Exception e)	{
					ErrorFeedback.handle(E_EVALUATION_PARAMS_RHYTHM, new RhythmException(this));
				}
			}
		}
		ErrorFeedback.handle(E_EVALUATION_PARAMS_RHYTHM, new RhythmException(this));
		return false;
	}	
	
	/**
	 * Evaluates this Note in the given JamParameters context.
	 * 
	 * JamParameters supplies the root pitch. The actual pitch of 
	 * this note is supplied by 0 < pitchRelative < 11, which is 
	 * added as an offset to the root pitch to give the real pitch.
	 * 
	 * @param params The JamParameters context - passed from somewhere on high.
	 * @return True on success, false on failure.
	 */
	protected boolean evaluatePitch(JamParamters params) {
		if (params == null)
			ErrorFeedback.handle(E_EVALUATION_PARAMS, new PitchException(this));
		return evaluatePitch(params.rootPitch());
	}
	
	
}

