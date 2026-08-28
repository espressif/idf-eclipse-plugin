/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.eimjson.model.EimInstallationModel;
import com.espressif.idf.core.util.StringUtil;

/**
 * Utility class for Tools Management operations
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsUtility
{
	private static final Pattern IDF_VERSION_MAJOR_PATTERN = Pattern.compile("IDF_VERSION_MAJOR\\s+(\\d+)"); //$NON-NLS-1$
	private static final Pattern IDF_VERSION_MINOR_PATTERN = Pattern.compile("IDF_VERSION_MINOR\\s+(\\d+)"); //$NON-NLS-1$
	private static final Pattern IDF_VERSION_PATCH_PATTERN = Pattern.compile("IDF_VERSION_PATCH\\s+(\\d+)"); //$NON-NLS-1$

	/**
	 * Detects the ESP-IDF version for the given installation.
	 * <p>
	 * Prefers the full {@code MAJOR.MINOR.PATCH} read directly from {@code tools/cmake/version.cmake}
	 * under the installation's {@code IDF_PATH}. This is intentionally not derived from the
	 * {@code ESP_IDF_VERSION} environment variable: ESP-IDF and EIM define that variable as
	 * {@code MAJOR.MINOR} only (components consume it in Kconfig, e.g. {@code Kconfig.idf_v5.5.in}), so
	 * it never carries the patch component. When {@code version.cmake} cannot be read, falls back to the
	 * {@code ESP_IDF_VERSION} value printed by the activation script.
	 */
	public static String getIdfVersion(EimInstallationModel installation)
	{
		String versionFromCMake = readIdfVersionFromCMake(installation.getPath());
		if (!StringUtil.isEmpty(versionFromCMake))
		{
			return versionFromCMake;
		}

		return installation.getActivationScript().map(ToolsUtility::readIdfVersionFromScript)
				.orElse(StringUtil.EMPTY);
	}

	private static String readIdfVersionFromCMake(String idfPath)
	{
		if (StringUtil.isEmpty(idfPath))
		{
			return StringUtil.EMPTY;
		}

		try
		{
			Path versionCMakeFile = Path.of(idfPath, "tools", "cmake", "version.cmake"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return parseVersionCMake(versionCMakeFile);

		}
		catch (InvalidPathException e)
		{
			Logger.log(e);
			return StringUtil.EMPTY;
		}

	}

	/**
	 * Parses {@code version.cmake} and returns {@code MAJOR.MINOR.PATCH} (or {@code MAJOR.MINOR} when
	 * the patch entry is absent). Returns an empty string when the file is missing or the required
	 * major/minor entries cannot be found.
	 */
	public static String parseVersionCMake(Path versionCMakeFile)
	{
		if (versionCMakeFile == null || !Files.isRegularFile(versionCMakeFile))
		{
			return StringUtil.EMPTY;
		}

		try
		{
			String content = Files.readString(versionCMakeFile);
			String major = firstGroup(IDF_VERSION_MAJOR_PATTERN, content);
			String minor = firstGroup(IDF_VERSION_MINOR_PATTERN, content);
			if (major == null || minor == null)
			{
				return StringUtil.EMPTY;
			}

			String patch = firstGroup(IDF_VERSION_PATCH_PATTERN, content);
			if (patch == null)
			{
				return major + "." + minor; //$NON-NLS-1$
			}

			return major + "." + minor + "." + patch; //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (IOException e)
		{
			Logger.log(e);
			return StringUtil.EMPTY;
		}
	}

	private static String firstGroup(Pattern pattern, String content)
	{
		Matcher matcher = pattern.matcher(content);
		return matcher.find() ? matcher.group(1) : null;
	}

	private static String readIdfVersionFromScript(String activationScript)
	{
		String espIdfVersion = StringUtil.EMPTY;

		try
		{
			// Determine the command to execute based on the OS
			List<String> args = getExportScriptCommand(activationScript);
			String[] command = args.toArray(new String[args.size()]);
			// Execute the script
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.directory(new File(activationScript).getParentFile());
			processBuilder.redirectErrorStream(true);

			Process process = processBuilder.start();

			// Read the script output
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					if (line.startsWith("ESP_IDF_VERSION=") && line.split("=").length >= 2) //$NON-NLS-1$ //$NON-NLS-2$
					{
						espIdfVersion = line.split("=")[1].trim(); //$NON-NLS-1$
						break;
					}
				}
			}

			process.waitFor();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}

		return espIdfVersion;
	}
	
	public static List<String> getExportScriptCommand(String activationScriptPath)
	{
		List<String> command = new ArrayList<>();
		if (Platform.getOS().equals(Platform.OS_WIN32))
		{
			command.add("powershell.exe"); //$NON-NLS-1$
			command.add("-ExecutionPolicy"); //$NON-NLS-1$
			command.add("Bypass"); //$NON-NLS-1$
			command.add("-File"); //$NON-NLS-1$
			command.add(activationScriptPath);
			command.add("-e"); //$NON-NLS-1$
		}
		else if (Platform.getOS().equals(Platform.OS_LINUX))
		{
			command.add("/bin/bash"); //$NON-NLS-1$
			command.add(activationScriptPath);
			command.add("-e"); //$NON-NLS-1$
		}
		else 
		{
			command.add("/bin/zsh"); //$NON-NLS-1$
			command.add(activationScriptPath);
			command.add("-e"); //$NON-NLS-1$
		}
		return command;
	}
	
	public static boolean isIdfInstalledActive(EimInstallationModel installation)
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		String espIdfIdEim = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.ESP_IDF_EIM_ID);
		return installation.getId().equals(espIdfIdEim);
	}
}
