/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IPageLayout;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeArchivesMemoryHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		// get the selected project
		IResource project = EclipseHandler.getSelectedProject(IPageLayout.ID_PROJECT_EXPLORER);
		if (project == null)
		{
			project = EclipseHandler.getSelectedResource((IEvaluationContext) event.getApplicationContext());
		}

		if (project == null)
		{
			return project;
		}

		// get the active server instance for the project
		if (project instanceof IProject)
		{
			try
			{
				IStatus status = runIdfSizeArchivesCommand((IProject) project);
				Logger.log(status.getMessage());

				if (status.isOK())
				{
					String message = status.getMessage();
					if (!StringUtil.isEmpty(message))
					{
						IDFConsole idfConsole = new IDFConsole();
						idfConsole.getConsoleStream().print(message);
					}
				}
			}
			catch (Exception e)
			{
				Logger.log(e);
			}
		}

		return null;
	}

	protected IStatus runIdfSizeArchivesCommand(IProject project) throws Exception
	{
		// Check IDF_PYTHON_ENV_PATH
		String pythonExecutablenPath = IDFUtil.getIDFPythonEnvPath();
		if (StringUtil.isEmpty(pythonExecutablenPath))
		{
			throw new Exception("IDF_PYTHON_ENV_PATH path is not found in the Eclispe CDT build environment variables");
		}

		// Check /project/build/projectname.map
		IPath mapPath = project.getLocation().append("build").append(project.getName() + ".map");
		if (!mapPath.toFile().exists())
		{
			String msg = NLS.bind("Couldn't find {0} map file in the project", mapPath.toOSString());
			throw new Exception(msg);
		}

		List<String> arguments = new ArrayList<String>();
		arguments.add(pythonExecutablenPath);
		arguments.add(IDFUtil.getIDFSizeScriptFile().getAbsolutePath());
		arguments.add(mapPath.toOSString());
		arguments.add("--archives");
		arguments.add("--json");

		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			return processRunner.runInBackground(arguments, Path.ROOT, System.getenv());
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return null;
	}

}
