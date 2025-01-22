/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.vo.IdfInstalled;

/**
 * Utility class for Tools Management operations
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsUtility
{
	/**
	 * Gets the file checksum based on the provided message digest
	 * 
	 * @param digest
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getFileChecksum(MessageDigest digest, File file) throws IOException
	{
		FileInputStream fis = new FileInputStream(file);
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;
		while ((bytesCount = fis.read(byteArray)) != -1)
		{
			digest.update(byteArray, 0, bytesCount);
		}
		;
		fis.close();
		byte[] bytes = digest.digest();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
		{
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
	
	public static String getIdfVersion(IdfInstalled idfInstalled, String gitPath)
	{
		String activationScript = idfInstalled.getActivationScript();
		String espIdfVersion = null;

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
					if (line.startsWith("ESP_IDF_VERSION=")) //$NON-NLS-1$
					{
						espIdfVersion = line.split("=")[1]; //$NON-NLS-1$
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
		else 
		{
			command.add("sh"); //$NON-NLS-1$
			command.add(activationScriptPath);
			command.add("-e"); //$NON-NLS-1$
		}
		return command;
	}
	
	public static boolean isIdfInstalledActive(IdfInstalled idfInstalled)
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		String espIdfIdEim = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.ESP_IDF_EIM_ID);
		return idfInstalled.getId().equals(espIdfIdEim);
	}
}
