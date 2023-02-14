/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.build.ESPToolChainManager;
import com.espressif.idf.core.build.ESPToolChainProvider;

/**
 * The util class to configure the toolchains after tools installation
 * 
 * @author Denys Almazov
 *
 */
public class ToolChainUtil
{

	private ToolChainUtil()
	{
	}

	/**
	 * Configure the file in the preferences and initialize the launch bar with available targets
	 */
	public static void configureToolChain()
	{
		IToolChainManager tcManager = CCorePlugin.getService(IToolChainManager.class);
		ICMakeToolChainManager cmakeTcManager = CCorePlugin.getService(ICMakeToolChainManager.class);

		ESPToolChainManager toolchainManager = new ESPToolChainManager();
		toolchainManager.initToolChain(tcManager, ESPToolChainProvider.ID);
		toolchainManager.initCMakeToolChain(tcManager, cmakeTcManager);
		toolchainManager.addToolchainBasedTargets(IDFCorePlugin.getService(ILaunchTargetManager.class));
	}
}
