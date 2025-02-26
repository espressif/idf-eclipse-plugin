
/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.espressif.idf.core.util.IDFUtil;

/**
 * @author Kondal Kolipaka
 * 
 *         Any preference constants which are intended to be used across IDE
 *
 */
public class IDFCorePreferenceConstants
{
	// CMake CCache preferences
	public static final String CMAKE_CCACHE_STATUS = "cmakeCCacheStatus"; //$NON-NLS-1$
	public static final String AUTOMATE_BUILD_HINTS_STATUS = "automateHintsStatus"; //$NON-NLS-1$
	public static final String HIDE_ERRORS_IDF_COMPONENTS = "hideErrorsOnIdfDerivedFiles"; //$NON-NLS-1$
	public static final String AUTOMATE_CLANGD_FORMAT_FILE = "automateClangFormatFileCreation"; //$NON-NLS-1$
	public static final boolean AUTOMATE_CLANGD_FORMAT_FILE_DEFAULT = true;
	public static final boolean CMAKE_CCACHE_DEFAULT_STATUS = true;
	public static final boolean AUTOMATE_BUILD_HINTS_DEFAULT_STATUS = true;
	public static final boolean HIDE_ERRORS_IDF_COMPONENTS_DEFAULT_STATUS = true;
	public static final String IDF_GITHUB_ASSETS = "IDF_GITHUB_ASSETS"; //$NON-NLS-1$
	public static final String IDF_GITHUB_ASSETS_DEFAULT_GLOBAL = "dl.espressif.com/github_assets"; //$NON-NLS-1$
	public static final String IDF_GITHUB_ASSETS_DEFAULT_CHINA = "dl.espressif.cn/github_assets"; //$NON-NLS-1$
	public static final String PIP_EXTRA_INDEX_URL = "PIP_EXTRA_INDEX_URL"; //$NON-NLS-1$
	public static final String PIP_EXTRA_INDEX_URL_DEFAULT_GLOBAL = "https://dl.espressif.com/pypi"; //$NON-NLS-1$
	public static final String PIP_EXTRA_INDEX_URL_DEFAULT_CHINA = "https://dl.espressif.cn/pypi"; //$NON-NLS-1$
	public static final String IDF_TOOLS_PATH = "IDF_TOOLS_PATH"; //$NON-NLS-1$
	public static final String IDF_TOOLS_PATH_DEFAULT = Platform.getOS().equals(Platform.OS_WIN32)
			? IDFUtil.resolveEnvVariable("%USERPROFILE%\\.espressif") //$NON-NLS-1$
			: IDFUtil.resolveEnvVariable("$HOME/.espressif"); //$NON-NLS-1$

	/**
	 * Returns the node in the preference in the given context.
	 *
	 * @param key     The preference key.
	 * @param project The current context or {@code null} if no context is available and the workspace setting should be
	 *                taken. Note that passing {@code null} should be avoided.
	 * @return Returns the node matching the given context.
	 */
	public static IEclipsePreferences getPreferenceNode(String key, IProject project)
	{
		IEclipsePreferences node = null;
		if (project != null)
		{
			node = new ProjectScope(project).getNode(IDFCorePlugin.PLUGIN_ID);
			if (node.get(key, null) != null)
			{
				return node;
			}
		}
		node = InstanceScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID);
		if (node.get(key, null) != null)
		{
			return node;
		}

		node = ConfigurationScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID);
		if (node.get(key, null) != null)
		{
			return node;
		}

		return DefaultScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID);
	}
}
