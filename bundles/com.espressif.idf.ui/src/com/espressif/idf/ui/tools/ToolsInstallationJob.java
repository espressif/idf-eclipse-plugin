package com.espressif.idf.ui.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.toolchain.ESPToolChainManager;
import com.espressif.idf.core.toolchain.ESPToolchain;
import com.espressif.idf.ui.update.ExportIDFTools;
import com.espressif.idf.ui.update.Messages;

public class ToolsInstallationJob extends ToolsJob
{
	public ToolsInstallationJob(String pythonExecutablePath, String gitExecutablePath, String idfPath, TableViewer tableViewer)
	{
		super(Messages.InstallToolsHandler_InstallingToolsMsg, pythonExecutablePath, gitExecutablePath, tableViewer);
		this.idfPath = idfPath;
		this.idfToolSet.setSystemGitExecutablePath(gitExecutablePath);
		this.idfToolSet.setSystemPythonExecutablePath(pythonExecutablePath);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		monitor.beginTask(Messages.InstallToolsHandler_ItWilltakeTimeMsg, 5);
		monitor.worked(1);

		IStatus status = handleToolsInstall();
		if (status.getSeverity() == IStatus.ERROR)
		{
			return status;
		}

		monitor.worked(1);
		monitor.setTaskName(Messages.InstallToolsHandler_InstallingPythonMsg);
		status = handleToolsInstallPython(console);
		if (status.getSeverity() == IStatus.ERROR)
		{
			return status;
		}

		monitor.worked(1);

		monitor.setTaskName(Messages.InstallToolsHandler_ExportingPathsMsg);
		ExportIDFTools exportIDFTools = new ExportIDFTools();
		status = exportIDFTools.getToolsExportOutputFromGivenIdfPath(pythonExecutablePath, gitExecutablePath, console,
				errorConsoleStream, idfPath);

		if (status.getSeverity() == IStatus.ERROR)
		{
			return status;
		}
		processExportCmdOutput(status.getMessage());

		monitor.worked(1);

		monitor.setTaskName(Messages.InstallToolsHandler_AutoConfigureToolchain);
		ESPToolChainManager espToolChainManager = new ESPToolChainManager();
		String pathToLookForToolChains = idfToolSet.getEnvVars().get(IDFEnvironmentVariables.PATH);
		String idfPath = idfToolSet.getEnvVars().get(IDFEnvironmentVariables.IDF_PATH);
		try
		{
			List<ESPToolchain> espToolChains = espToolChainManager
					.getStdToolChains(Arrays.asList(pathToLookForToolChains.split(File.pathSeparator)), idfPath);
			idfToolSet.setEspStdToolChains(espToolChains);
			List<ICMakeToolChainFile> cMakeToolChainFiles = espToolChainManager.getCmakeToolChains(idfPath);
			idfToolSet.setEspCmakeToolChainFiles(cMakeToolChainFiles);
		}
		catch (CoreException e)
		{
			Logger.log(e);
			logToConsole("Error Getting Toolchains", errorConsoleStream);
			return IDFCorePlugin.errorStatus("Error Getting Toolchains", null); //$NON-NLS-1$
		}

		monitor.setTaskName("Loading Available Targets in IDF");

		status = loadTargetsAvailableFromIdfInCurrentToolSet();
		if (status.getSeverity() == IStatus.ERROR)
		{
			return status;
		}

		idfToolSet.setLaunchTargets(extractTargets(status.getMessage()));

		monitor.worked(1);

		monitor.setTaskName(Messages.InstallToolsHandler_InstallingWebscoketMsg);
		handleWebSocketClientInstall();
		monitor.worked(1);

		console.println(Messages.InstallToolsHandler_ToolsCompleted);
		
		console.print("Writing to the configuration file");
		idfToolSet.setId(idfToolSet.hashCode());
		
		toolSetConfigurationManager.export(idfToolSet);
		
		console.println("You Need to set the tools active");
		refreshTable();
		return Status.OK_STATUS;
	}

	private void logToConsole(String message, MessageConsoleStream stream)
	{
		try
		{
			stream.write(message);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	protected IStatus handleToolsInstall()
	{
		// idf_tools.py install all
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_CMD);
		arguments.add(IDFConstants.TOOLS_INSTALL_ALL_CMD);

		console.println(Messages.InstallToolsHandler_InstallingToolsMsg);
		console.println(Messages.InstallToolsHandler_ItWilltakeTimeMsg);
		return runCommand(arguments, console);
	}

	protected IStatus handleToolsInstallPython(MessageConsoleStream console)
	{
		List<String> arguments;
		// idf_tools.py install-python-env
		arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_PYTHON_CMD);
		return runCommand(arguments, console);
	}

}
