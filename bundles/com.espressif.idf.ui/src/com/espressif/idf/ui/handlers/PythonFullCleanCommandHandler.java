/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.update.AbstractToolsHandler;

/**
 * idf.py fullclean
 * 
 * @author Kondal Kolipaka
 *
 */
public class PythonFullCleanCommandHandler extends AbstractToolsHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		activateIDFConsoleView();
		if (!NewProjectHandlerUtil.installToolsCheck())
		{
			return null;
		}
		IProject selectedProject = EclipseUtil.getSelectedProjectInExplorer();

		Path pathToProject = new Path(selectedProject.getLocation().toString());
		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("fullclean"); //$NON-NLS-1$
		Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
		console.println(commands.toString());
		console.print((runCommand(commands, pathToProject, envMap)));
		return event;
	}

	@Override
	protected void execute()
	{
	}

}
