/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFConsole
{
	private MessageConsole messageConsole;
	
	public MessageConsoleStream getConsoleStream()
	{
		return getConsoleStream("ESP-IDF Console", null, false); //$NON-NLS-1$
	}
	
	public MessageConsoleStream getConsoleStream(String consoleName, URL imageUrl, boolean errorStream)
	{
		// Create Tools console
		MessageConsole msgConsole = findConsole(consoleName, imageUrl);
		msgConsole.clearConsole();
		MessageConsoleStream console = msgConsole.newMessageStream();
		msgConsole.activate();

		// Open console view so that users can see the output
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				openConsoleView();
				if (errorStream) console.setColor(new Color(255, 0, 0));
			}
		});
		
		return console;
	}

	/**
	 * Find a console for a given name. If not found, it will create a new one and return
	 * 
	 * @param name
	 * @return
	 */
	private MessageConsole findConsole(String name, URL imageURL)
	{
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
		{
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, ImageDescriptor.createFromURL(imageURL));
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	protected void openConsoleView()
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		}
		catch (PartInitException e)
		{
			Logger.log(e);
		}
	}
}
