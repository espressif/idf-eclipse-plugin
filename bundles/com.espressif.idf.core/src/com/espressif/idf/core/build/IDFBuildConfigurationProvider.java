/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.build;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.internal.CMakeBuildConfiguration;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.IDFCorePlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFBuildConfigurationProvider implements ICBuildConfigurationProvider
{

	public static final String ID = "com.espressif.idf.cmake.core.provider"; //$NON-NLS-1$

	private ICMakeToolChainManager manager = CCorePlugin.getService(ICMakeToolChainManager.class);
	private ICBuildConfigurationManager configManager = CCorePlugin.getService(ICBuildConfigurationManager.class);

	@Override
	public String getId()
	{
		return ID;
	}

	@SuppressWarnings("restriction")
	@Override
	public synchronized ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name)
			throws CoreException
	{
		if (config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME))
		{
			IToolChain toolChain = null;

			// try the toolchain for the local target
			Map<String, String> properties = new HashMap<>();
			properties.put(IToolChain.ATTR_OS, Platform.getOS());
			properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
			IToolChainManager toolChainManager = IDFCorePlugin.getService(IToolChainManager.class);
			for (IToolChain tc : toolChainManager.getToolChainsMatching(properties))
			{
				toolChain = tc;
				break;
			}

			// local didn't work, try and find one that does
			if (toolChain == null)
			{
				for (IToolChain tc : toolChainManager.getToolChainsMatching(new HashMap<>()))
				{
					toolChain = tc;
					break;
				}
			}

			if (toolChain != null)
			{
				return new IDFBuildConfiguration(config, name, toolChain);
			}
			else
			{
				// No valid combinations
				return null;
			}
		}
		IDFBuildConfiguration cmakeConfig = new IDFBuildConfiguration(config, name);
		ICMakeToolChainFile tcFile = cmakeConfig.getToolChainFile();
		IToolChain toolChain = cmakeConfig.getToolChain();
		if (toolChain == null) {
			// config not complete
			return null;
		}
		if (tcFile != null && !toolChain.equals(tcFile.getToolChain())) {
			// toolchain changed
			return new IDFBuildConfiguration(config, name, tcFile.getToolChain(), tcFile,
					cmakeConfig.getLaunchMode());
		} else {
			return cmakeConfig;
		}
	}

	@Override
	public ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException
	{
		// get matching toolchain file if any
		Map<String, String> properties = new HashMap<>();
		String os = toolChain.getProperty(IToolChain.ATTR_OS);
		if (os != null && !os.isEmpty())
		{
			properties.put(IToolChain.ATTR_OS, os);
		}
		String arch = toolChain.getProperty(IToolChain.ATTR_ARCH);
		if (arch != null && !arch.isEmpty())
		{
			properties.put(IToolChain.ATTR_ARCH, arch);
		}
		ICMakeToolChainFile file = manager.getToolChainFileFor(toolChain);
		if (file == null)
		{
			Collection<ICMakeToolChainFile> files = manager.getToolChainFilesMatching(properties);
			if (!files.isEmpty())
			{
				file = files.iterator().next();
			}
		}

		// Let's generate build artifacts directly under the build folder so that CLI and eclipse IDF will be in sync
		String name = ICBuildConfiguration.DEFAULT_NAME;

		IBuildConfiguration config = configManager.createBuildConfiguration(this, project, name, monitor);
		CBuildConfiguration cmakeConfig = new IDFBuildConfiguration(config, name, toolChain, file, launchMode);
		configManager.addBuildConfiguration(config, cmakeConfig);
		return cmakeConfig;
	}

}
