/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.launch.serial.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.embedcdt.core.StringUtils;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFDynamicVariables;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.configparser.EspConfigParser;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;

public class ESPFlashUtil
{

	private static final int OPENOCD_JTAG_FLASH_SUPPORT_V = 20201125;
	public static final String VERSION_PATTERN = "(v.\\S+)"; //$NON-NLS-1$
	public static final String SERIAL_PORT = "${serial_port}"; //$NON-NLS-1$
	// prefix for backward compatibility with 2.9.1 where this prefix was not added in the argument in the UI
	private static final String DEFAULT_ARGUMENT_PREFIX = "${openocd_path}/${openocd_executable} "; //$NON-NLS-1$

	private ESPFlashUtil()
	{
	}

	/**
	 * @param launch
	 * @return command to flash the application
	 */
	public static String getEspFlashCommand(String serialPort)
	{

		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("-p"); //$NON-NLS-1$
		commands.add(serialPort);
		commands.add(IDFConstants.FLASH_CMD);

		return String.join(" ", commands); //$NON-NLS-1$
	}

	/**
	 * @param launch
	 * @return command to flash the application
	 */
	public static String getParseableEspFlashCommand(String serialPort)
	{

		List<String> commands = new ArrayList<>();
		commands.add(VariablesPlugin.getDefault().getStringVariableManager()
				.generateVariableExpression(IDFDynamicVariables.IDF_PY.name(), null));
		commands.add("-B"); //$NON-NLS-1$
		commands.add(VariablesPlugin.getDefault().getStringVariableManager()
				.generateVariableExpression(IDFDynamicVariables.BUILD_DIR.name(), null));
		commands.add("-p"); //$NON-NLS-1$
		commands.add(serialPort);
		commands.add(IDFConstants.FLASH_CMD);

		return String.join(" ", commands); //$NON-NLS-1$
	}

	public static boolean checkIfJtagIsAvailable()
	{
		EspConfigParser parser = new EspConfigParser();
		String openOCDPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS);
		if (openOCDPath.isEmpty() && !parser.hasBoardConfigJson())
		{
			return false;
		}

		String openOcdVersionOutput = IDFUtil.getOpenocdVersion();
		Pattern pattern = Pattern.compile(VERSION_PATTERN);
		Matcher matcher = pattern.matcher(openOcdVersionOutput);
		if (matcher.find() && Integer.parseInt(matcher.group(1).split("-")[2]) < OPENOCD_JTAG_FLASH_SUPPORT_V) //$NON-NLS-1$
		{
			return false;
		}

		return true;
	}

	public static String getEspJtagFlashCommand(ILaunchConfiguration configuration)
	{
		String espFlashCommand = "-c program_esp_bins <path-to-build-dir> flasher_args.json verify reset"; //$NON-NLS-1$
		try
		{

			String buildPath = configuration.getMappedResources()[0].getProject()
					.getPersistentProperty(new QualifiedName(IDFCorePlugin.PLUGIN_ID, IDFConstants.BUILD_DIR_PROPERTY));
			// converting to UNIX path so openocd could read it
			buildPath = new Path(buildPath).toString();

			buildPath = buildPath.isBlank() ? configuration.getMappedResources()[0].getProject()
					.getFolder(IDFConstants.BUILD_FOLDER).getLocationURI().getPath() : buildPath;

			char a = buildPath.charAt(2);
			if (a == ':')
			{
				buildPath = buildPath.substring(1);
			}
			espFlashCommand = espFlashCommand.replace("<path-to-build-dir>", buildPath.replace(" ", "\\ ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return espFlashCommand;
	}

	public static void flashOverJtag(ILaunchConfiguration configuration, ILaunch launch) throws CoreException
	{
		List<String> commands = new ArrayList<>();

		String arguments = configuration.getAttribute(IDFLaunchConstants.ATTR_JTAG_FLASH_ARGUMENTS, ""); //$NON-NLS-1$
		arguments = addPrefixIfNeeded(arguments);
		arguments = getVariablesValueFromExpression(arguments);
		commands.addAll(StringUtils.splitCommandLineOptions(arguments));

		String flashCommand = ESPFlashUtil.getEspJtagFlashCommand(configuration) + " exit"; //$NON-NLS-1$
		commands.add(flashCommand);

		try
		{
			Process p = Runtime.getRuntime().exec(commands.toArray(new String[0]));
			DebugPlugin.newProcess(launch, p, String.join(" ", commands)); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	/*
	 * checks if the active launch configuration has JTAG as a selected flash interface option
	 */
	public static boolean isJtag()
	{
		try
		{
			ILaunchConfiguration configuration = IDFCorePlugin.getService(ILaunchBarManager.class)
					.getActiveLaunchConfiguration();
			return configuration.getAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, false);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return false;
	}

	private static String getVariablesValueFromExpression(String expression) throws CoreException
	{
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		return manager.performStringSubstitution(expression);
	}

	/*
	 * Adding prefix if it's missing for backward. It should be missing if configuration was created in 2.9.1 version.
	 * old format: "-s ${openocd_path}/share/openocd/scripts -f board/esp32-wrover-kit-1.8v.cfg" new format:
	 * "${openocd_path}/${openocd_exe} -s ${OPENOCD_SCRIPTS} -f board/esp32s2-bridge.cfg
	 */
	private static String addPrefixIfNeeded(String arguments)
	{
		if (arguments.indexOf("-s") == 0) //$NON-NLS-1$
		{
			arguments = DEFAULT_ARGUMENT_PREFIX + arguments;
		}
		return arguments;
	}
}
