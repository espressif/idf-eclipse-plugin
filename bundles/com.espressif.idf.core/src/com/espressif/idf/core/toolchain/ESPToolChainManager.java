/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.build.Messages;
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
	private String envValue;
	private static Map<String, ESPToolChainElement> toolchainElements = new HashMap<>();

	public ESPToolChainManager()
	{
		readESPToolchainRegistry();
	}

	private static Map<String, ESPToolChainElement> readESPToolchainRegistry()
	{
		if (toolchainElements.isEmpty()) // load only once from the extension point
		{
			IConfigurationElement[] configElements = Platform.getExtensionRegistry()
					.getConfigurationElementsFor("com.espressif.idf.core.toolchain"); //$NON-NLS-1$
			for (IConfigurationElement iConfigurationElement : configElements)
			{
				String name = iConfigurationElement.getAttribute("name"); //$NON-NLS-1$
				String id = iConfigurationElement.getAttribute("id"); //$NON-NLS-1$
				String arch = iConfigurationElement.getAttribute("arch"); //$NON-NLS-1$
				String fileName = iConfigurationElement.getAttribute("fileName"); //$NON-NLS-1$
				String compilerPattern = iConfigurationElement.getAttribute("compilerPattern"); //$NON-NLS-1$
				String debuggerPatten = iConfigurationElement.getAttribute("debuggerPattern"); //$NON-NLS-1$

				String uniqueToolChainId = name.concat("/").concat(arch).concat("/").concat(fileName); //$NON-NLS-1$ //$NON-NLS-2$

				toolchainElements.put(uniqueToolChainId,
						new ESPToolChainElement(name, id, arch, fileName, compilerPattern, debuggerPatten));

			}
		}
		return toolchainElements;
	}

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
		List<String> paths = new ArrayList<>();
		String idfToolsExportPath = StringUtil.isEmpty(envValue) ? getIdfToolsExportPath() : envValue;
		if (idfToolsExportPath != null)
		{
			paths.add(idfToolsExportPath);
		}
		else
		{
			paths = getAllPaths();
		}
		Logger.log(paths.toString());

		String idfPath = IDFUtil.getIDFPath();
		for (ESPToolChainElement toolChainElement : toolchainElements.values())
		{
			File toolchainCompilerFile = findToolChain(paths, toolChainElement.compilerPattern);
			Path toolChainCmakeFile = Paths.get(getIdfCMakePath(idfPath)).resolve(toolChainElement.fileName);
			if (toolchainCompilerFile != null && Files.exists(toolChainCmakeFile))
			{
				addToolChain(manager, toolchainProvider, toolchainCompilerFile, toolChainElement);
			}
		}
	}

	public File findDebugger(String target)
	{
		return toolchainElements
		.values()
		.stream()
		.filter(espToolChainElement -> espToolChainElement.name.equals(target))
		.map(espToolChainElement -> findToolChain(getAllPaths(), espToolChainElement.debuggerPattern))
		.findFirst()
		.orElse(null);
	}

	public File findToolChain(List<String> paths, String filePattern)
	{
		for (String path : paths)
		{
			Path[] directories = getDirectories(path);
			for (Path dir : directories)
			{
				File file = findMatchingFile(dir, filePattern);
				if (file != null)
				{
					return file;
				}
			}
		}
		return null;
	}

	private Path[] getDirectories(String path)
	{
		return Arrays.stream(path.split(File.pathSeparator))
				.map(String::trim)
				.map(Paths::get)
				.toArray(Path[]::new);
	}

	private File findMatchingFile(Path dir, String filePattern)
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir))
		{
			Pattern pattern = Pattern.compile(filePattern);
			for (Path file : stream)
			{
				if (Files.isRegularFile(file))
				{
					Matcher matcher = pattern.matcher(file.getFileName().toString());
					if (matcher.matches())
					{
						return file.toFile();
					}
				}
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		return null;
	}


	public void removePrevInstalledToolchains(IToolChainManager manager)
	{
		try
		{
			Collection<IToolChain> toolchains = manager.getAllToolChains();
			ArrayList<IToolChain> tcList = new ArrayList<>(toolchains);
			tcList.forEach(manager::removeToolChain);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	private void addToolChain(IToolChainManager manager, IToolChainProvider toolchainProvider, File compilerFile,
			ESPToolChainElement toolChainElement)
	{
		try
		{
			if (!isToolChainExist(manager, toolChainElement))
			{
				manager.addToolChain(new ESPToolchain(toolchainProvider, compilerFile.toPath(), toolChainElement));
			}
		}
		catch (CoreException e)
		{
			CCorePlugin.log(e.getStatus());
		}
	}

	private boolean isToolChainExist(IToolChainManager manager, ESPToolChainElement toolChainElement)
			throws CoreException
	{
		Map<String, String> props = new HashMap<>();
		props.put(IToolChain.ATTR_OS, toolChainElement.name);
		props.put(IToolChain.ATTR_ARCH, toolChainElement.arch);
		props.put(TOOLCHAIN_ATTR_ID, toolChainElement.fileName);

		return !manager.getToolChainsMatching(props).isEmpty();
	}

	protected List<String> getAllPaths()
	{
		List<String> paths = new ArrayList<>();

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

		// remove null paths if any
		paths.removeIf(Objects::isNull);

		return paths;
	}

	protected String getIdfToolsExportPath()
	{
		String idfPath = IDFUtil.getIDFPath();
		String toolsPath = idfPath + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.IDF_TOOLS_SCRIPT;

		Logger.log("idf_tools.py path: " + toolsPath); //$NON-NLS-1$
		if (!new File(toolsPath).exists())
		{
			Logger.log("idf_tools.py path doesn't exist"); //$NON-NLS-1$
			return null;
		}

		String idfPythonEnvPath = IDFUtil.getIDFPythonEnvPath();

		try
		{
			List<String> commands = new ArrayList<>();
			if (!StringUtil.isEmpty(idfPythonEnvPath))
			{
				commands.add(idfPythonEnvPath);
			}
			commands.add(toolsPath);
			commands.add(IDFConstants.TOOLS_EXPORT_CMD);
			commands.add(IDFConstants.TOOLS_EXPORT_CMD_FORMAT_VAL);

			Logger.log(commands.toString());

			IStatus idfToolsExportStatus = new ProcessBuilderFactory().runInBackground(commands,
					org.eclipse.core.runtime.Path.ROOT, System.getenv());
			if (idfToolsExportStatus != null && idfToolsExportStatus.isOK())
			{
				String message = idfToolsExportStatus.getMessage();
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

	public void initCMakeToolChain(ICMakeToolChainManager manager)
	{
		String idfPath = IDFUtil.getIDFPath();
		if (StringUtil.isEmpty(idfPath))
		{
			Logger.log("IDF_PATH is not found to auto-configure the toolchains."); //$NON-NLS-1$
			return;
		}

		if (!new File(idfPath).exists())
		{
			String msg = MessageFormat.format(Messages.ESP32CMakeToolChainProvider_PathDoesnNotExist, idfPath);
			Logger.log(msg);
			return;
		}

		// add the newly found IDF_PATH to the eclipse environment variables if it's not
		// there
		IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
		if (!new File(idfEnvMgr.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).exists())
		{
			idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfPath);
		}

		toolchainElements.values().stream().forEach(value -> {
			Path toolChainFile = Paths.get(getIdfCMakePath(idfPath)).resolve(value.fileName);
			if (Files.exists(toolChainFile))
			{
				ICMakeToolChainFile toolchainFile = manager.newToolChainFile(toolChainFile);
				toolchainFile.setProperty(IToolChain.ATTR_OS, value.name);
				toolchainFile.setProperty(IToolChain.ATTR_ARCH, value.arch);
				toolchainFile.setProperty(ICBuildConfiguration.TOOLCHAIN_TYPE, ESPToolchain.TYPE_ID);
				toolchainFile.setProperty(ICBuildConfiguration.TOOLCHAIN_ID, value.id);

				manager.addToolChainFile(toolchainFile);
			}
		});
	}

	public Collection<Map<String, String>> getToolchainProperties(Map<String, ESPToolChainElement> map)
	{
		Collection<Map<String, String>> propertiesList = new ArrayList<>();
		for (ESPToolChainElement toolChainElement : map.values())
		{
			Map<String, String> esp = new HashMap<>();
			esp.put(IToolChain.ATTR_OS, toolChainElement.name);
			esp.put(IToolChain.ATTR_ARCH, toolChainElement.arch);
			esp.put(TOOLCHAIN_ATTR_ID, toolChainElement.fileName);

			propertiesList.add(esp);
		}

		return propertiesList;
	}

	public List<String> getAvailableEspTargetList()
	{
		Set<String> targetSet = new HashSet<>();
		for (IToolChain toolchain : getAllEspToolchains())
		{
			targetSet.add(toolchain.getProperty(IToolChain.ATTR_OS));
		}
		return targetSet.stream().collect(Collectors.toList());
	}

	public Collection<IToolChain> getAllEspToolchains()
	{
		IToolChainManager tcManager = CCorePlugin.getService(IToolChainManager.class);
		Collection<IToolChain> toolchains = Collections.emptyList();
		try
		{
			toolchains = tcManager.getAllToolChains();
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return toolchains.stream().filter(tc -> tc.getProperty(IToolChain.ATTR_OS).contains("esp")) //$NON-NLS-1$
				.collect(Collectors.toList());

	}

	public void addToolchainBasedTargets(ILaunchTargetManager targetManager)
	{
		Collection<IToolChain> toolchainsWithoutDuplicateTargets = getAllEspToolchains().stream()
				.filter(distinctByOs(tc -> tc.getProperty(IToolChain.ATTR_OS))).collect(Collectors.toList());

		try
		{
			addLaunchTargets(targetManager, toolchainsWithoutDuplicateTargets);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	private <T> Predicate<T> distinctByOs(Function<? super T, Object> extractor)
	{
		HashSet<Object> osSet = new HashSet<>();
		return t -> osSet.add(extractor.apply(t));
	}

	private void addLaunchTargets(ILaunchTargetManager targetManager,
			Collection<IToolChain> toolchainsWithoutDuplicateTargets) throws SecurityException, IllegalArgumentException
	{

		for (IToolChain toolchain : toolchainsWithoutDuplicateTargets)
		{
			String os = toolchain.getProperty(IToolChain.ATTR_OS);
			String arch = toolchain.getProperty(IToolChain.ATTR_ARCH);

			if (targetManager.getLaunchTarget(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE, os) == null)
			{
				ILaunchTarget target = targetManager.addLaunchTarget(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE, os);
				ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
				wc.setAttribute(ILaunchTarget.ATTR_OS, os);
				wc.setAttribute(ILaunchTarget.ATTR_ARCH, arch);
				wc.setAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, os);
				wc.save();
			}
		}
	}

	protected String getIdfCMakePath(String idfPath)
	{
		return idfPath + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR + IDFConstants.CMAKE_FOLDER;
	}

	/**
	 * Configure the file in the preferences and initialize the launch bar with available targets
	 */
	public void configureToolChain()
	{
		IToolChainManager tcManager = CCorePlugin.getService(IToolChainManager.class);
		ICMakeToolChainManager cmakeTcManager = CCorePlugin.getService(ICMakeToolChainManager.class);

		initToolChain(tcManager, ESPToolChainProvider.ID);
		initCMakeToolChain(cmakeTcManager);
		addToolchainBasedTargets(IDFCorePlugin.getService(ILaunchTargetManager.class));
	}

}

class ESPToolChainElement
{
	public final String name;
	public final String id;
	public final String arch;
	public final String fileName;
	public final String compilerPattern;
	public final String debuggerPattern;

	public ESPToolChainElement(String name, String id, String arch, String fileName, String compilerPattern,
			String debuggerPatten)
	{
		this.name = name; // os or target name
		this.id = id;
		this.arch = arch;
		this.fileName = fileName;
		this.compilerPattern = compilerPattern;
		this.debuggerPattern = debuggerPatten;
	}
}
