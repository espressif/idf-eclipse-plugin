/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.build.ESPToolChainManager;
import com.espressif.idf.core.build.ESPToolChainProvider;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * IDF Tools install command handler
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class InstallToolsHandler extends AbstractToolsHandler
{

	private IToolChainManager tcManager = CCorePlugin.getService(IToolChainManager.class);
	private ICMakeToolChainManager cmakeTcManager = CCorePlugin.getService(ICMakeToolChainManager.class);

	@Override
	protected void execute()
	{
		Job installToolsJob = new Job(Messages.InstallToolsHandler_InstallingToolsMsg)
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				monitor.beginTask(Messages.InstallToolsHandler_ItWilltakeTimeMsg, 5);
				monitor.worked(1);

				handleToolsInstall();
				monitor.worked(1);

				monitor.setTaskName(Messages.InstallToolsHandler_InstallingPythonMsg);
				handleToolsInstallPython();
				monitor.worked(1);

				monitor.setTaskName(Messages.InstallToolsHandler_ExportingPathsMsg);
				new ExportIDFTools().runToolsExport(pythonExecutablenPath, gitExecutablePath, console);
				monitor.worked(1);
				console.println(Messages.InstallToolsHandler_ConfiguredBuildEnvVarMsg);
				
				monitor.setTaskName(Messages.InstallToolsHandler_AutoConfigureToolchain);
				configureToolChain();
				monitor.worked(1);
				console.println(Messages.InstallToolsHandler_ConfiguredCMakeMsg);
				
				console.println(Messages.InstallToolsHandler_ToolsCompleted);
				return Status.OK_STATUS;
			}

		};
		installToolsJob.schedule();

	}

	/**
	 * Configure the toolchain and toolchain file in the preferences
	 */
	protected void configureToolChain()
	{
		ESPToolChainManager toolchainManager = new ESPToolChainManager();
		toolchainManager.initToolChain(tcManager, ESPToolChainProvider.ID);
		toolchainManager.initCMakeToolChain(tcManager, cmakeTcManager);
	}

	protected void handleToolsInstall()
	{
		// idf_tools.py install all
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_CMD);
		arguments.add(IDFConstants.TOOLS_INSTALL_ALL_CMD);

		console.println(Messages.InstallToolsHandler_InstallingToolsMsg);
		console.println(Messages.InstallToolsHandler_ItWilltakeTimeMsg);
		runCommand(arguments);

	}

	protected void handleToolsInstallPython()
	{
		List<String> arguments;
		// idf_tools.py install-python-env
		arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_PYTHON_CMD);
		runCommand(arguments);
	}

}
