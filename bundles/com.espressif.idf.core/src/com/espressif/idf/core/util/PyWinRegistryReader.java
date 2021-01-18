/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.utils.WindowsRegistry;

import com.espressif.idf.core.logging.Logger;

/**
 * Python Windows registry Wrapper.
 * 
 * <br>
 * Search for Python versions and identify the install location in the windows registry on the following locations: <br>
 * 
 * HKEY_CURRENT_USER/SOFTWARE/Python/PythonCore>/<version>/InstallPath/ExecutablePath <br>
 * HKEY_LOCAL_MACHINE/SOFTWARE/Python/PythonCore>/<version>/InstallPath/ExecutablePath <br>
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class PyWinRegistryReader
{

	private static String PY_REG_PATH = "SOFTWARE\\Python\\PythonCore"; //$NON-NLS-1$
	private static String PY_INSTALL_PATH = "InstallPath"; //$NON-NLS-1$
	private static String PY_EXE_PATH = "ExecutablePath"; //$NON-NLS-1$

	/**
	 * @return
	 */
	public Map<String, String> getPythonVersions()
	{
		Map<String, String> pythonRegistryMap = new HashMap<String, String>();

		WindowsRegistry registry = WindowsRegistry.getRegistry();
		List<String> py_version_list = new ArrayList<String>();
		String version;

		// HKEY_CURRENT_USER
		for (int i = 0; (version = registry.getCurrentUserKeyName(PY_REG_PATH, i)) != null; i++)
		{
			py_version_list.add(version);
			String compKey = PY_REG_PATH + '\\' + version + '\\' + PY_INSTALL_PATH; // $NON-NLS-1$ 
			Logger.log(compKey);

			String installLocation = registry.getCurrentUserValue(compKey, PY_EXE_PATH);
			Logger.log(installLocation);
			if (installLocation != null)
			{
				pythonRegistryMap.put(version, installLocation);
			}

		}
		// HKEY_LOCAL_MACHINE
		for (int i = 0; (version = registry.getLocalMachineKeyName(PY_REG_PATH, i)) != null; i++)
		{
			py_version_list.add(version);
			String compKey = PY_REG_PATH + '\\' + version + '\\' + PY_INSTALL_PATH; // $NON-NLS-1$ 
			Logger.log(compKey);

			String installLocation = registry.getLocalMachineValue(compKey, PY_EXE_PATH);
			Logger.log(installLocation);
			if (installLocation != null)
			{
				pythonRegistryMap.put(version, installLocation);
			}

		}

		return pythonRegistryMap;

	}

}
