/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.util.Iterator;
import java.util.Optional;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager2;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.build.ESP32S2ToolChain;
import com.espressif.idf.core.build.ESP32ToolChain;
import com.espressif.idf.core.logging.Logger;

/**
 * A class for revalidating the build configuration and adding the correct toolchain to the settings. This helps to
 * resolve the "Build not configured correctly" issue and also resolves the build config creation issue due to the issue
 * of getting the toolchain.
 * 
 * @author Denys Almazov
 *
 */
public class RecheckConfigsHelper
{

	public static void revalidateToolchain(IProject project)
	{
		Preferences settings;
		try
		{
			settings = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node("config").node(project.getName())
					.node(project.getActiveBuildConfig().getName());
			IToolChainManager toolChainManager = CCorePlugin.getService(IToolChainManager.class);
			IToolChain toolChain = getESPToolChain(toolChainManager);
			settings.put(ICBuildConfiguration.TOOLCHAIN_TYPE,
					Optional.ofNullable(toolChain).map(IToolChain::getTypeId).orElse("")); //$NON-NLS-1$
			settings.put(ICBuildConfiguration.TOOLCHAIN_ID,
					Optional.ofNullable(toolChain).map(IToolChain::getId).orElse("")); //$NON-NLS-1$
			recheckConfigs();
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

	}

	private static void recheckConfigs()
	{
		ICBuildConfigurationManager mgr = CCorePlugin.getService(ICBuildConfigurationManager.class);
		ICBuildConfigurationManager2 manager = (ICBuildConfigurationManager2) mgr;
		manager.recheckConfigs();
	}

	private static IToolChain getESPToolChain(IToolChainManager toolChainManager) throws CoreException
	{
		Iterator<IToolChain> iter = toolChainManager.getAllToolChains().iterator();
		IToolChain toolChain = null;
		while (iter.hasNext())
		{
			toolChain = iter.next();
			if (toolChain instanceof ESP32ToolChain || toolChain instanceof ESP32S2ToolChain) // TODO: remove specific
																								// conditions
			{
				return toolChain;
			}
		}
		return toolChain;
	}
}
