/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import org.eclipse.cdt.utils.WindowsRegistry;

/**
 * Gets the install path for git in windows via registry
 * 
 * @author Ali Azam Rana
 *
 */
public class GitWinRegistryReader
{
	private static String GIT_REG_PATH = "SOFTWARE\\GitForWindows"; //$NON-NLS-1$
	private static String GIT_INSTALL_PATH = "InstallPath"; //$NON-NLS-1$
	private static String GIT_EXE_PATH = "ExecutablePath"; //$NON-NLS-1$

	/**
	 * @return
	 */
	public String getGitInstallPath()
	{
		WindowsRegistry registry = WindowsRegistry.getRegistry();

		// HKEY_CURRENT_USER
		String gitPath = registry.getCurrentUserValue(GIT_REG_PATH, GIT_INSTALL_PATH);

		if (!StringUtil.isEmpty(gitPath))
		{
			return gitPath;
		}

		// HKEY_LOCAL_MACHINE
		gitPath = registry.getLocalMachineValue(GIT_REG_PATH, GIT_INSTALL_PATH);
		return gitPath;
	}
}
