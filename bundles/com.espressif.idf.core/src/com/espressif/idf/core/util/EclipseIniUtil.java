/*******************************************************************************
 * Copyright 2021-2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;

import com.espressif.idf.core.logging.Logger;

/**
 * Utility class for managing and editing different parameter in eclipse.ini and config.ini file for eclipse
 * 
 * @author Ali Azam Rana
 *
 */
public class EclipseIniUtil
{
	private String ECLIPSE_INI_FILE;
	private static final String ECLIPSE_INI_VMARGS = "-vmargs"; //$NON-NLS-1$

	private List<String> eclipseIniFileContents;
	private List<String> eclipseIniArgs;
	private Map<String, String> eclipseIniSwitchMap;
	private URI eclipseIniUri;
	private Map<String, String> eclipseVmArgMap;

	public EclipseIniUtil() throws Exception
	{
		loadIniFilePath();
		Logger.log(Platform.getLocation().toOSString());
		eclipseIniUri = URIUtil.fromString(ECLIPSE_INI_FILE);
		loadEclipseIniFileContents();
		loadEclipseIniSwitchMap();
		loadEclipseVmArgMap();
	}

	private void loadIniFilePath() throws Exception
	{
		URL url = new URL(Platform.getInstallLocation().getURL()
				+ System.getProperty("eclipse.launcher.name", "eclipse").toLowerCase() + ".ini"); //$NON-NLS-1$ //$NON-NLS-2$
		ECLIPSE_INI_FILE = url.toString();
	}

	/**
	 * Sets the eclipse switch in eclipse.ini file for eclipse, these are not vmargs. Changes will not take effect until
	 * restart
	 * 
	 * @param eclipseSwitch The switch to set with the respective hyphen e.g: -nl or --launcher.defaultAction
	 * @param switchValue   The value of the switch to set
	 * @throws IOException
	 */
	public void setEclipseSwitchValue(String eclipseSwitch, String switchValue) throws IOException
	{
		eclipseIniSwitchMap.put(eclipseSwitch, switchValue);

		updateEclipseIniFile();
	}

	/**
	 * Gets the current value for a switch
	 * 
	 * @param eclipseSwitch The switch to get with the respective hyphen e.g: -nl or --launcher.defaultAction
	 * @return
	 */
	public String getEclipseIniSwitchValue(String eclipseSwitch)
	{
		return eclipseIniSwitchMap.get(eclipseSwitch);
	}

	/**
	 * Checks if the eclipse.ini contains the switch
	 * 
	 * @param eclipseSwitch The switch to look for with the respective hyphen e.g: -nl or --launcher.defaultAction
	 * @return True if present false if not
	 */
	public boolean containsEclipseSwitchInEclipseIni(String eclipseSwitch)
	{
		return eclipseIniSwitchMap.containsKey(eclipseSwitch);
	}

	private void loadEclipseIniFileContents() throws Exception
	{
		File file = new File(eclipseIniUri);
		eclipseIniFileContents = FileUtils.readLines(file, Charset.defaultCharset());
		int indexOfVmArgs = eclipseIniFileContents.indexOf(ECLIPSE_INI_VMARGS);
		if (indexOfVmArgs != -1)
		{
			eclipseIniArgs = new ArrayList<String>();
			for (int i = indexOfVmArgs + 1; i < eclipseIniFileContents.size(); i++)
			{
				eclipseIniArgs.add(eclipseIniFileContents.get(i));
			}
		}
	}

	private void updateEclipseIniFile() throws IOException
	{
		File file = new File(eclipseIniUri);
		List<String> contentsToWrite = new ArrayList<String>();
		for (Entry<String, String> entry : eclipseIniSwitchMap.entrySet())
		{
			contentsToWrite.add(entry.getKey());
			if (!StringUtil.isEmpty(entry.getValue()))
			{
				contentsToWrite.add(entry.getValue());
			}
		}

		if (eclipseIniFileContents.indexOf(ECLIPSE_INI_VMARGS) != -1)
		{
			contentsToWrite.add(ECLIPSE_INI_VMARGS);

			for (Entry<String, String> entry : eclipseVmArgMap.entrySet())
			{
				if (!StringUtil.isEmpty(entry.getValue()))
				{
					String contentToWrite = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
					contentsToWrite.add(contentToWrite);
				}
				else
				{
					contentsToWrite.add(entry.getKey());
				}
			}
		}

		FileUtils.writeLines(file, contentsToWrite);
	}

	private void loadEclipseIniSwitchMap()
	{
		eclipseIniSwitchMap = new HashMap<>();
		int indexOfVmArgs = eclipseIniFileContents.indexOf(ECLIPSE_INI_VMARGS);
		if (indexOfVmArgs == -1)
		{
			indexOfVmArgs = eclipseIniFileContents.size();
		}

		for (int i = 0; i < indexOfVmArgs; i++)
		{
			String key = eclipseIniFileContents.get(i);
			String value = eclipseIniFileContents.get(i + 1);
			if (value.charAt(0) == '-')
			{
				eclipseIniSwitchMap.put(key, "");
			}
			else
			{
				eclipseIniSwitchMap.put(key, value);
				i++;
			}
		}
	}

	private void loadEclipseVmArgMap()
	{
		eclipseVmArgMap = new HashMap<>();
		int indexOfVmArgs = eclipseIniFileContents.indexOf(ECLIPSE_INI_VMARGS);
		if (indexOfVmArgs == -1)
		{
			return;
		}

		for (int i = indexOfVmArgs + 1; i < eclipseIniFileContents.size(); i++)
		{
			String[] arg = eclipseIniFileContents.get(i).split("=");
			if (arg.length == 2)
			{
				String key = arg[0];
				String value = arg[1];
				eclipseVmArgMap.put(key, value);
			}
			else
			{
				eclipseVmArgMap.put(eclipseIniFileContents.get(i), "");
			}
		}
	}
}
