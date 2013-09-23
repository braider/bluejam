package net.parallaxed.bluejam.pd;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import net.parallaxed.bluejam.Evolve;
import net.parallaxed.bluejam.HeuristicCollection;
import net.parallaxed.bluejam.Note;
import net.parallaxed.bluejam.NoteSequence;
import net.parallaxed.bluejam.JamParamters;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.PopulationParameters;
import net.parallaxed.bluejam.Scale;
import net.parallaxed.bluejam.ScaledSet;
import net.parallaxed.bluejam.SequenceParameters;
import net.parallaxed.bluejam.TreeParser;
import net.parallaxed.bluejam.JamParamters.Config;
import net.parallaxed.bluejam.evolution.HeuristicSelectionType;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.ParameterException;
import net.parallaxed.bluejam.exceptions.ValidationException;
import net.parallaxed.bluejam.grammar.ModelParser;
import net.parallaxed.bluejam.grammar.PitchModel;
import net.parallaxed.bluejam.playback.Listener;
import net.parallaxed.bluejam.playback.MIDI;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.Executable;
import com.cycling74.max.MaxClock;
import com.cycling74.max.MaxObject;

/**
 * Waits for a PD bang to kickstart the evolution and 
 * playback of evolved solos. Also configures various parameters
 * of the evolution before running the a generation.
 *  
 * This object supports two modes - evolution and single file. In
 * single file mode, the object is initialized with an 
 * argument from PD, pointing to a tree file. The program loads
 * this tree file and all subsequent playback will involve that
 * tree file. The user can reload this single file after making
 * changes to adjust the sound produced. This mode helps
 * when designing heuristics, as the user can hear the programmed
 * heuristic at any tempo and relative to any pitch.
 * 
 * In evolution mode, the program loads all heuristics present in
 * ${pd_home}/extra/bluejam/config (all .heuristic files), along
 * with all models if present (.../model-${PITCH}.m), and 
 * will run evolution cycles when triggered on or off.
 * 
 * Custom scales are not supported (yet).
 * TODO Stop errors from killing instantiation.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class Configure extends MaxObject implements Executable, Listener
{
	private static QueueLoader _queueLoader = null;
	
	private Evolve evolution = null;
	private int populationSize = 50;
	private PopulationParameters popParams = null;
	
	private static final String E_READ_CANONICAL_PATH = "Unable to read the current working directory. Please make sure the user has permissions to read all PD directories."; 
	// Clock object.
	MaxClock clock;
	// Beats per minute.
	float bpm = 120;
	
	/**
	 * Are we ready to change pitch?
	 * 
	 * This changes when we're sent a bang.
	 */
	private boolean ready = false;
	private boolean singleFileMode = false;
	private String filename = "";
	private boolean _changed = false;
	
	private HeuristicCollection hCollection = new HeuristicCollection();
	private HashMap<Pitch, PitchModel> models = new HashMap<Pitch, PitchModel>();
	private int _currentSequenceFeedback = 0;
	private Note play = null;
	private NoteSequence _activeNotes = null; 
	private Iterator<Note> _currentSequence = null;
	
