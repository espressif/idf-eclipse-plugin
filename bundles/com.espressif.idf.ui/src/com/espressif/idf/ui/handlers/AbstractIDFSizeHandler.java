/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.UIPlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public abstract class AbstractIDFSizeHandler extends AbstractHandler
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

		if (project instanceof IProject)
		{
			try
			{
				IStatus status = runIdfSizeCommand((IProject) project);
				String message = status.getMessage();
				Logger.log(message);
				if (status.isOK() && !StringUtil.isEmpty(message))
				{
					IDFConsole idfConsole = new IDFConsole();
					idfConsole.getConsoleStream().print(message);
				}
			}
			catch (Exception e)
			{
				Logger.log(e);
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", e.getMessage()); //$NON-NLS-1$
			}
		}

		return null;
	}

	protected IStatus runIdfSizeCommand(IProject project) throws Exception
	{
		// Check IDF_PYTHON_ENV_PATH
		String pythonExecutablenPath = IDFUtil.getIDFPythonEnvPath();
		if (StringUtil.isEmpty(pythonExecutablenPath))
		{
			throw new Exception("IDF_PYTHON_ENV_PATH path is not found in the Eclispe CDT build environment variables"); //$NON-NLS-1$
		}

		// Check /project/build/projectname.map
		IPath mapPath = getMapFilePath(project);
		if (!mapPath.toFile().exists())
		{
			String msg = NLS.bind("Couldn't find {0} map file in the project", mapPath.toOSString()); //$NON-NLS-1$
			throw new Exception(msg);
		}

		List<String> arguments = getCommandArgs(pythonExecutablenPath, mapPath);

		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			return processRunner.runInBackground(arguments, Path.ROOT, System.getenv());
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
			return new Status(Status.ERROR, UIPlugin.PLUGIN_ID, e1.getMessage());
		}
	}

	protected IPath getMapFilePath(IProject project)
	{
		IPath mapPath = project.getLocation().append("build").append(project.getName() + ".map"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!mapPath.toFile().exists())
		{
			File buildDir = project.getLocation().append("build").toFile(); //$NON-NLS-1$
			if (buildDir.exists())
			{
				// search for .map filea
				File[] fileList = buildDir.listFiles();
				for (File file : fileList)
				{
					if (file.getName().endsWith(".map")) // $NON-NLS-N$
					{
						return new Path(file.getAbsolutePath());
					}
				}
			}
		}
		return mapPath;
	}

	protected abstract List<String> getCommandArgs(String pythonExecutablenPath, IPath mapPath);

}
