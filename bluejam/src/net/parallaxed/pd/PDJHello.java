package net.parallaxed.pd;


import com.cycling74.max.*;

/**
 * A "Hello World"-esque MaxObject based on pdj_test_class.
 * 
 * Pumps a float out of the outlet every clock tick, similar
 * to the PD "metro" object.
 * 
 * Two Inlets are defined. The hot inlet on the left side
 * reacts to a "bang" (the "do it" imperative in PD). The
 * cold inlet on the right hand side accepts a value 
 * representing the BPM at which the metronome should tick.
 * 
 * Read here to understand the difference between hot and
 * cold inlets. 
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 */

public class PDJHello extends MaxObject implements Executable {
	
	// Clock object.
	MaxClock clock;
	// Is the metronome on?
	boolean active = false;	
	// Beats per minute.
	float bpm = 120;
	
	/** 
	 * Constructs a MaxObject for PD that accepts a float on
	 * the cold inlet (BPM), and a bang on the hot inlet (on/off).
	 * 
	 * Sends a float on the outlet using the clock to tick
	 * at the specified BPM.
	 */
	public PDJHello() {
		// declares a new clock.
		clock = new MaxClock(this);
		// sets the inlet data types.
		int[] types = { DataTypes.ANYTHING,DataTypes.FLOAT };
		this.declareInlets(types);
		// sets this object as the executable for each clock tick.
		clock.setExecutable(this);
		post("If you can see this, everything loaded correctly. Click bang to start.");
	}
	/**
	 * Constructor used when arguments are passed to the
	 * object.
	 * 
	 * This doesn't do anything special except handle
	 * for the case where arguments are passed to the 
	 * constructor. You can pass the arguments by making
	 * the object with "pdj PDHello arg1 arg2 [...]" etc.
	 * 
	 * @param args An array of Atom objects.
	 */
	public PDJHello(Atom args[]) {
		// executes the trivial constructor
		this();
		// print out any args to the PD terminal.
		
		for (int i=0;i<args.length;i++) {
			post("arg[" + i +"]:" + args[i].toString());
		}
	}
	
	/**
	 * Implements the method executed by MaxClock every clock
	 * tick. 
	 * 
	 * Each tick plays out a note if the metronome is active,
	 * and sets a new delay on the clock - this is effectively
	 * a recursive method (it calls itself) while we're in an
	 * active state.
	 */
	public void execute() 
	{
		// if we're not active don't do anything.
		if (active)
		{
			post("tick.");
			float note = 69;
			int outlet = 0;
			// send the note value out of the outlet.
			this.outlet(outlet,note);
			// schedule another tick.
			float delay = (1000 / (bpm / 60));
			clock.delay(delay);
		}
	}
	
	/**
	 * Called when the object receives a bang from PD.
	 * 
	 * Bang objects in PD are a metaphor for non-latching
	 * switches.
	 * 
	 * This effectively makes them a boolean object that
	 * can exhibit on/off push-switch like behaviour
	 * (without temporal dimensions, i.e. you can't hold
	 * a bang over time). 
	 * 
	 *  A "bang" can do a variety of things, but it's helpful
	 *  to analogize with the view that bang means "do it" in PD. 
	 * 
	 * Will activate/deactivate the metronome.
	 */
	protected void bang()
	{
		post("banged.");		
		// toggles the metronome on or off.
		active = !active;
		
		float delay = (1000 / (bpm / 60));		
		clock.delay(delay);	
	}
	
	/**
	 * Called when any float type inlet receives a message.
	 * 
	 * Cleverly, PDJ lets you clarify which of the float
	 * inlets (if you have more than one) received the message
	 * by using getInlet()
	 * 
	 * In this case, the float inlet is cold (not in the top left)
	 * As a rule, cold inlets don't produce anything on a hot
	 * outlet, but they are regularly used to change the internal
	 * state of an object.
	 * 
	 * @param f A number representing the BPM.
	 * 
	 */
	protected void inlet(float f) 
	{
		// turns it off if the BPM is < 1.
		if (f < 1)
		{
			active = false;
			return;
		}
		// thus BPM is always > 1.
		this.bpm = f;		
	}
}