//	private Iterator<Note> _sequenceBuffer = null;
	private volatile ArrayList<NoteSequence> _played = new ArrayList<NoteSequence>();
	private volatile ArrayList<NoteSequence> _activeNS = new ArrayList<NoteSequence>();
	private volatile ArrayList<Iterator<Note>> _queue = new ArrayList<Iterator<Note>>();
	private JamParamters _config = new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120);
	double offset = 0d;
	
	/**
	 * Instantiates the configuration to operate in evolution
	 * mode (no arguments).
	 */
	public Configure()
	{
		_queueLoader = new QueueLoader(this);
		_config = new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120);
		File f = new File(".");
		try {
			System.out.println("Working directory: "+f.getCanonicalPath());
		}
		catch(IOException e)
		{
			System.out.println(E_READ_CANONICAL_PATH);
		}
		
		/*
		 * Sets the inlet DataTypes (Hot inlet is the first one)
		 */
		int[] types = { 
				DataTypes.ANYTHING, 
				DataTypes.FLOAT, 
				DataTypes.FLOAT, 
				DataTypes.ANYTHING, 
				DataTypes.FLOAT,
				DataTypes.FLOAT, 
				DataTypes.FLOAT,
				DataTypes.FLOAT
			};
		
		declareInlets(types);
		int[] outTypes = {
			DataTypes.FLOAT,
			DataTypes.FLOAT,
			DataTypes.FLOAT
		};
		declareOutlets(outTypes);
		// Two outlets are declared by default, so we don't need to
		// worry about those.
		
		// Declares a new clock.
		clock = new MaxClock(this);
		// Sets this object as the executable for each clock tick.
		clock.setExecutable(this);
		
	}
	
	/**
	 * If constructed with arguments, only the supplied filename is played 
	 * (singleFileMode).
	 * @param args A string array of all arguments passed to the program, only args[0] is read.
	 */
	public Configure(Atom[] args)
	{	
		this();
		for (int i=0;i<args.length;i++) {
			post("arg[" + i +"]:" + args[i].toString());			
		}
		singleFileMode = true;
		String fileToLoad = args[0].toString();
		
		if (fileToLoad.length() > 0)
			loadfile(this.filename = fileToLoad);
	}
	
	/**
	 * Queue's up a NoteSequence in the buffer and pre-meditatively
	 * evaluates it.
	 */
	public void listen(NoteSequence n) {
		/*
		 * Load and buffer the passed note sequence by evaluating it
		 * and grabbing it's iterator.
		 */
		if (n == null) {
			System.out.println("Evolution Finished.");
			return;
		}
			
		if (n != null) {
			n.sequenceParameters().Jam = _config;
			n.sequenceParameters().Changed = _changed;
			try { 
				n.validateNotes();
				_activeNS.add(n);
				_queue.add(n.getNotes());
			}
			catch (ValidationException e) {
				ErrorFeedback.handle(e.getMessage(), e);
			}
			//n.sequenceParameters().Changed = _changed = false;
		}
		// Have we started yet?
		if (_activeNotes == null)
			getReady();
	}
	

	/**
	 * Checks a loaded NoteSequence in available in _queue, and
	 * sets up the play() function to read the contents of 
	 * _currentSequence (an iterator over _activeNotes).
	 */
	private void getReady() {
		
		post("Getting Ready - Root Pitch: "+Pitch.getName((Pitch)_config.getParameter(Config.ROOT_PITCH)));
		
		
		try {
			/*
			 * Check we've got something in our buffer, if we have,
			 * use it.
			 * TODO Deal intelligently with buffer underruns.
			 */
			_activeNotes = null;
			post("Queue Size: "+_queue.size());
			
			if (_queue.size() > 0)
			{
				_currentSequence = _queue.remove(0);
				_activeNotes = _activeNS.remove(0);
			}
							
			// If the queue is null - replay the last buffer in single file mode
			if ((_currentSequence == null || singleFileMode) && _played.size() > 0)
			{
				post("Reloading last sequence.");				
				_activeNotes = _played.get(_played.size()-1);	
				_currentSequence = _activeNotes.getNotes();
			}
			
			/*
			 * Check we have a NoteSequence and corresponding iterator. 
			 * If anything has changed since we last processed it, re-
			 * validate the notes.
			 * TODO May need to check performance on slower systems.
			 */
			if (_currentSequence != null && _activeNotes != null) {
				
				if (_activeNotes.sequenceParameters().Changed = _changed)
				{
					//System.out.println("Detected change.");
					_activeNotes.validateNotes();
					_currentSequence = _activeNotes.getNotes();
					// We need to re-evaluate the queue with the changed parameters.
					if (_queue.size() > 0)
						new Thread(_queueLoader,"ReEvalauteQueue").start();
				}
				//System.out.println("Scale Pitch: "+_activeNotes.sequenceParameters().Jam.rootPitch().toString());
				ready = true;
				if (evolution != null)
					outlet(2,evolution.generations());
			}
			else {
				// buffer underrun :(
				ErrorFeedback.handle("Buffer Underrun", new Exception());
			}
		}
		catch (Exception e) {
			post("Failed to validate the notes in the passed sequence: "+e.getMessage());
			ready = false;
		}
		
	}

	
	private void loadfile(String filename)
	{
		TreeParser _tp = new TreeParser("extra/bluejam/config/"+filename);
		NoteSequence file = (NoteSequence) _tp.getNoteTree();
		file.sequenceParameters().Jam = _config;
		listen(file);
	}
	/**
	 * Kickstarts the evolution process by re-reading the
	 * config directory and restarting the evolution thread.
	 * FIXME Test the default config location picks up correctly on other platforms
	 */
	private void bootEvolution() {

		try {
			/*
			 * Collect heuristics and models.
			 */
			File dir = new File("./extra/bluejam/config/");
			
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
						System.out.println("Found and loaded: "+name);
					}
				}
				if (name.contains(".heuristic"))
					if (f.canRead())
					{
						hCollection.add(new TreeParser(f).getNoteTree());
						System.out.println("Found and loaded: "+name);
					}
			}
			/*
			 * Plug in to sequence parameters.
			 */
