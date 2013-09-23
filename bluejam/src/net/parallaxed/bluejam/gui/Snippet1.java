package net.parallaxed.bluejam.gui;
/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * example snippet: Hello World
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.graphics.FontData;

public class Snippet1 {

public static void main (String [] args) {
	Display display = new Display ();
	Shell shell = new Shell(display);
	Label text = new Label(shell, SWT.NONE);
	FontRegistry fontReg = new FontRegistry();
	fontReg.put("global", new FontData[] { new FontData("Arial",24,SWT.BOLD)});
	text.setText("Hello, World!");	
	text.setFont(fontReg.get("global"));
	text.pack();
	shell.pack ();
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}

	
	display.dispose ();
}
}
