/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.CMakeToolChainEvent;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainListener;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.ICMakeToolChainProvider;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.aptana.core.ShellExecutable;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.util.StringUtil;

public class ESP32CMakeToolChainProvider implements ICMakeToolChainProvider, ICMakeToolChainListener
{

	private static final String TOOLCHAIN_ESP32_CMAKE = "toolchain-esp32.cmake"; //$NON-NLS-1$
	private IToolChainManager tcManager = CCorePlugin.getService(IToolChainManager.class);

	@Override
	public void init(ICMakeToolChainManager manager)
	{
		manager.addListener(this);

		Map<String, String> properties = new HashMap<>();
		properties.put(IToolChain.ATTR_OS, ESP32ToolChain.OS);
		properties.put(IToolChain.ATTR_ARCH, ESP32ToolChain.ARCH);
		try
		{
			for (IToolChain tc : tcManager.getToolChainsMatching(properties))
			{

				String idfPath = getIDFPath();
				if (!new File(idfPath).exists())
				{
					String errorMsg = MessageFormat.format(Messages.ESP32CMakeToolChainProvider_PathDoesnNotExist, idfPath);
					throw new CoreException(new Status(IStatus.ERROR, IDFCorePlugin.PLUGIN_ID, errorMsg));
				}
				
				String idfCMakeDir = idfPath + IPath.SEPARATOR + "tools" + IPath.SEPARATOR + "cmake"; //$NON-NLS-1$ //$NON-NLS-2$
				Path toolChainFile = Paths.get(idfCMakeDir).resolve(TOOLCHAIN_ESP32_CMAKE);
				if (Files.exists(toolChainFile))
				{
					ICMakeToolChainFile file = manager.newToolChainFile(toolChainFile);
					file.setProperty(IToolChain.ATTR_OS, ESP32ToolChain.OS);
					file.setProperty(IToolChain.ATTR_ARCH, ESP32ToolChain.ARCH);

					file.setProperty(ICBuildConfiguration.TOOLCHAIN_TYPE, tc.getTypeId());
					file.setProperty(ICBuildConfiguration.TOOLCHAIN_ID, tc.getId());

					manager.addToolChainFile(file);
				}
			}
		}
		catch (CoreException e)
		{
			IDFCorePlugin.getPlugin().getLog().log(e.getStatus());
		}
	}

	/**
	 * @return file path for IDF_PATH 
	 */
	protected String getIDFPath()
	{
		String idfPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PATH);
		if (StringUtil.isEmpty(idfPath))
		{

			// Try to get it from the system properties
			idfPath = System.getProperty(IDFEnvironmentVariables.IDF_PATH);
			if (StringUtil.isEmpty(idfPath))
			{
				Map<String, String> environment = ShellExecutable.getEnvironment();
				idfPath = environment.get(IDFEnvironmentVariables.IDF_PATH);
			}

			// Add this to C/C++ build environment variables - any issues?
			if (!StringUtil.isEmpty(idfPath))
			{
				new IDFEnvironmentVariables().addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfPath);
			}
		}

		return idfPath;
	}

	@Override
	public void handleCMakeToolChainEvent(CMakeToolChainEvent event)
	{
		switch (event.getType())
		{
		case CMakeToolChainEvent.ADDED:
			try
			{
				// This will load up the toolchain
				IToolChain toolChain = tcManager.getToolChain(ESP32ToolChainProvider.ID, ESP32ToolChain.ID);
				assert toolChain != null;
			}
			catch (CoreException e)
			{
				IDFCorePlugin.getPlugin().getLog().log(e.getStatus());
			}
			break;
		}
	}

}