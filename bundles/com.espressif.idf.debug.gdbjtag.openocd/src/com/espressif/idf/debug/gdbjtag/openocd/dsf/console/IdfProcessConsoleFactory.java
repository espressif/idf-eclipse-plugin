/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.dsf.console;

import java.nio.charset.Charset;

import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;

/**
 * Process console factory class to register the console with eclipse and also invoke it
 * @author Ali Azam Rana
 *
 */
public class IdfProcessConsoleFactory implements IConsoleFactory
{
	private static IdfProcessConsole idfProcessConsole;
	
	public static IdfProcessConsole showAndActivateConsole(Charset charset)
	{
		if (idfProcessConsole == null)
		{
			idfProcessConsole = new IdfProcessConsole(Charset.forName(WorkbenchEncoding.getWorkbenchDefaultEncoding()));
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { idfProcessConsole });
		}
		
		idfProcessConsole.activate();
		
		return idfProcessConsole;
	}

	@Override
	public void openConsole()
	{
		if (idfProcessConsole == null)
		{
			idfProcessConsole = new IdfProcessConsole(Charset.forName(WorkbenchEncoding.getWorkbenchDefaultEncoding()));
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { idfProcessConsole });
		}
		
		idfProcessConsole.activate();
	}
}
