package net.parallaxed.bluejam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.ParsingException;
import net.parallaxed.bluejam.exceptions.PitchException;
import net.parallaxed.bluejam.exceptions.SequenceException;
import net.parallaxed.bluejam.exceptions.ValidationException;

/**
 * This class reads in information from passed tree files,
 * which normally represent a serialized layout of a NoteSequence.
 * These files come in two flavours, .heuristic and .tree, each
 * specifying roughly the same parameters. The different extension
 * is merely a semantic pragma to inform the user what the file
 * contains (a heuristic, or just a tree).
 * 
 * .tree files may vary in what they contain, but they are normally serial stores
 * of tree structures at a certain point in the evolution (they
 * represent trees assigned to individuals) - or they are arbitrarily
 * created files used for testing Tree parser provides only one method
 * for accessing the parsed tree. The user can call getNoteTree() 
 * and cast the result to whatever is desirable. Errors during parsing
 * are printed out to the terminal.
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class TreeParser 
{
	private static final long serialVersionUID = 1;
	private File _treeFile = null;
	private static final String E_FILE_NOT_FOUND_EXCEPTION = "Cannot read from file";
	private static final String E_IO_EXCEPTION = "Cannot read from file";
	private static final String E_VALIDATION_EXCEPTION = "Error parsing note";
	private static final String E_PITCH_NOT_FOUND = "Pitch not found in Pitch.stringPitch map";
	private static final String W_VERSION_STRING = "WARNING: Parsed version string different from this class";
	private int lineNumber = 1;
	private NoteTree _nt = new NoteTree();

	/**
	 * An additional structure to find notes of certain id's later.
	 */
	private HashMap<Integer,NoteLeaf> notes = new HashMap<Integer, NoteLeaf>();
	
	/**
	 * Creates a tree parser given a relative path.
	 * @param treeFile The file to parse.
	 */
	public TreeParser(String treeFile)
	{		
		readFile(treeFile);
		parseFile(_treeFile);
	}
	
	/**
	 * Creates a TreeParser given a reference to a java.io.File object.
	 * @param treeFile
	 */
	public TreeParser(File treeFile)
	{
		if (treeFile == null || !treeFile.canRead())
			throw new RuntimeException("Cannot read from the supplied tree file: "+treeFile.getAbsolutePath());
		_treeFile = treeFile;
		parseFile(_treeFile);
	}
	
	/**
	 * Parses the .heuristic file and builds an internal 
	 * tree representation of it.
	 * 
	 * @param treeFile The tree file to parse.
	 */
	private void parseFile(File treeFile)
	{		
		try {
			FileReader fr = new FileReader(treeFile);
			BufferedReader br = new BufferedReader(fr);
			NoteCollection leaves = new NoteCollection();
			boolean heuristic = false;
			
			while (br.ready())
			{
				String line = br.readLine();
				if (line.startsWith("#"))
				{
					lineNumber++;
					continue;
				}
				StringTokenizer st = new StringTokenizer(line,"\n\r;");
				
				while (st.hasMoreTokens())			
				{
					NoteLeaf n = processNote(st.nextToken());
					if (n == null)
						continue;
					if (n.isRelative() && heuristic == false)
						heuristic = true;
					leaves.add(n);					
				}
				lineNumber++;
			}					
		
			buildTree(leaves);
		}
		catch (FileNotFoundException e)
		{
			ErrorFeedback.handle(E_FILE_NOT_FOUND_EXCEPTION+ ": " + treeFile.getAbsolutePath(), e);
		}
		catch (IOException e) {
			ErrorFeedback.handle(E_IO_EXCEPTION + ": " + treeFile.getAbsolutePath(), e);
		}
		catch (ParsingException e) {
			if (e.getMessage().length() > 0) {
				ErrorFeedback.handle(e.getMessage(),e);
				return;
			}
			ErrorFeedback.handle(E_VALIDATION_EXCEPTION + " on line " + lineNumber +
					" of file: "+ treeFile.getAbsolutePath(), e);
		}
	}	
	
	private void buildTree(NoteCollection notes) throws ParsingException {
		buildTree(notes, _nt);
	}
	
	/**
	 * Postprocessing. Builds a tree from the given NoteCollection,
	 * using the AddNotes method of the passed NoteTree.
	 * 
	 * There should be NoteLeaf's in the note collection so we can
	 * specify rhythm. These are tacitly added to the tree with minimal
	 * validation.
	 * 
	 * @param notes The NoteCollection to add to the NoteTree
	 * @throws ParsingException If there is a problem casting notes in NoteCollection to type NoteLeaf
	 */
	private void buildTree(NoteCollection notes, NoteTree _nt) throws ParsingException
	{
		for (Note note : notes)
		{
			try {
				
				NoteLeaf n = (NoteLeaf) note;
				
				if (n.swingPartnerId() > -1) {
					Note swingPartner = notes.getIndex(n.swingPartnerId());
					// We've reciprocally processed this not already.
					if (swingPartner._swingPartner == null)
						n.swingNote(n._swingPercent, swingPartner);
				}
				_nt.addNotes(n);
			}
			catch (ClassCastException e) {
				throw new ParsingException("Incorrect type cast in file",_treeFile);
			}
			catch (SequenceException e)	{
				System.out.println(e.toString());
			}
		}		
	}
	 
	/**
	 * Processes a given note - passed from a line in the
	 * .heuristic file.
	 * @param note A string representing note data.
	 */
	private NoteLeaf processNote(String note) throws ParsingException
	{		
		StringTokenizer st = new StringTokenizer(note,")");		
		boolean hasTokens = false;
		Integer id = 0;
		Integer swingPercent = -1;
		Integer swingPartnerId = -1;
		Rhythm rhythm = null;	
		Pitch pitchClass = null;
		int pitchRelative = 0;
		boolean rest = false;
		int lockMask = 3;
		int octave = 4;		
		
		while (st.hasMoreTokens())
		{
			hasTokens = true;
			String token = st.nextToken();
			
			int comma = token.indexOf(',');
			
			// note ID
			if (token.matches("i\\(.*")) {
				id = Integer.parseInt(token.substring(2));
				if (notes.get(id) != null)
					throw new ParsingException("Duplicate note ID in tree "+_treeFile.getAbsolutePath()+" on line "+lineNumber);
				continue;
			}
			
			// swing
			if (token.matches("s\\(.*")) {			
				swingPercent = Integer.parseInt(
						token.substring(2,((comma = token.lastIndexOf(',')) == -1 ? token.length() : comma))
					);
				if (comma > 0)
					swingPartnerId = Integer.parseInt(token.substring(comma+1));
				continue;
			}
			
			// rhythm
			if (token.matches("r\\(.*")) {		
				rhythm = Rhythm.getRhythm(token.substring(2));
				continue;				
			}
			
			// pitch
			if (token.matches("p\\(.*")) {				
				pitchClass = Pitch.getPitch(token.substring(2,3));
				if (comma > 0)
					pitchRelative = Integer.parseInt(token.substring(comma+1));
				continue;				
			}
			
			// rest
			if (token.matches("re\\(.*"))	{
				if (Integer.parseInt(token.substring(3)) == 1)
					rest = true;
				continue;
			}
			
			// lock mask
			if (token.matches("m\\(.*")) {
				lockMask = Integer.parseInt(token.substring(2));
				continue;
			}
			
			// octave
			if (token.matches("o\\(.*")) {
				octave = Integer.parseInt(token.substring(2));
				continue;				
			}
			
			if (token.matches("v\\(.*")) {
				int version = Integer.parseInt(token.substring(2));
				if (version != serialVersionUID) 
					ErrorFeedback.handle(W_VERSION_STRING, new Exception());
				return null;
			}
		}
		
		// we've reached a point and we have a note with Rhythm...
		// insert it into a tree	
		if (rhythm != null)
		{	
			NoteLeaf n = new NoteLeaf(rhythm);
			n.id = id;
			n.octave(octave);
			if (pitchClass == null)	{
				ErrorFeedback.handle(E_PITCH_NOT_FOUND, new PitchException(n));
				throw new ParsingException();
			}
			n.pitchClass(pitchClass);
			n.pitchRelative(pitchRelative);
			try {
				n.lockMask(lockMask,true);
			}
			catch (ValidationException ex) 
			{ // don't need to do anything here, just swallow the impending
			  // exception if the pitch is relative.				
			}
			n.toggleRest(rest);
			n.swingNote(swingPercent, swingPartnerId);
			notes.put(n.id, n);
			return n;
		}
		
		if (!hasTokens)
			throw new ParsingException();
		return null;
	}	
	
	/**
	 * Reads in the tree file from the supplied path.
	 * 
	 * (both .heuristic and .tree files are read)
	 * 
	 * @param path Absolute path to the heuristic file.
	 */
	private void readFile(String path)
	{
		try {
			_treeFile = new File(path);			
		}
		catch (Exception e)	{
			ErrorFeedback.handle(e.getMessage(), e);
			//throw new RuntimeException("Cannot read from the supplied tree file: "+path);
		}
	}
	
	/**
	 * @return The parsed note tree, can be cast to the desired type. 
	 */
	public NoteTree getNoteTree() {
		return _nt;
	}
}