package net.parallaxed.bluejam.grammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import net.parallaxed.bluejam.Pitch;
import net.parallaxed.bluejam.exceptions.ErrorFeedback;
import net.parallaxed.bluejam.exceptions.ParsingException;

/**
 * This class parses flat-file Markov models which can be used 
 * by sets during the selection process to assign proportionate
 * probabilities to possible notes in the scale.
 * 
 * Models describe what's likely to happen given note X has 
 * already happened.
 * 
 * @see PitchModel
 * 
 * @author Ciarán Rowe (csr2@kent.ac.uk)
 *
 */
public class ModelParser {
	
	private static String E_FILE_NOT_FOUND_EXCEPTION = "Cannot read from file";
	private static String E_IO_EXCEPTION = "Cannot read from file";
	private static String E_VALIDATION_EXCEPTION = "Error parsing model";
	private static String E_UNKNOWN_TOKEN= "Unknown token on line ";
	
	private File _modelFile = null;
	private int lineNumber = 1;
	private PitchModel _model = null;
	
	/**
	 * Constructs a ModelParser. Use getModel() to extract
	 * the parsed model.
	 * 
	 * @param modelFile The absolute path to the model file.
	 * 
	 */
	public ModelParser(String modelFile)
	{	
		if (modelFile.length() > 0)
		{
			String name = modelFile.substring(modelFile.lastIndexOf('\\')+1);
			_model = new PitchModel(name);
			readFile(modelFile);
			parseFile(_modelFile);
		}
	}
	
	/**
	 * Constructs a ModelParser. Use getModel() to extract
	 * the parsed model.
	 * 
	 * @param modelFile A reference to a File object for the model file.
	 * 
	 */
	public ModelParser(File modelFile)
	{
		if (modelFile == null || !modelFile.canRead())
			throw new RuntimeException("Cannot read from the supplied model file: "+modelFile.getAbsolutePath());
		String fileName = modelFile.getName();
		String name = fileName.substring(fileName.lastIndexOf('\\')+1);
		
		_model = new PitchModel(name);
		_modelFile = modelFile;
		parseFile(_modelFile);
	}
	
	/**
	 * Takes one line of the model. Will attempt to find a pitch
	 * before subsequently parsing all other tokens into an 
	 * instance of double.
	 * 
	 * Parts are tokenized by space or \t (tab).
	 * 
	 * The model is checked for consistency later.
	 */
	private void processModel(String model) throws ParsingException
	{
		StringTokenizer st = new StringTokenizer(model," \t");
		Pitch p = null;
		double[] probabilites = new double[st.countTokens() - 1];
		int i = 0;
		
		while (st.hasMoreTokens())
		{
			String _st = st.nextToken();
			if (_st.length() < 1)
				continue;
				
			if (p == null)
			{
				if (_st.matches("[a-zA-Z]+"))					
					// we have a pitch - begin parsing a model
					p = Pitch.getPitch(_st);
				continue;
			}
			if (_st.matches("\\d.*"))
				probabilites[i++] = Double.parseDouble(_st);
			else 
				ErrorFeedback.handle(E_UNKNOWN_TOKEN+lineNumber,new Exception(E_UNKNOWN_TOKEN));
		}
		
		_model.put(p, probabilites);
		
		if (p == null)
			return;
	}
	
	/**
	 * Will load one line at a time from modelFile and
	 * call processModel with it. Lines are tokenized by
	 * \r, \n or ;
	 * 
	 * @param modelFile
	 */
	private void parseFile(File modelFile)
	{		
		try {
			FileReader fr = new FileReader(modelFile);
			BufferedReader br = new BufferedReader(fr);
			
			while (br.ready())
			{
				String line = br.readLine();
				if (line.startsWith("#"))
					continue;
				
				// Ensures we capture ; separation
				StringTokenizer st = new StringTokenizer(line,"\n\r;");
				
				while (st.hasMoreTokens())			
				{
					processModel(st.nextToken());
				}				
				lineNumber++;
			}
			_model.validateModel();
		}
		catch (FileNotFoundException e)
		{
			ErrorFeedback.handle(E_FILE_NOT_FOUND_EXCEPTION+ ": " + modelFile.getAbsolutePath(), e);
		}
		catch (IOException e) {
			ErrorFeedback.handle(E_IO_EXCEPTION + ": " + modelFile.getAbsolutePath(), e);
		}
		catch (ParsingException e) {
			if (e.getMessage().length() > 0) {
				ErrorFeedback.handle(e.getMessage(),e);
				return;
			}
			ErrorFeedback.handle(E_VALIDATION_EXCEPTION + " on line " + lineNumber +
					" of file: "+ modelFile.getAbsolutePath(), e);
		}
	}
	

	/**
	 * Reads in the model file from the supplied path.
	 * 
	 * @param path Absolute path to the heuristic file.
	 */
	private void readFile(String path)
	{
		try {
			_modelFile = new File(path);			
		}
		catch (Exception e)	{
			ErrorFeedback.handle(e.getMessage(), e);
		}
	}
	
	/**
	 * @return The model that has been parsed by this ModelParser, or null if no Model has been parsed.
	 */
	public PitchModel getModel()
	{
		return _model;
	}
}
