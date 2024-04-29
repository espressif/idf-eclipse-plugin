/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.tools.ManageEspIdfVersionsHandler;

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
			showMessage(Messages.ListInstalledTools_MissingIdfPathMsg);
			throw new ExecutionException("Paths can't be empty. Please check IDF_PATH and Python"); //$NON-NLS-1$
		}

		activateIDFConsoleView();

		String listInstalledToolsJobName = Messages.ListInstalledToolsHandler_InstalledToolsListJobName;
		Job job = new Job(listInstalledToolsJobName)
		{

			protected IStatus run(IProgressMonitor monitor)
			{
				execute();
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family)
			{
				return listInstalledToolsJobName.equals(family);
			}

		};
		job.schedule();
		return null;
	}

	private void showMessage(final String message)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
						Messages.ListInstalledTools_MessageTitle, message);
				if (isYes)
				{
					ManageEspIdfVersionsHandler manageEspIdfVersionsHandler = new ManageEspIdfVersionsHandler();
					try
					{
						
						manageEspIdfVersionsHandler.execute(null);
					}
					catch (ExecutionException e)
					{
						Logger.log(e);
					}
				}
			}
		});
	}

	@Override
	protected void execute()
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_LIST_CMD);
		if (StringUtil.isEmpty(gitExecutablePath))
		{
			gitExecutablePath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.GIT_PATH);
		}

		runCommand(arguments, console);
	}

}
