/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.help;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.update.ListInstalledToolsHandler;
import com.espressif.idf.ui.update.Messages;

public class ProductInformationHandler extends ListInstalledToolsHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		Job job = new Job(Messages.ProductInformationHandler_ProductInformationLogJobName)
		{

			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					printingProductInformationLog(event);
				}
				catch (
						ExecutionException
						| InterruptedException e)
				{
					Logger.log(e);
					return new Status(Status.ERROR, "unknown", Status.ERROR, e.getMessage(), e); //$NON-NLS-1$
				}
				catch (OperationCanceledException e)
				{
					Logger.log(e);
					return Status.CANCEL_STATUS;
				}

				return Status.OK_STATUS;
			}

		};
		job.schedule();
		return null;
	}

	protected void printingProductInformationLog(ExecutionEvent event)
			throws ExecutionException, OperationCanceledException, InterruptedException
	{

		if (!StringUtil.isEmpty(getPythonExecutablePath()) && !StringUtil.isEmpty(IDFUtil.getIDFPath()))
		{
			super.execute(event);
			Job.getJobManager().join(Messages.ListInstalledToolsHandler_InstalledToolsListJobName, null);
		}
		else
		{
			activateIDFConsoleView();
		}

		showIDFEnvVars();
		console.println();
		console.println(Messages.OperatingSystemMsg + System.getProperty("os.name").toLowerCase()); //$NON-NLS-1$
		console.println(Messages.JavaRuntimeVersionMsg
				+ (Optional.ofNullable(System.getProperty("java.runtime.version")).orElse(Messages.NotFoundMsg))); //$NON-NLS-1$
		console.println(Messages.EclipseMsg + (Optional.ofNullable(Platform.getBundle("org.eclipse.platform")) //$NON-NLS-1$
				.map(o -> o.getVersion().toString()).orElse(Messages.NotFoundMsg))); // $NON-NLS-1$
		console.println(Messages.EclipseCDTMsg + (Optional.ofNullable(Platform.getBundle("org.eclipse.cdt")) //$NON-NLS-1$
				.map(o -> o.getVersion().toString()).orElse(Messages.NotFoundMsg))); // $NON-NLS-1$
		console.println(Messages.IdfEclipseMsg + (Optional.ofNullable(Platform.getBundle("com.espressif.idf.branding")) //$NON-NLS-1$
				.map(o -> o.getVersion().toString()).orElse(Messages.NotFoundMsg))); // $NON-NLS-1$
		showEspIdfVersion();
		console.println(Messages.PythonIdfEnvMsg + (Optional
				.ofNullable(getPythonExeVersion(IDFUtil.getIDFPythonEnvPath())).orElse(Messages.NotFoundMsg)));
	}

	private void showIDFEnvVars()
	{
		console.println(Messages.ProductInformationHandler_CDTBuildEnvVariables);
		Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();
		for (Entry<String, String> entry : envMap.entrySet())
		{
			String IDFEnvVarValue = entry.getValue();
			IDFEnvVarValue = IDFEnvVarValue.isEmpty() ? Messages.NotFoundMsg : IDFEnvVarValue;
			console.println(entry.getKey() + ": " + IDFEnvVarValue); //$NON-NLS-1$
		}
	}

	private void showEspIdfVersion()
	{
		if (IDFUtil.getIDFPath() != null && IDFUtil.getIDFPythonEnvPath() != null)
		{
			List<String> commands = new ArrayList<>();
			commands.add(IDFUtil.getIDFPythonEnvPath());
			commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
			commands.add("--version"); //$NON-NLS-1$
			Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();
			console.println(runCommand(commands, envMap));
		}
		else
		{
			console.println(Messages.MissingIdfPathMsg);
		}

	}

	private String getPythonExeVersion(String pythonExePath)
	{
		List<String> commands = new ArrayList<>();
		commands.add(pythonExePath);
		commands.add("--version"); //$NON-NLS-1$
		return pythonExePath != null ? runCommand(commands, System.getenv()) : null;
	}

	private String runCommand(List<String> arguments, Map<String, String> env)
	{
		String exportCmdOp = ""; //$NON-NLS-1$
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments, Path.ROOT, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$

			}

			// process export command output
			exportCmdOp = status.getMessage();
			Logger.log(exportCmdOp);
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return exportCmdOp;
	}

}