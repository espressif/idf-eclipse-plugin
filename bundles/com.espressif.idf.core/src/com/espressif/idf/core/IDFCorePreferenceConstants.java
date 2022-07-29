
/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

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
	public static final boolean CMAKE_CCACHE_DEFAULT_STATUS = true;

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
