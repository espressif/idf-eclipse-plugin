/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.build.gcc.core.GCCToolChain.GCCInfo;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESPToolChainManager
{
	/**
	 * Toolchain target property id.
	 */
	private static final String TOOLCHAIN_ATTR_ID = "ATTR_ID"; //$NON-NLS-1$

	/**
	 * @param manager
	 * @param toolchainId
	 */
	public void initToolChain(IToolChainManager manager, String toolchainId)
	{
		try
		{
			IToolChainProvider provider = manager.getProvider(toolchainId);
			initToolChain(manager, provider);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	/**
	 * @param manager
	 * @param toolchainProvider
	 */
	public void initToolChain(IToolChainManager manager, IToolChainProvider toolchainProvider)
	{
		Logger.log("Initializing toolchain..."); //$NON-NLS-1$
		List<String> paths = new ArrayList<String>();
		String idfToolsExportPath = getIdfToolsExportPath();
		if (idfToolsExportPath != null)
		{
			paths.add(idfToolsExportPath);
		}
		else
		{
			paths = getAllPaths();
		}
		Logger.log(paths.toString());

		// remove null paths if any
		paths.removeIf(Objects::isNull);

		for (String path : paths)
		{
			for (String dirStr : path.split(File.pathSeparator))
			{
				File dir = new File(dirStr);
				if (dir.isDirectory())
				{
					for (File file : dir.listFiles())
					{
						if (file.isDirectory())
						{
							continue;
						}

						Collection<Pattern> toolchainPatterns = ESPToolChainProvider.getToolchainPatterns();
						for (Pattern pattern : toolchainPatterns)
						{
							Matcher matcher = pattern.matcher(file.getName());
							if (matcher.matches())
							{
								addToolChain(manager, toolchainProvider, file, matcher);
							}
						}
					}
				}
			}
		}
	}

	private void addToolChain(IToolChainManager manager, IToolChainProvider toolchainProvider, File file,
			Matcher matcher)
	{
		try
		{
			GCCInfo info = new GCCInfo(file.toString());
			if (info.target != null && info.version != null)
			{
				GCCToolChain gcc = null;
				switch (info.target)
				{
				case ESP32ToolChain.ID:
					gcc = new ESP32ToolChain(toolchainProvider, file.toPath());
					break;
				case ESP32S2ToolChain.ID:
					gcc = new ESP32S2ToolChain(toolchainProvider, file.toPath());
					break;
				case ESP32S3ToolChain.ID:
					gcc = new ESP32S3ToolChain(toolchainProvider, file.toPath());
					break;
				case ESP32C3ToolChain.ID:
					gcc = new ESP32C3ToolChain(toolchainProvider, file.toPath());
					break;
				default:
					gcc = new ESP32ToolChain(toolchainProvider, file.toPath());
					break;
				}
				try
				{
					if (manager.getToolChain(gcc.getTypeId(), gcc.getId()) == null)
					{
						// Only add if another provider hasn't already added it
						if (matcher.matches())
						{
							manager.addToolChain(gcc);
						}
					}
				}
				catch (CoreException e)
				{
					CCorePlugin.log(e.getStatus());
				}
			}
		}
		catch (IOException e)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e);
		}
	}

	protected List<String> getAllPaths()
	{
		List<String> paths = new ArrayList<String>();

		String path = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.PATH);
		if (!StringUtil.isEmpty(path))
		{
			paths.add(path);
		}

		path = System.getenv(IDFEnvironmentVariables.PATH);
		if (!StringUtil.isEmpty(path))
		{
			paths.add(path);
		}

		return paths;
	}

	protected String getIdfToolsExportPath()
	{
		String idf_path = IDFUtil.getIDFPath();
		String tools_path = idf_path + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.IDF_TOOLS_SCRIPT;

		Logger.log("idf_tools.py path: " + tools_path); //$NON-NLS-1$
		if (!new File(tools_path).exists())
		{
			Logger.log("idf_tools.py path doesn't exist"); //$NON-NLS-1$
			return null;
		}

		String idfPythonEnvPath = IDFUtil.getIDFPythonEnvPath();

		try
		{
			List<String> commands = new ArrayList<String>();
			if (!StringUtil.isEmpty(idfPythonEnvPath))
			{
				commands.add(idfPythonEnvPath);
			}
			commands.add(tools_path);
			commands.add(IDFConstants.TOOLS_EXPORT_CMD);
			commands.add(IDFConstants.TOOLS_EXPORT_CMD_FORMAT_VAL);

			Logger.log(commands.toString());

			IStatus idf_tools_export_status = new ProcessBuilderFactory().runInBackground(commands,
					org.eclipse.core.runtime.Path.ROOT, System.getenv());
			if (idf_tools_export_status != null && idf_tools_export_status.isOK())
			{
				String message = idf_tools_export_status.getMessage();
				Logger.log("idf_tools.py export output: " + message); //$NON-NLS-1$
				if (message != null)
				{
					String[] exportEntries = message.split("\n"); //$NON-NLS-1$
					for (String entry : exportEntries)
					{
						String[] keyValue = entry.split("="); //$NON-NLS-1$
						if (keyValue.length == 2 && keyValue[0].equals(IDFEnvironmentVariables.PATH)) // 0 - key, 1 -
																										// value
						{
							Logger.log("PATH: " + keyValue[1]); //$NON-NLS-1$
							return keyValue[1];
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e);
		}
		return null;
	}

	public void initCMakeToolChain(IToolChainManager tcManager, ICMakeToolChainManager manager)
	{
		String idfPath = IDFUtil.getIDFPath();
		if (StringUtil.isEmpty(idfPath))
		{
			Logger.log("IDF_PATH is not found to auto-configure the toolchains.");
			return;
		}

		if (!new File(idfPath).exists())
		{
			String msg = MessageFormat.format(Messages.ESP32CMakeToolChainProvider_PathDoesnNotExist, idfPath);
			Logger.log(msg);
			return;
		}

		// add the newly found IDF_PATH to the eclipse environment variables if it's not there
		IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
		if (!new File(idfEnvMgr.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).exists())
		{
			idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfPath);
		}

		Collection<Map<String, String>> toolchains = getToolchainProperties();
		for (Map<String, String> properties : toolchains)
		{
			try
			{
				for (IToolChain tc : tcManager.getToolChainsMatching(properties))
				{

					Path toolChainFile = Paths.get(getIdfCMakePath(idfPath)).resolve(properties.get(TOOLCHAIN_ATTR_ID));
					if (Files.exists(toolChainFile))
					{
						ICMakeToolChainFile toolchainFile = manager.newToolChainFile(toolChainFile);
						toolchainFile.setProperty(IToolChain.ATTR_OS, properties.get(IToolChain.ATTR_OS));
						toolchainFile.setProperty(IToolChain.ATTR_ARCH, properties.get(IToolChain.ATTR_ARCH));

						toolchainFile.setProperty(ICBuildConfiguration.TOOLCHAIN_TYPE, tc.getTypeId());
						toolchainFile.setProperty(ICBuildConfiguration.TOOLCHAIN_ID, tc.getId());

						manager.addToolChainFile(toolchainFile);
					}
				}
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
		}
	}

	public Collection<Map<String, String>> getToolchainProperties()
	{
		Collection<Map<String, String>> propertiesList = new ArrayList<>();

		// esp32
		Map<String, String> esp32 = new HashMap<>();
		esp32.put(IToolChain.ATTR_OS, ESP32ToolChain.OS);
		esp32.put(IToolChain.ATTR_ARCH, ESP32ToolChain.ARCH);
		esp32.put(TOOLCHAIN_ATTR_ID, ESP32CMakeToolChainProvider.TOOLCHAIN_NAME);
		propertiesList.add(esp32);

		// esp32s2
		Map<String, String> esp32s2 = new HashMap<>();
		esp32s2.put(IToolChain.ATTR_OS, ESP32S2ToolChain.OS);
		esp32s2.put(IToolChain.ATTR_ARCH, ESP32S2ToolChain.ARCH);
		esp32s2.put(TOOLCHAIN_ATTR_ID, ESP32S2CMakeToolChainProvider.TOOLCHAIN_NAME);
		propertiesList.add(esp32s2);

		// esp32s3
		Map<String, String> esp32s3 = new HashMap<>();
		esp32s3.put(IToolChain.ATTR_OS, ESP32S3ToolChain.OS);
		esp32s3.put(IToolChain.ATTR_ARCH, ESP32S3ToolChain.ARCH);
		esp32s3.put(TOOLCHAIN_ATTR_ID, ESP32S3CMakeToolChainProvider.TOOLCHAIN_NAME);
		propertiesList.add(esp32s3);

		// esp32c3
		Map<String, String> esp32c3 = new HashMap<>();
		esp32c3.put(IToolChain.ATTR_OS, ESP32C3ToolChain.OS);
		esp32c3.put(IToolChain.ATTR_ARCH, ESP32C3ToolChain.ARCH);
		esp32c3.put(TOOLCHAIN_ATTR_ID, ESP32C3CMakeToolChainProvider.TOOLCHAIN_NAME);
		propertiesList.add(esp32c3);

		return propertiesList;
	}

	protected String getIdfCMakePath(String idfPath)
	{
		return idfPath + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR + IDFConstants.CMAKE_FOLDER;
	}
}
