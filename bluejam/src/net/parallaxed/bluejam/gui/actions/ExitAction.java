package net.parallaxed.bluejam.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.ApplicationWindow;

public class ExitAction extends Action
{
	ApplicationWindow _w = null;
	public ExitAction(ApplicationWindow w)
	{
		_w = w;
		setText("E&xit@Alt+F4");
		setToolTipText("Exit BlueJam");		
	}
	
	public void run()	
	{
		_w.close();
	}
}
