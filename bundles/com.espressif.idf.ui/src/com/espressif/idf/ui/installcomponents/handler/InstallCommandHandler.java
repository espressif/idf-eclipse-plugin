/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.handlers.NewProjectHandlerUtil;

/**
 * Install command for the components installation handler
 * 
 * @author Ali Azam Rana
 *
 */
public class InstallCommandHandler
{
	private static final String DOUBLE_QUOTES = "\""; //$NON-NLS-1$
	private static final String ADD_DEPENDENCY_COMMAND = "add-dependency"; //$NON-NLS-1$
	private static final String EQUALITY = "=="; //$NON-NLS-1$
	private static final String ASTERIK = "*"; //$NON-NLS-1$
	private static final String FORWARD_SLASH = "/"; //$NON-NLS-1$
	private String name;
	private String namespace;
	private String version;

	public InstallCommandHandler(String name, String namespace, String version)
	{
		this.name = name;
		this.namespace = namespace;
		this.version = version;
	}

	public void executeInstallCommand() throws Exception
	{
		if (!NewProjectHandlerUtil.installToolsCheck())
		{
			return;
		}
		Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
		IProject selectedProject = EclipseUtil.getSelectedProjectInExplorer();

		Path pathToProject = new Path(selectedProject.getLocation().toString());
		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add(ADD_DEPENDENCY_COMMAND);
		if (StringUtil.isEmpty(version))
		{
			commands.add(DOUBLE_QUOTES.concat(namespace.concat(FORWARD_SLASH).concat(name.concat(ASTERIK)))
					.concat(DOUBLE_QUOTES));
		}
		else
		{
			commands.add(
					DOUBLE_QUOTES.concat(namespace.concat(FORWARD_SLASH).concat(name.concat(EQUALITY).concat(version)))
							.concat(DOUBLE_QUOTES));
		}

		new IDFConsole().getConsoleStream().print((runCommand(commands, pathToProject, envMap)));

		selectedProject.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	private String runCommand(List<String> arguments, Path workDir, Map<String, String> env)
	{
		String exportCmdOp = ""; //$NON-NLS-1$
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments, workDir, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Status can't be null", null).toString(); //$NON-NLS-1$
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
