/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.util;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;

public class ConsoleManager
{

	private ConsoleManager()
	{
	}

	public static MessageConsole getConsole(String consoleName)
	{
		MessageConsole console = findConsole(consoleName);
		if (console == null)
		{
			console = new MessageConsole(consoleName, null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		}
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
		return console;
	}

	private static MessageConsole findConsole(String consoleName)
	{
		for (IConsole existing : ConsolePlugin.getDefault().getConsoleManager().getConsoles())
		{
			if (consoleName.equals(existing.getName()))
			{
				return (MessageConsole) existing;
			}
		}
		return null;
	}
}
