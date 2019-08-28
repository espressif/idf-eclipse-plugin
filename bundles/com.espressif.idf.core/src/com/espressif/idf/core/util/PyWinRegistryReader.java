/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.espressif.idf.core.logging.Logger;

/**
 * Python Windows registry Wrapper
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class PyWinRegistryReader
{

	private static String PY_REG_PATH = "SOFTWARE\\Python\\PythonCore"; //$NON-NLS-1$
	private static String PY_INSTALL_PATH = "InstallPath"; //$NON-NLS-1$
	private static String PY_EXE_PATH = "ExecutablePath"; //$NON-NLS-1$

	protected List<Integer> getHKeyRootNodes()
	{
		List<Integer> rootNodes = new ArrayList<Integer>();
		rootNodes.add(WinRegistry.HKEY_CURRENT_USER);
		rootNodes.add(WinRegistry.HKEY_LOCAL_MACHINE);

		return rootNodes;
	}

	/**
	 * @return
	 */
	public Map<String, String> getPythonVersions()
	{
		Map<String, String> pythonRegistryMap = new HashMap<String, String>();

		List<Integer> hKeyRootNodes = getHKeyRootNodes();
		for (Integer rootNode : hKeyRootNodes)
		{
			List<String> py_version_list = null;
			try
			{
				py_version_list = WinRegistry.subKeysForPath(rootNode, PY_REG_PATH);
			}
			catch (Exception e)
			{
				Logger.log(e.getMessage());
			}
			if (py_version_list == null)
			{
				return pythonRegistryMap;
			}

			Logger.log(py_version_list.toString());
			for (String version : py_version_list)
			{
				String pyRegistryPath = PY_REG_PATH + "\\" + version + "\\" + PY_INSTALL_PATH;
				Logger.log(pyRegistryPath);
				List<String> py_exe_path_list = null;
				try
				{
					py_exe_path_list = WinRegistry.valuesForKeyPath(rootNode, pyRegistryPath, PY_EXE_PATH);
				}
				catch (Exception e)
				{
					Logger.log(e.getMessage());
				}
				if (py_exe_path_list == null)
				{
					return pythonRegistryMap;
				}

				Logger.log(py_exe_path_list.toString());
				if (!py_exe_path_list.isEmpty())
				{
					String pyExePath = py_exe_path_list.get(0);
					pythonRegistryMap.put(version, pyExePath);
				}
			}
		}

		return pythonRegistryMap;

	}

	// Test
	public static void main(String[] args)
	{
		try
		{
			Map<String, String> pyVersions = new PyWinRegistryReader().getPythonVersions();
			Set<String> versionNumbers = pyVersions.keySet();
			for (String version : versionNumbers)
			{
				System.out.println("Python " + version + " Path: " + pyVersions.get(version));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
