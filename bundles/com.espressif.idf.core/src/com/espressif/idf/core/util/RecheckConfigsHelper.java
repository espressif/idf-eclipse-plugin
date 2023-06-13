/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.util.NoSuchElementException;

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

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.toolchain.AbstractESPToolchain;

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

	private RecheckConfigsHelper()
	{
	}

	public static void revalidateToolchain(IProject project)
	{
		Preferences settings;
		try
		{
			settings = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node("config").node(project.getName()) //$NON-NLS-1$
					.node(project.getActiveBuildConfig().getName());
			IToolChainManager toolChainManager = CCorePlugin.getService(IToolChainManager.class);
			IToolChain anyEspToolChain = getESPToolChain(toolChainManager);
			String typeId = settings.get(ICBuildConfiguration.TOOLCHAIN_TYPE, anyEspToolChain.getTypeId());
			String toolchainId = settings.get(ICBuildConfiguration.TOOLCHAIN_ID, anyEspToolChain.getId());
			IToolChain currentToolChain = toolChainManager.getToolChain(typeId, toolchainId);
			if (!(currentToolChain instanceof AbstractESPToolchain))
			{
				currentToolChain = anyEspToolChain;
			}
			settings.put(ICBuildConfiguration.TOOLCHAIN_TYPE, currentToolChain.getTypeId());
			settings.put(ICBuildConfiguration.TOOLCHAIN_ID, currentToolChain.getId());
			recheckConfigs();
		}
		catch (
				CoreException
				| NoSuchElementException e)
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

		return toolChainManager.getAllToolChains().stream().filter(AbstractESPToolchain.class::isInstance).findFirst()
				.orElseThrow();

	}
}
