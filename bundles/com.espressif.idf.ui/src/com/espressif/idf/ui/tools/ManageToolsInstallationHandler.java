/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.tools.shell.ManageToolsInstallationShell;

/**
 * Tools Installation Handler for the menu command
 * 
 * @author Ali Azam Rana
 *
 */
public class ManageToolsInstallationHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		ToolsJsonParser toolsJsonParser = new ToolsJsonParser();
		try
		{
			toolsJsonParser.loadJson();
			ManageToolsInstallationShell manageToolsInstallationShell = new ManageToolsInstallationShell(
					toolsJsonParser.getToolsList());
			manageToolsInstallationShell.openShell();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}
}
