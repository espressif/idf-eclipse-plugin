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

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.util.IDFUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESPCMakeToolChainProvider implements ICMakeToolChainProvider, ICMakeToolChainListener
{

	private static final String TOOLCHAIN_ESP32_CMAKE = "toolchain-esp32.cmake"; //$NON-NLS-1$
	private IToolChainManager tcManager = CCorePlugin.getService(IToolChainManager.class);

	@Override
	public void init(ICMakeToolChainManager manager)
	{
		manager.addListener(this);

		Map<String, String> properties = new HashMap<>();
		properties.put(IToolChain.ATTR_OS, ESPToolChain.OS);
		properties.put(IToolChain.ATTR_ARCH, ESPToolChain.ARCH);
		try
		{
			for (IToolChain tc : tcManager.getToolChainsMatching(properties))
			{

				String idfPath = IDFUtil.getIDFPath();
				if (!new File(idfPath).exists())
				{
					String errorMsg = MessageFormat.format(Messages.ESP32CMakeToolChainProvider_PathDoesnNotExist,
							idfPath);
					throw new CoreException(new Status(IStatus.ERROR, IDFCorePlugin.PLUGIN_ID, errorMsg));
				}

				// add the newly found IDF_PATH to the eclipse environment variables if it's not there
				IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
				if (!new File(idfEnvMgr.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).exists())
				{
					idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfPath);
				}

				Path toolChainFile = Paths.get(getIdfCMakePath(idfPath)).resolve(TOOLCHAIN_ESP32_CMAKE);
				if (Files.exists(toolChainFile))
				{
					ICMakeToolChainFile file = manager.newToolChainFile(toolChainFile);
					file.setProperty(IToolChain.ATTR_OS, ESPToolChain.OS);
					file.setProperty(IToolChain.ATTR_ARCH, ESPToolChain.ARCH);

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

	protected String getIdfCMakePath(String idfPath)
	{
		return idfPath + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR + IDFConstants.CMAKE_FOLDER;
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
				IToolChain toolChain = tcManager.getToolChain(ESPToolChainProvider.ID, ESPToolChain.ID);
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