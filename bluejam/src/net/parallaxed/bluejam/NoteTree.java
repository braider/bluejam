package net.parallaxed.bluejam;

import java.util.ArrayList;
import java.util.Iterator;

import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.NoteAddException;
import net.parallaxed.bluejam.exceptions.NullNoteException;
import net.parallaxed.bluejam.exceptions.SequenceException;
import net.parallaxed.bluejam.exceptions.ValidationException;

/**
 * Note trees are a data structure representing an unordered 
 * tree of NoteSequences, itself forming a note sequence.
 * 
 * Trees are walked and flattened into note sequences,
 * representing a series of musical notes that make up a
 * phrase.
 * 
 * Depending on the player, the tree may be walked in different
 * ways, so the order of the notes in the tree is fixed,
 * but that fixed structure may be interpreted in all directions.
 * 
 * Trees are a function, and by most definitions that function is
 * isomorphic to "playing" the tree. When
 * 
 * If the parent of a note tree is null, then the current node
 * is determined to be the root of the tree. No other node in the
 * tree should have a null parent.
 * 
 * There are several operations that can happen to a NoteTree,
 * remove(note), add(note) and swap(notesOut,notesIn). Add and remove 
 * are self explanatory.
 * 
 * Notes cannot be added in arbitrary positions yet, all added notes are
 * appended to the node that executes the add operation.
 * 
 * NoteTree instances may also be Heuristics, effectively 
 * providing the "teaching" basics for the program, guiding 
 * the solo in a certain direction. Evolution cycles may occur 
 * with or without heuristic trees.
 * 
 * Node arity is not limited, each add will create a new branch and
 * the arity will increase.
 * 
 * The arity of the note tree determines the rhythm of the 
 * notes underneath it, so the position of node in a tree defines
 * the duration of that node (not including alterations for 
 * properties such as swing). 
 * 
 * As a result of this, "rhythm" is not a property of notes, it is
 * prescribed by the structure of the note sequence and evaluated at
 * play time into the duration (long millisecs) property.
 * 
 * TODO Change locking mechanism so the Heuristic locks are preserved.
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class NoteTree implements NoteSequence, Heuristic, Function, Cloneable
{


	/**
	 * Rhythms below HEMIQUAVER are not supported, so the
	 * MAX_DEPTH for any NoteTree is 8.
	 */
	public static final int MAX_DEPTH = 8;
	////// EXCEPTIONS
	private static final String W_NULL_CHILD = "Warning, null child at: ";
	private static final String E_NODE_NOT_FOUND = "Node not found in this tree: ";
	private static final String E_TYPE_SWAP = "swapOut must be a referece to node in this NoteTree";
	//////
	// PUBLIC FOR TESTING ONLY, CHANGE THIS
	private NoteSequence[] children;
	
	//////
	private NoteSequence _parent = null;
	
	/**
	 * @return The parent of this NoteSequence
	 */
	public NoteSequence parent() { return _parent; }
	//////
	
	/**
	 * Changed is true on construction to ensure we count the
	 * depth at first add.
	 * 
	 */
	protected boolean _changed = true;
	
	/**
	 * This variable gets updated with the current depth of this
	 * node.
	 */
	private int _depth = 0;
	/**
	 * @return The depth at which this NoteTree node exists relative to the root
	 */
	public int depth() { return _depth; }
	
	/**
	 * @return The rhythm object that this tree will accept in an addNotes() call.
	 */
	public Rhythm acceptedRhythm() {
		return Rhythm.getRhythm((int)Math.pow(2,_depth));
	}
	
	/**
	 * @deprecated
	 * @return The name of this NoteTree (use toString())
	 */
	public String getName() {
		return "";
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SequenceParameters sequenceParameters() {
		if (_parent != null)
			return _parent.sequenceParameters();			
		return _sp;
	}
	
	/**
	 * To allow this to be a heuristic, we must be able to override it's
	 * SequenceParameters object (on construction), since
	 * NoteTrees are cloned from this and need to have a reference to
	 * it after cloning. 
	 */
	public void setSequenceParameters(SequenceParameters sequenceParameters) {
		_sp = sequenceParameters;
		for (NoteSequence child : children)
			if (child != null)
				if (child.getClass() == NoteTree.class)
					((NoteTree) child).setSequenceParameters(sequenceParameters);		
					
	}
	
	private SequenceParameters _sp = null;
	
	public NoteTree() {
		_sp = new SequenceParameters();
		children = new NoteSequence[_sp.length];		
	}
	
	public NoteTree(NoteSequence parent)
	{
		_parent = parent;
		children = new NoteSequence[] { null, null };
		_sp = ((NoteTree)_parent).sequenceParameters();
		countDepth();
		_changed = false;
	}
	
	/**
	 * For instantiating a tree with custom parameters.
	 * 
	 * @param params
	 */
	public NoteTree(SequenceParameters params)	{
		_sp = params;
		if (_sp == null)
		{
			_sp = new SequenceParameters();
			ErrorFeedback.handle("Warning: NoteTree instantiated with null SequenceParameters", new Exception());
		}
		children = new NoteSequence[_sp.length];
		countDepth();
	}
	
	/**
	 * Returns the contents of the note tree (all notes that
	 * extend from the tree as children included). Included to 
	 * satisfy implementation of Function
	 * 
	 * @see Function
	 * 
	 */
	public NoteSequence getNoteSequence() {
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This determines how the note will be played.
	 */
	public Iterator<Note> getNotes()
	{
		NoteCollection n = new NoteCollection();
		for (int i = 0; i<children.length; i++)
		{
			if (children[i] instanceof Terminal) {
				n.addNotes(children[i]);
			}
			if (children[i] instanceof Function) {
				Iterator<Note> notes = children[i].getNotes();
				
				// while loop must be on the outside so the add operation can
				// continue if it encounters an exception.
				while (notes.hasNext())
				{
					try {
						n.addNotes((NoteSequence) notes.next());
					}
					catch (ClassCastException e) {
						throw new RuntimeException("GetNotes failed - invalid cast to NoteSequence:" +n.toString());
					}
				}
			}
		}
		return n.iterator();
	}
		
	public boolean contains(NoteSequence n)
	{
		if (this == n)
			return true;
		for (NoteSequence c : children)
		{
			if (c != null)
				if (c.contains(n))
					return true;
		}			
		
		return false;
	}
	
	/**
	 * Functions as a basic find/replace for tree mutations and
	 * other genetic operators like 1-point crossover.
	 * 
	 * The first argument is searched for in the children of 
	 * this node, if that object it found, it is detached
	 * from it's parent and the swapIn parameter
	 * replaces it.
	 * 
	 * Whatever method calls this should take care of setting
	 * placing the swapOut NoteSequence back in the right place.
	 * 
	 * swapOut should be a reference to some node in this note tree.
	 * 
	 * swapIn will be converted to a NoteTree if it is not already.
	 * 
	 * @param swapOut The NoteSequence to replace in the tree.
	 * @param swapIn The new NoteSequence to place in the position of swapIn.
	 */
	public boolean swapNotes(NoteSequence swapOut, NoteSequence swapIn) throws SequenceException
	{
		if ((swapOut.getClass() != NoteTree.class) && (swapOut.getClass() != NoteLeaf.class))	{
			ErrorFeedback.handle(E_TYPE_SWAP, new NoteAddException(swapIn));
			return false;
		}
		
		// There's NO point swapping at depth = 0 - we can't anyway.
		if (swapOut.getClass() == NoteTree.class)
		{
			if (((NoteTree) swapOut).depth() < 1)
				return false;
		}
		
		for (int i = 0; i < children.length; i++)
		{
			if (children[i] == swapOut)
			{
				if (swapIn.getClass() == NoteTree.class)	
					((NoteTree)swapIn)._parent = this;
				else if (swapIn.getClass() == NoteLeaf.class)
					((NoteLeaf)swapIn)._parent = this;
				children[i] = swapIn;
				return true;
			}			
		}
		
		for (int i = 0; i < children.length; i++)
			if(children[i] != null)
				if (children[i].swapNotes(swapOut, swapIn))
					return true;
		if (_parent == null)
			ErrorFeedback.handle(E_NODE_NOT_FOUND+swapOut.toString(), new NoteAddException(swapIn));
		return false;
		
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This function will add notes until the tree is full.
	 * 
	 * Currently optimized to a lookahead depth of 2.
	 * 
	 * 
	 * // NB: THIS METHOD _SHOULD_ ONLY CALL ITSELF WITH A SINGLE NOTE
	 *  - but it doesn't matter if there's more than 1...
	 */
	
	public boolean addNotes(NoteSequence notes) throws SequenceException
	{		
		if (_changed)
			countDepth();
		// Inexpensive optimization gamble... (find out if we have empty children)
		int emptySlot = hasEmptyChildren();
		Iterator<Note> i = notes.getNotes();
		
		noteAdd:				
			while (i.hasNext())
			{				
				try {
				NoteLeaf n = (NoteLeaf)i.next();
				int rhythmDepth = rhythmDepth(n.rhythm());
				if (rhythmDepth < _depth)
					throw new NoteAddException(n);
				// TODO Optimize n-levels above target depth.
				// OPTIMIZE - primitive lookahead (are we two above target depth,
				// and do we have no children?)
				if ((_depth+2 == rhythmDepth) && emptySlot > -1)
				{					
					int firstChild = 0;
					if (emptySlot > 0)
						// try and force an add on all children before the empty slot
						while (firstChild < emptySlot)
							if (((NoteTree)children[firstChild]).addNote(n,((NoteTree)children[firstChild++]).hasEmptyChildren()))
								continue noteAdd;
					
					// otherwise, create a tree in the empty slot and add this note.
					children[emptySlot] = new NoteTree(this);				
					((NoteTree)children[emptySlot]).addNote(n,0);
					continue noteAdd;
				}
				/// END OPTIMIZE	
				// otherwise, is this node above the target depth?				
				if (_depth+1 == rhythmDepth)
				{
					// We must add to one of our children, this is the
					// right depth.
					for (int c = 0; c < children.length; c++)
					{
						if (children[c] == null) {
							children[c] = n;
							n._parent = this;
							continue noteAdd;
						}
					}
					// We shouldn't be here... means we couldn't add the note :(
					throw new NoteAddException(n);
				}
				
				// the parent has passed this note down too far :(
				if (_depth == rhythmDepth)					
					throw new NoteAddException(n);
				
				// This is not the right depth, pass the note down
				for (int c = 0; c< children.length;c++)
				{
					if (children[c] == null) 
						children[c] = new NoteTree(this);
					if (children[c].getClass() != NoteLeaf.class)
						if (children[c].addNotes(n))
							continue noteAdd;
				}
				throw new NoteAddException(n);
			}		
			catch (ClassCastException e) { 
				return false;
			}
			catch (SequenceException e)
			{				
				// If we're not at the root, maybe our parent can
				// deal with it - 
				if (_parent != null)
					return false;
				
				// If we are at the parent, there's not much left to do :(
				// this tree cannot accept the passed note sequence.
				ErrorFeedback.handle("Tree "+this.toString()+" is full at depth "+e.getRhythm().toString() ,e);
			}
		}
		return true;
	}
	
	
	/**
	 * If this tree has empty children, return the index
	 * of the first empty slot.
	 * @return Index of the first empty child.
	 */
	private int hasEmptyChildren()
	{
		for (int i = 0; i < children.length; i++)
			if (children[i] == null)
				return i;
		return -1;
	}
	
	/**
	 * Allows overriding behaviour, to add a note to a particular
	 * childIndex of a NoteTree. This method should only ever be 
	 * called by the parent, _knowing_ that the NoteTree is newly
	 * instantiated. 
	 * 
	 * This forces out other children and can leave the tree
	 * unbalanced if used improperly.
	 * 
	 * @param note The NoteLeaf to insert as a child of this node.
	 * @param childIndex The index at which to insert this note.
	 */
	protected boolean addNote(NoteSequence note, int childIndex)
	{
		if (childIndex == -1)
			return false;
		children[childIndex] = note;
		((NoteLeaf)note)._parent = this;
			return true;
	}
	
	/*
	 * @deprecated
	
	private void initializeChildren()
	{
		if (_parent != null )
			children = new NoteSequence[2];
		else
			children = new NoteSequence[4];
	}
	 */
	/**
	 * Tree depth is calculated by taking the logarithm
	 * of the rhythmic fraction to the base 2, and adding 1.
	 * 
	 * i.e. 2^3 = 8 (giving us depth 3 for a quaver).
	 * 
	 * We add 1 to compensate for the total length of the phrase
	 * starting at depth 0 (tree root).
	 * 
	 * This way, a whole note always starts at depth 1, not
	 * depth zero, since:
	 * 
	 * 2^0 = 1 (giving us depth 0 for a whole note)
	 * 
	 * @param r A rhythm enum representing the value of the 
	 * @return An integer representing the tree depth.
	 */
	protected int rhythmDepth(Rhythm r) {		
		return (int) (Math.log(r.eval())/Math.log(2)) + 1;
	}
	
	/**
	 * Given this tree's parameters, traces the route to the
	 * parent and counts how deep this node is.
	 */
	private int countDepth()
	{
		_changed = false;
		if (_parent == null)
			return 0;
		try {
			NoteTree parent = (NoteTree) _parent;
			if (_parent != this)
				return (_depth = parent.countDepth()+1);
			else
			{
				ErrorFeedback.handle("Error in countDepth()",new Exception("NoteTree is the parent of itself!"));
				return _depth;
			}
		}
		catch (ClassCastException e) {
			return 0;
		}
	}
	
	/**
	 * Deletes a branch from this Node and left-shifts
	 * all remaining children.
	 */
	public void removeNotes(NoteSequence notes) 
	{
		if (notes == this)
		{
			_parent = null;
			return;
		}
		
		for (int i = 0; i < children.length; i++)
		{
			if (children[i] == notes)
			{
				children[i].removeNotes(notes);
				children[i] = null;				
				return;
			}			
		}
		
		for (NoteSequence _n : children)
			if (_n != null)
				_n.removeNotes(notes);
	}
	
	/**
	 * Simply calls validateNotes on all children and ensures
	 * their rhythms add up to _sp.length whole notes.
	 */
	public void validateNotes() throws ValidationException {
		for (int i = 0; i < children.length; i++) {
			if (children[i] != null)
				children[i].validateNotes();
			else 
				ErrorFeedback.handle(W_NULL_CHILD+i, new NullNoteException(this));
		}
		
		/// FIXME UNSTABLE : Very not threadsafe 
		if (_parent == null)		
			sequenceParameters().Changed = false;
		
	}
	
	//////////////////////////
	////// EVOLUTION FUNCTIONS
	////// These methods can be typed because they are only
	////// accessed by algorithms with knowledge of the structure.
	
	/**
	 * Used during evolution to get all the references we need
	 * to find nodes that can be mutated.
	 * 
	 * @return An array of NoteLeaf instances
	 */
	public NoteLeaf[] getMutationReferences() {

		return  _getMutationReferences().toArray(new NoteLeaf[0]);
	}
	
	private ArrayList<NoteLeaf> _getMutationReferences()
	{
		ArrayList<NoteLeaf> mutableLeaves = new ArrayList<NoteLeaf>();
		for (NoteSequence _child : children)
		{
			if (_child != null && _child.getClass() == NoteLeaf.class)
			{
				NoteLeaf _nl = (NoteLeaf) _child;
				if ((_nl.mutable() & Mutable.ALL) != Mutable.NONE)
					mutableLeaves.add((NoteLeaf)_child);
			}
			else if (_child != null &&  _child.getClass() == NoteTree.class)
				mutableLeaves.addAll(((NoteTree)_child)._getMutationReferences());
		}
		return mutableLeaves;
	}
	/**
	 * Used during evolution to get all the references to NoteTree
	 * nodes that can undergo Crossover using the swapNotes() method.
	 * @return An array of NoteSequenceInstances that are candidates for crossover.
	 */
	public NoteSequence[] getCrossoverReferences() {
		
		boolean _returnChildList = false;
		ArrayList<NoteSequence> _ns = new ArrayList<NoteSequence>();
		
		for (NoteSequence n : children)
		{
			try {
				if (n == null)
					continue;
				if (n.getClass() == NoteTree.class)
				{
					NoteTree _nt = (NoteTree) n;
					
					// if all children return themselves, then return itself
					// otherwise return the list of children.
					NoteSequence[]  _crossoverRef =  (NoteSequence[]) _nt.getCrossoverReferences();				
					if (_crossoverRef.length != 1 || _crossoverRef[0] != _nt)
						_returnChildList = true;
					
					for (NoteSequence crossoverPoint : _crossoverRef)
						if (crossoverPoint != null)
							_ns.add(crossoverPoint);
					
					// NOT GREEDY, will go to the minimum depth.
				}
				else if (n.getClass() == NoteLeaf.class)
				{
					NoteLeaf _nl = (NoteLeaf) n;
					byte _perms = _nl.mutable();
					if ((_perms & Mutable.ALL) == Mutable.ALL)
					{
						_ns.add(_nl);
						continue;
					}
					else					
						_returnChildList = true;					
					//continue;	
				}
				else 
					// The child is of some other type, so there's not much we can
					// do. Don't return anything.
					continue;
				
				
			}
			catch (ClassCastException e) {
				ErrorFeedback.handle(e.getMessage(), e);
			}
		}
		_ns.trimToSize();
		return (_returnChildList ?  _ns.toArray(new NoteSequence[1]) : new NoteSequence[] {this});
	}
	
	/**
	 * This method returns an array of references to 
	 * nodes in the NoteTree with null children. 
	 * 
	 * A reference is returned for each null child, so for
	 * NoteTrees with multiple empty children, multiple
	 * references will be returned.
	 *  
	 * @return An array of references to nodes with null children.
	 */
	public NoteSequence[] getIncompleteReferences() 
	{
		return _getIncompleteReferences().toArray(new NoteSequence[0]);
	}
	/**
	 * Recursive collector helper method.
	 * @return An ArrayList of Nodes with null children.
	 */
	private ArrayList<NoteSequence> _getIncompleteReferences()
	{
		ArrayList<NoteSequence> notes = new ArrayList<NoteSequence>();
		for (NoteSequence n : children)
		{
			if (n == null) {
				notes.add(this);
				continue;
			}
			if (n.getClass() == NoteTree.class)	{				
				notes.addAll(((NoteTree)n)._getIncompleteReferences());
			}
		}
		return notes;
	}
	
	/**
	 * @param index The index at which to retrieve the child
	 * @return The child at the passed index
	 */
	public NoteSequence getChild(int index) {
		return children[index];
	}
	
	public NoteSequence[] getSubTrees() {
		return _getSubTrees().toArray(new NoteSequence[0]);
	}
	
	/**
	 * Do not return the leaves
	 * @return
	 */
	private ArrayList<NoteSequence> _getSubTrees() {
		ArrayList<NoteSequence> _children = new ArrayList<NoteSequence>();
		for (NoteSequence child : children)
			if (child != null && child.getClass() == NoteTree.class)
				_children.addAll(((NoteTree)child)._getSubTrees());
		_children.add(this);
		return _children;
				
				
	}
	/**
	 * @return The number of children this node has
	 */
	public int getNumChildren() {
		return children.length;
	}
	

	/**
	 * Returns a cloned NoteTree.
	 * 
	 * Every node in the note tree will be replaced by copies
	 * of those nodes (this method is recursive).
	 * 
	 * @return A full copy of the note tree.
	 */
	public NoteTree clone()
	{
		NoteTree _nt = new NoteTree();
		_nt._changed = _changed;
		_nt._sp = _sp;
		_nt._parent = _parent;
		_nt._depth = _depth;
		
		_nt.children = new NoteSequence[children.length];
		for (int i = 0; i<children.length; i++)
		{
			try { 
				NoteSequence n = children[i];
				if (n == null)
					continue;
				
				if (n.getClass() == NoteTree.class)	{
					_nt.children[i] = ((NoteTree) n).clone();					
					((NoteTree)_nt.children[i])._parent = _nt;
					((NoteTree)_nt.children[i])._sp = _sp;
					continue;
				}
				
				if (n.getClass() == NoteLeaf.class) {
					_nt.children[i] = ((NoteLeaf) n).clone();
					((NoteLeaf)_nt.children[i])._parent = _nt;
					continue;
				}
					
				if (n instanceof NoteSequence)	{
					_nt.children[i] = n.clone();					
				}
			}
			catch (CloneNotSupportedException e) {
				return null;
			}			 
		}
		return _nt;	
	}
}