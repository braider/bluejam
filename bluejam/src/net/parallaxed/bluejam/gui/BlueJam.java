package net.parallaxed.bluejam.gui;

import net.parallaxed.bluejam.gui.actions.ExitAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This class starts the BlueJam GUI.
 * 
 * You don't need this if you're using the PD interface.
 * 
 * This class is intended to start the program and begin
 * evolution. MIDI output pending as it appears to be
 * broken (a bit) in Java.
 * 
 * Ideally you'll be able to start the evolution process and
 * visualize the generated melodies.
 * 
 * The program will pick one melody from each generation, this
 * is not part of any elitist method, but each melody chosen 
 * gets remembered in the program's "working memory", which
 * is then used to evaluate further phrases.
 * 
 * Please see the evolution classes for more details on the 
 * process.
 * 
 * @param args This program takes no arguments (yet...)
 */
public class BlueJam extends ApplicationWindow {

	public BlueJam()
	{
		super(null);		
		addMenuBar();
		setBlockOnOpen(true);		
		open();
		Display.getCurrent().dispose();
	}
	
	protected MenuManager createMenuManager()
	{
		MenuManager m = new MenuManager();
		MenuManager file = new MenuManager("&File");
		m.add(file);
		file.add(new ExitAction(this));
		return m;
	}
	
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText("BlueJam");
	}
	
	protected Control createContent(Composite parent)
	{
		Composite c = new Composite(parent,SWT.NONE);
		c.setLayout(new GridLayout());
		
		return c;
	}
	
	/**
	 * Starts the BlueJam SWT GUI (Very Minimal at the moment).
	 * @param args Nothing (yet).
	 */
	public static void main(String[] args) {		
		new BlueJam();
	}

}
