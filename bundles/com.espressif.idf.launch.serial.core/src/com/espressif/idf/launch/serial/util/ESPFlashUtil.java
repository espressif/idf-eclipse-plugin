/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.launch.serial.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFDynamicVariables;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.EspConfigParser;
import com.espressif.idf.core.util.IDFUtil;

public class ESPFlashUtil {

	private static final int OPENOCD_JTAG_FLASH_SUPPORT_V = 20201125;
	public static final String VERSION_PATTERN = "(v.\\S+)"; //$NON-NLS-1$
	public static final String SERIAL_PORT = "${serial_port}"; //$NON-NLS-1$

	private ESPFlashUtil() {
	}

	/**
	 * @param launch
	 * @return command to flash the application
	 */
	public static String getEspFlashCommand(String serialPort) {

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
	public static String getParseableEspFlashCommand(String serialPort) {

		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getParseableVarValue(IDFDynamicVariables.IDF_PY));
		commands.add("-p"); //$NON-NLS-1$
		commands.add(serialPort);
		commands.add(IDFConstants.FLASH_CMD);

		return String.join(" ", commands); //$NON-NLS-1$
	}

	public static boolean checkIfJtagIsAvailable() {
		EspConfigParser parser = new EspConfigParser();
		String openOCDPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS);
		if (openOCDPath.isEmpty() && !parser.hasBoardConfigJson()) {
			return false;
		}

		String openOcdVersionOutput = IDFUtil.getOpenocdVersion();
		Pattern pattern = Pattern.compile(VERSION_PATTERN);
		Matcher matcher = pattern.matcher(openOcdVersionOutput);
		if (matcher.find() && Integer.parseInt(matcher.group(1).split("-")[2]) < OPENOCD_JTAG_FLASH_SUPPORT_V) { //$NON-NLS-1$
			return false;
		}

		return true;
	}

	public static String getEspJtagFlashCommand(ILaunchConfiguration configuration) {
		String espFlashCommand = "-c program_esp_bins <path-to-build-dir> flasher_args.json verify reset"; //$NON-NLS-1$
		try {

			String buildPath = configuration.getMappedResources()[0].getProject()
					.getPersistentProperty(new QualifiedName(IDFCorePlugin.PLUGIN_ID, IDFConstants.BUILD_DIR_PROPERTY));
			// converting to UNIX path so openocd could read it
			buildPath = new Path(buildPath).toString();

			buildPath = buildPath.isBlank() ? configuration.getMappedResources()[0].getProject()
					.getFolder(IDFConstants.BUILD_FOLDER).getLocationURI().getPath() : buildPath;

			char a = buildPath.charAt(2);
			if (a == ':') {
				buildPath = buildPath.substring(1);
			}
			espFlashCommand = espFlashCommand.replace("<path-to-build-dir>", buildPath); //$NON-NLS-1$
		} catch (CoreException e) {
			Logger.log(e);
		}
		return espFlashCommand;
	}
}
