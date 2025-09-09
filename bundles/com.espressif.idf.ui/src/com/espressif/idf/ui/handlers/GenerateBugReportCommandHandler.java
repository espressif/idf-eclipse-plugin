/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD.
 * All rights reserved. Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.bug.BugReportGenerator;
import com.espressif.idf.core.bug.GithubIssueOpener;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.update.Messages;

/**
 * Generates a bug report by invoking existing handlers that log details to the ESP-IDF console, waits deterministically
 * for their jobs to finish, and then captures the relevant console output slices.
 * 
 * @author Ali Azam Rana
 *
 */
public class GenerateBugReportCommandHandler extends AbstractHandler
{	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		MessageConsole console = uiGetConsole(Messages.IDFToolsHandler_ToolsManagerConsole);
		MessageConsoleStream msgConsole = console.newMessageStream();
		msgConsole.println(Messages.ProductInformationHandler_ProductInformationLogJobName);
		
		BugReportGenerator generator = new BugReportGenerator();
		String report = generator.generateBugReport();
		
		msgConsole.println(String.format(Messages.BugReportHandler_CompletedBugReportMsg, report)); //$NON-NLS-1$
		
		msgConsole.println();
		msgConsole.println("Opening browser to create a new issue..."); //$NON-NLS-1$
		try
		{
			GithubIssueOpener.openNewIssue();
		}
		catch (Exception e)
		{
			Logger.log("Failed to open browser to create a new issue"); //$NON-NLS-1$
			Logger.log(e);
		}
		return null;
		
	}

	private MessageConsole uiGetConsole(String name)
	{
		final MessageConsole[] result = new MessageConsole[1];
		Display.getDefault().syncExec(() -> {
			result[0] = findConsole(name);
		});
		return result[0];
	}

	private MessageConsole findConsole(String name)
	{
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (IConsole ic : existing)
		{
			if (name.equals(ic.getName()))
			{
				return (MessageConsole) ic;
			}
		}
		// No console found, create a new one (no icon)
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
}