//			//_config = new JamParamters(Pitch.C,Scale.BLUES.getInstance(),120);
			SequenceParameters _sp = new SequenceParameters(_config);
			
			ScaledSet s = _config.getScaledSet();
			//s.setModel(models.get(Pitch.C));
			s.setModel(models.get(_config.rootPitch()));
			evolution = new Evolve(_sp,populationSize,hCollection);	
			popParams = evolution.getPopulationParameters();
			evolution.addListener(this);
			new Thread(evolution,"Evolution").start();
		}
		catch (Exception e) {
			ErrorFeedback.handle(e.getMessage(), e);
		}		
	}

	
	/**
	 * Keeps the ball rolling; this function is called by
	 * the inner MaxClock. The delay of the MaxClock is set
	 * to the duration of the note.
	 * 
	 * TODO - This is very primitive sequencing, could introduce separate clock for timing. 
	 *  
	 */
	public void execute()
	{
		if (play == null) 	{
			post("Null note.");
			return;
		}
		
		post("Duration: "+play.duration());
		post("Note: "+play.noteValue());
		
		double delay = play.duration();
		
		if (delay == 0)
			if (!_currentSequence.hasNext())

				reload();
		
		if(!play.rest() && delay > 0) {
			this.outlet(1,play.duration());
			this.outlet(0,play.noteValue());
		}
		
		if (_currentSequence.hasNext()) {
			play = _currentSequence.next();
			clock.delay(delay);			
		}
		else
		{
			if (!singleFileMode && evolution != null)
				evolution.feedback(_currentSequenceFeedback, _activeNotes);
			
			play = null;
			
			getReady();
			// Continue
			if (!singleFileMode)
				play();
		}
	}
	
	/**
	 * Reloads the file or completely restarts the evolution 
	 * if running.
	 */
	public void reload()
	{
		if (evolution != null)
			evolution.running = false;
		evolution = null;
		hCollection = new HeuristicCollection();
		models = new HashMap<Pitch, PitchModel>();
		
		play = null;
		_activeNotes = null; 
		_currentSequence = null;
		
		_played = new ArrayList<NoteSequence>();
		_activeNS = new ArrayList<NoteSequence>();
		_queue = new ArrayList<Iterator<Note>>();
		if (singleFileMode)
			loadfile(filename);
			
	}
	
	/**
	 * This instructs the program to get ready and listen
	 * on the MIDI lines to detect a root pitch key.
	 */
	protected void bang()
	{
		if (getInlet() > 1)
			reload();
		/*
		 * Turn us off if we're on.
		 */
		if (play != null) {
			clock.unset();			
			reload();
			getReady();
			return;
		}
		
		if(!ready) {
			post("Not Ready.");
			getReady();
		}
		
		// Should we load the evolution or begin playback?
		if(!singleFileMode && evolution == null)
			bootEvolution();
		else
			play();
	}
	
	/**
	 * Kickstarts the play process by checking readyness and then
	 * execute()ing
	 */
	private void play()
	{
		if (ready) {
			play = _currentSequence.next();
		
			post("banged.");

			if (_played.size()>0)
			{
				if (_played.get(_played.size()-1) != _activeNotes)
					_played.add(_activeNotes);
			}
			else { _played.add(_activeNotes); }
			post("size: "+ _played.size());
			
			double delay = play.duration();
			if(!play.rest() && delay > 0) {
				this.outlet(1,delay);
				this.outlet(0,play.noteValue());
			}
			
			if (_currentSequence.hasNext()) {
				play = _currentSequence.next();
				clock.delay(delay);
			}
			else 
				ready = false;
		}
	}
	
	
	
	/*
	 * Doesn't technically matter to the evolution...
	 * ... only to the evaluation at playTime.
	 */
	private void setBPM(float f) {
		if (f < 1 || f > 240)		
			return;
		
		bpm = f;
		_changed = true;
		_config.tempo((int) bpm);
		ready = false;
	}
	
	private void setKey(float f)
	{
		Note n = MIDI.numberToNote(f);
		post("SetKey: "+f+", Pitch: "+Pitch.getName(n.pitchClass()));
		_config.rootPitch(n.pitchClass());
		// reset this
		_changed = true;
		ready = false;
	}
	
	private void setPopulationSize(float f)
	{
		if (f < 25 || f > 250)		
			return;
		bpm = f;
		_changed = true;
		populationSize = (int)f;
		System.out.println("Population size updated. You need to reload for these changes to take effect.");
		
	}
	
	/**
	 * Deals with floats arriving at inlets; parameters like
	 * populationSize, numberOfGenerations etc.
	 */
	protected void inlet(float f) 
	{
		switch (getInlet())
		{			
			case 2:
				setKey(f);
				break;
			case 3:
				setBPM(f);
				break;
			case 4: 
				setPopulationSize(f);
				break;
			case 5:
				
				if (evolution != null) {
					evolution.generations((int)f);
					System.out.println("Setting number of generations "+f);
					outlet(2,evolution.generations());
					return;
				}
				System.out.println("PLease boot evolution before setting number of generations");
				break;
			case 6:
				if (popParams == null)
					return;
				try {
					HeuristicSelectionType hType = HeuristicSelectionType.RANDOM;
					if (f == 1) 
						hType = HeuristicSelectionType.EVEN;
															
					popParams.setParameter(PopulationParameters.HEURISTIC_SELECTION_TYPE, hType);
					System.out.println("Updated HEURISTIC_SELECTION_TYPE to "+hType.toString());
				}
				catch (ParameterException e) { ErrorFeedback.handle(e); }				
				break;
			case 7:
				// Gives feedback to the evolution function.
				if (f > 10000)
					_currentSequenceFeedback = 1;
				else if (f < 3000)
					_currentSequenceFeedback = 0;
				System.out.println("Current feedback status: "+_currentSequenceFeedback);
				break;					
			default:
				System.out.println("Unhandled message on inlet " + getInlet());
		}			
	}
	
	/**
	 * Re-evaluates everything in the current queue in a separate thread.
	 * @author Ciarán Rowe (csr2@kent.ac.uk)
	 *
	 */
	private static class QueueLoader implements Runnable 
	{
		Configure current;
		private QueueLoader(Configure c) {
			current = c;
		}
		/**
		 * Executes the QueueLoader.
		 */
		public void run() {
			System.out.println("Re-evaluating queue.");
			current._queue.clear();
			for (NoteSequence n : current._activeNS)
			{
				try { 
					n.validateNotes();
					current._queue.add(n.getNotes());
				}
				catch (ValidationException e) {
					ErrorFeedback.handle(e);
				}
			}
			current._activeNotes.sequenceParameters().Changed = current._changed = false;
		}
	}
	
}
