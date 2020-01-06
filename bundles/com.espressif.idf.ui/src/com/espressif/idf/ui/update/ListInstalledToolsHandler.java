/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * List installed tools and versions command handler
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ListInstalledToolsHandler extends AbstractToolsHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		// Get IDF_PATH
		idfPath = IDFUtil.getIDFPath();
		Logger.log("IDF_PATH :" + idfPath); //$NON-NLS-1$

		pythonExecutablenPath = getPythonExecutablePath();
		Logger.log("Python Path :" + pythonExecutablenPath); //$NON-NLS-1$
		if (StringUtil.isEmpty(pythonExecutablenPath) || StringUtil.isEmpty(idfPath))
		{
			throw new ExecutionException("Paths can't be empty. Please check IDF_PATH and Python"); //$NON-NLS-1$
		}
		activateIDFConsoleView();
		execute();
		
		return null;
	}

	@Override
	protected void execute()
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_LIST_CMD);

		runCommand(arguments);
	}

}
