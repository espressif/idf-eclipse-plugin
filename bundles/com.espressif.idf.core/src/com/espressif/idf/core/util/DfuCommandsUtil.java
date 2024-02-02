/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFDynamicVariables;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;

public class DfuCommandsUtil
{

	public static final String DFU_COMMAND = "com.espressif.idf.ui.command.dfu"; //$NON-NLS-1$
	private static final String[] SUPPORTED_TARGETS = { "esp32s2", "esp32s3" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String DFU_FLASH_COMMAND = "dfu-flash"; //$NON-NLS-1$

	public static boolean isDfu()
	{
		try
		{
			ILaunchConfiguration configuration = IDFCorePlugin.getService(ILaunchBarManager.class)
					.getActiveLaunchConfiguration();
			return configuration.getAttribute(IDFLaunchConstants.DFU, false);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return false;
	}

	public static boolean isDfuSupported(ILaunchTarget launchTarget)
	{
		boolean isDfuSupported = isTargetSupportDfu(launchTarget);
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!isDfuSupported)
				{
					MessageDialog.openWarning(getShell(), Messages.DfuWarningDialog_Title,
							Messages.DfuWarningDialog_WrongTargterMsg);
				}
			}
		});
		return isDfuSupported;
	}

	public static boolean isTargetSupportDfu(ILaunchTarget launchTarget)
	{
		String targetName = launchTarget.getAttribute("com.espressif.idf.launch.serial.core.idfTarget", //$NON-NLS-1$
				StringUtil.EMPTY);
		boolean isDfuSupported = Arrays.stream(SUPPORTED_TARGETS).anyMatch(target -> target.contentEquals(targetName));
		return isDfuSupported;
	}

	public static String getDfuFlashCommand()
	{
		List<String> commands = new ArrayList<>();
		commands.add(generateVariableExpression(IDFDynamicVariables.IDF_PYTHON_ENV_PATH.name()));
		commands.add(generateVariableExpression(IDFDynamicVariables.IDF_PY.name()));
		commands.add(DFU_FLASH_COMMAND);
		return String.join(" ", commands); //$NON-NLS-1$
	}

	public static List<String> getDfuBuildCommand()
	{
		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("dfu"); //$NON-NLS-1$
		return commands;
	}

	private static String generateVariableExpression(String variableName)
	{
		return VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(variableName, null);
	}

	private static String resolveExpressionFromVariableManager(String expression) throws CoreException
	{
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
	}

	public static void flashDfuBins(ILaunchConfiguration configuration, IProject project, ILaunch launch,
			IProgressMonitor monitor)
	{
		List<String> flashCommandList = new ArrayList<>();
		try
		{
			String flashCommand = configuration.getAttribute(IDFLaunchConstants.ATTR_DFU_FLASH_ARGUMENTS,
					getDfuFlashCommand());
			flashCommand = resolveExpressionFromVariableManager(flashCommand);
			flashCommandList = Arrays.asList(flashCommand.split(" ")); //$NON-NLS-1$
		}
		catch (CoreException e1)
		{
			Logger.log(e1);
		}
		List<String> commands = new ArrayList<>();
		commands.addAll(flashCommandList);
		File workingDir = null;
		workingDir = new File(project.getLocationURI());
		Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();
		List<String> strings = new ArrayList<>(envMap.size());
		for (Entry<String, String> entry : envMap.entrySet())
		{
			StringBuilder buffer = new StringBuilder(entry.getKey());
			buffer.append('=').append(entry.getValue()); // $NON-NLS-1$
			strings.add(buffer.toString());
		}

		String[] envArray = strings.toArray(new String[strings.size()]);
		Process p = null;
		try
		{
			p = DebugPlugin.exec(commands.toArray(new String[0]), workingDir, envArray);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		DebugPlugin.newProcess(launch, p, String.join(" ", commands)); //$NON-NLS-1$
	}

	private static Shell getShell()
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
		{
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 0)
			{
				return windows[0].getShell();
			}
		}
		else
		{
			return window.getShell();
		}
		return null;
	}
}
