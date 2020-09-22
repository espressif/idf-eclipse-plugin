/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.build;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.cdt.cmake.core.internal.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.internal.CMakeUtils;
import org.eclipse.cdt.cmake.core.internal.CompileCommand;
import org.eclipse.cdt.cmake.core.internal.Messages;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChain2;
import org.eclipse.cdt.core.build.ScannerInfoCache;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeFileEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IMacroFileEntry;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeExportPatterns;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.parser.ParserSettings2;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.osgi.service.environment.Constants;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 * 
 * All the processLine() dependent methods will be removed once the CDT indexing issue is fixed.
 *
 */
@SuppressWarnings("restriction")
public class IDFBuildConfiguration extends CMakeBuildConfiguration
{

	private ILaunchTarget launchtarget;
	private Object scannerInfoLock = new Object();
	private ScannerInfoCache scannerInfoCache;
	private String name;
	private IToolChain toolchain;
	private static final List<String> DEFAULT_COMMAND = new ArrayList<>(0);
	
	
	public IDFBuildConfiguration(IBuildConfiguration config, String name) throws CoreException
	{
		super(config, name);
		this.name = name;
	}

	public IDFBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain)
	{
		super(config, name, toolChain, null, "run"); //$NON-NLS-1$
		this.toolchain = toolChain;
		this.name = name;
	}

	public IDFBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			ICMakeToolChainFile toolChainFile, String launchMode)
	{
		super(config, name, toolChain, toolChainFile, launchMode);
		this.toolchain = toolChain;
		this.name = name;

	}

	@Override
	public Path getBuildDirectory() throws CoreException
	{
		IProject project = getProject();
		String absolutePath = project.getLocation().toFile().getAbsolutePath();
		return Paths.get(absolutePath, IDFConstants.BUILD_FOLDER);

	}

	@Override
	public IContainer getBuildContainer() throws CoreException
	{
		IProject project = getProject();
		IFolder buildRootFolder = project.getFolder(IDFConstants.BUILD_FOLDER);

		IProgressMonitor monitor = new NullProgressMonitor();
		if (!buildRootFolder.exists())
		{
			buildRootFolder.create(IResource.FORCE | IResource.DERIVED, true, monitor);
		}

		return buildRootFolder;
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException
	{
		IProject project = getProject();
		ICMakeToolChainFile toolChainFile = getToolChainFile();

		try
		{
			if (launchtarget == null || !launchtarget.getTypeId().equals("com.espressif.idf.launch.serial.core.serialFlashTarget")) //$NON-NLS-1$
			{
				console.getErrorStream().write("No esp launch target found. Please create/select the correct 'Launch Target'"); //$NON-NLS-1$
				return null;
			}
			
			//Check for spaces in the project path
			if (project.getLocation().toOSString().contains(" ")) //$NON-NLS-1$
			{
				console.getErrorStream().write("Project path can’t include space " + project.getLocation().toOSString()); //$NON-NLS-1$
				return null;
			}
			
			String generator = getProperty(CMAKE_GENERATOR);
			if (generator == null)
			{
				generator = "Ninja"; //$NON-NLS-1$
			}

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();
			if (!buildDir.toFile().exists())
			{
				buildDir.toFile().mkdir();
			}

			outStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingIn, buildDir.toString()));

			// Make sure we have a toolchain file if cross
			if (toolChainFile == null && !isLocal())
			{
				ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
				toolChainFile = manager.getToolChainFileFor(getToolChain());

				if (toolChainFile == null)
				{
					// error
					console.getErrorStream().write(Messages.CMakeBuildConfiguration_NoToolchainFile);
					return null;
				}
			}

			boolean runCMake;
			switch (generator)
			{
			case "Ninja": //$NON-NLS-1$
				runCMake = !Files.exists(buildDir.resolve("build.ninja")); //$NON-NLS-1$
				break;
			default:
				runCMake = !Files.exists(buildDir.resolve("CMakeFiles")); //$NON-NLS-1$
			}

			if (runCMake)
			{

				List<String> command = new ArrayList<>();

				command.add("cmake"); //$NON-NLS-1$
				command.add("-G"); //$NON-NLS-1$
				command.add(generator);

				if (toolChainFile != null)
				{
					command.add("-DCMAKE_TOOLCHAIN_FILE=" + toolChainFile.getPath().toString()); //$NON-NLS-1$
				}

				command.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"); //$NON-NLS-1$

				if (launchtarget != null)
				{
					String idfTargetName = launchtarget.getAttribute("com.espressif.idf.launch.serial.core.idfTarget", //$NON-NLS-1$
							"");
					if (!idfTargetName.isEmpty())
					{
						command.add("-DIDF_TARGET=" + idfTargetName); //$NON-NLS-1$
					}
				}

				String userArgs = getProperty(CMAKE_ARGUMENTS);
				if (userArgs != null)
				{
					command.addAll(Arrays.asList(userArgs.trim().split("\\s+"))); //$NON-NLS-1$
				}

				command.add(new File(project.getLocationURI()).getAbsolutePath());

				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
						getBuildDirectory().toString());
				Process p = startBuildProcess(command, new IEnvironmentVariable[0], workingDir, console, monitor);
				if (p == null)
				{
					console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
					return null;
				}

				watchProcess(p, console);
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds()))
			{
				epm.setOutputStream(console.getOutputStream());

				List<String> command = new ArrayList<>();

				String envStr = getProperty(CMAKE_ENV);
				List<IEnvironmentVariable> envVars = new ArrayList<>();
				if (envStr != null)
				{
					List<String> envList = CMakeUtils.stripEnvVars(envStr);
					for (String s : envList)
					{
						int index = s.indexOf("="); //$NON-NLS-1$
						if (index == -1)
						{
							envVars.add(new EnvironmentVariable(s));
						}
						else
						{
							envVars.add(new EnvironmentVariable(s.substring(0, index), s.substring(index + 1)));
						}
					}
				}

				String buildCommand = getProperty(BUILD_COMMAND);
				if (buildCommand == null)
				{
					command.add("cmake"); //$NON-NLS-1$
					command.add("--build"); //$NON-NLS-1$
					command.add("."); //$NON-NLS-1$
					if ("Ninja".equals(generator))
					{
						command.add("--"); //$NON-NLS-1$
						command.add("-v"); //$NON-NLS-1$
					}

				}
				else
				{
					command.addAll(Arrays.asList(buildCommand.split(" "))); //$NON-NLS-1$
				}

				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
						getBuildDirectory().toString());
				Process p = startBuildProcess(command, envVars.toArray(new IEnvironmentVariable[0]), workingDir,
						console, monitor);
				if (p == null)
				{
					console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
					return null;
				}

				watchProcess(p, new IConsoleParser[] { epm });

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

				// Load compile_commands.json file
				processCompileCommandsFile(monitor);

				outStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingComplete, epm.getErrorCount(),
						epm.getWarningCount(), buildDir.toString()));
			}

			//This is specifically added to trigger the indexing sine in Windows OS it doesn't seem to happen!
			refreshScannerInfo();
			return new IProject[] { project };
		}
		catch (IOException e)
		{
			throw new CoreException(Activator
					.errorStatus(String.format(Messages.CMakeBuildConfiguration_Building, project.getName()), e));
		}
	}

	private boolean isLocal() throws CoreException
	{
		IToolChain toolchain = getToolChain();
		return (Platform.getOS().equals(toolchain.getProperty(IToolChain.ATTR_OS))
				|| "linux-container".equals(toolchain.getProperty(IToolChain.ATTR_OS))) //$NON-NLS-1$
				&& (Platform.getOSArch().equals(toolchain.getProperty(IToolChain.ATTR_ARCH)));
	}

	private void processCompileCommandsFile(IProgressMonitor monitor) throws CoreException
	{
		IProject project = getProject();
		Path commandsFile = getBuildDirectory().resolve("compile_commands.json"); //$NON-NLS-1$
		if (Files.exists(commandsFile))
		{
			List<Job> jobsList = new ArrayList<>();
			monitor.setTaskName(Messages.CMakeBuildConfiguration_ProcCompJson);
			try (FileReader reader = new FileReader(commandsFile.toFile()))
			{
				Gson gson = new Gson();
				CompileCommand[] commands = gson.fromJson(reader, CompileCommand[].class);
				Map<String, CompileCommand> dedupedCmds = new HashMap<>();
				for (CompileCommand command : commands)
				{
					dedupedCmds.put(command.getFile(), command);
				}
				for (CompileCommand command : dedupedCmds.values())
				{
					processLine(command.getCommand(), jobsList);
				}
				for (Job j : jobsList)
				{
					try
					{
						j.join();
					}
					catch (InterruptedException e)
					{
						// ignore
					}
				}
				shutdown();
			}
			catch (IOException e)
			{
				throw new CoreException(Activator.errorStatus(
						String.format(Messages.CMakeBuildConfiguration_ProcCompCmds, project.getName()), e));
			}
		}
	}

	private List<String> stripArgs(String argString) {
		String[] args = CommandLineUtil.argumentsToArray(argString);
		return new ArrayList<>(Arrays.asList(args));
	}
	
	private static final String NEED_REFRESH = "cdt.needScannerRefresh"; //$NON-NLS-1$

	private static boolean isWindows() {
		boolean osWin;
		try {
			osWin = Platform.getOS().equals(Constants.OS_WIN32);
		} catch (Exception e) {
			osWin = false;
		}
		return osWin;
	}
	
	/**
	 * Process a compile line for Scanner info in a separate job
	 *
	 * @param line - line to process
	 * @param jobsArray - array of Jobs to keep track of open scanner info jobs
	 * @return - true if line processed, false otherwise
	 *
	 * @since 6.5
	 */
	@Override
	public boolean processLine(String line, List<Job> jobsArray) {
		// Split line into args, taking into account quotes
		List<String> command = stripArgs(line);
		
		if (isWindows())
		{
			List<String> newcmds = new ArrayList<String>();
			for (String cmd : command) {
				String expandedCmd =  expandShortFileName(cmd);
				newcmds.add(expandedCmd);
			}
			command = newcmds;
		}
		
		try
		{
			this.toolchain = getToolChain();
		}
		catch (CoreException e1)
		{
			Logger.log(e1);
		}
		
		String[] compileCommands = toolchain.getCompileCommands();
		boolean found = false;
		loop: for (String arg : command) {
			// TODO we should really ask the toolchain, not all args start with '-'
			if (arg.startsWith("-")) { //$NON-NLS-1$
				// option found, missed our command
				return false;
			}

			for (String cc : compileCommands) {
				if (arg.endsWith(cc) && (arg.equals(cc) || arg.endsWith("/" + cc) || arg.endsWith("\\" + cc))) { //$NON-NLS-1$ //$NON-NLS-2$
					found = true;
					break loop;
				}
			}

			if (Platform.getOS().equals("win32") && !arg.endsWith(".exe")) { //$NON-NLS-1$
				// Try with exe
				arg = arg + ".exe"; //$NON-NLS-1$
				for (String cc : compileCommands) {
					if (arg.endsWith(cc) && (arg.equals(cc) || arg.endsWith("/" + cc) || arg.endsWith("\\" + cc))) { //$NON-NLS-1$ //$NON-NLS-2$
						found = true;
						break loop;
					}
				}
			}
		}

		if (!found) {
			return false;
		}

		try {
			IResource[] resources = toolchain.getResourcesFromCommand(command, getBuildDirectoryURI());
			if (resources != null && resources.length > 0) {
				List<String> commandStrings = toolchain.stripCommand(command, resources);

				boolean needScannerRefresh = false;

				if (toolchain instanceof IToolChain2) {
					String needRefresh = toolchain.getProperty(NEED_REFRESH);
					if ("true".equals(needRefresh)) { //$NON-NLS-1$
						needScannerRefresh = true;
					}
				}

				for (IResource resource : resources) {
					loadScannerInfoCache();
					boolean hasCommand = true;
					synchronized (scannerInfoLock) {
						if (scannerInfoCache.hasCommand(commandStrings)) {
							IExtendedScannerInfo info = scannerInfoCache.getScannerInfo(commandStrings);
							if (info.getIncludePaths().length == 0) {
								needScannerRefresh = true;
							}
							if (!scannerInfoCache.hasResource(commandStrings, resource)) {
								scannerInfoCache.addResource(commandStrings, resource);
								infoChanged = true;
							}
						} else {
							hasCommand = false;
						}
					}
					if (!hasCommand || needScannerRefresh) {
						Path commandPath = findCommand(command.get(0));
						if (commandPath != null) {
							command.set(0, commandPath.toString());
							Job job = new ScannerInfoJob(
									String.format("Calculating scanner info for %s", resource),
									getToolChain(), command, resource, getBuildDirectoryURI(), commandStrings);
							job.schedule();
							jobsArray.add(job);
						}
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	private static String expandShortFileName(String commandLine) {
	    if (commandLine.indexOf('~', 6) == -1) {
	      // not a short file name
	      return commandLine;
	    }
	    String command;
	    StringBuilder commandLine2 = new StringBuilder();
	    // split at first space character
	    int idx = commandLine.indexOf(' ');
	    if (idx != -1) {
	      command = commandLine.substring(0, idx);
	      commandLine2.append(commandLine.substring(idx));
	    } else {
	      command = commandLine;
	    }
	    // convert to long file name and retry lookup
	    try {
	      command = new File(command).getCanonicalPath();
	      commandLine2.insert(0, command);
	      return commandLine2.toString();
	    } catch (IOException e) {
	      //
	    }
	    return commandLine;
	  }
	public void setLaunchTarget(ILaunchTarget target)
	{
		this.launchtarget = target;
	}

	@Override
	protected Path findCommand(String command)
	{
		try
		{
			Path cmdPath = Paths.get(command);
			if (cmdPath.isAbsolute())
			{
				return cmdPath;
			}

			Map<String, String> env = new HashMap<>(System.getenv());
			setBuildEnvironment(env);

			String pathStr = env.get("PATH"); //$NON-NLS-1$
			if (pathStr == null)
			{
				pathStr = env.get("Path"); // for Windows //$NON-NLS-1$
				if (pathStr == null)
				{
					return null; // no idea
				}
			}
			String[] path = pathStr.split(File.pathSeparator);
			for (String dir : path)
			{
				Path commandPath = Paths.get(dir, command);
				if (Files.exists(commandPath) && commandPath.toFile().isFile())
				{
					return commandPath;
				}
				else
				{
					if (Platform.getOS().equals(org.eclipse.core.runtime.Platform.OS_WIN32)
							&& !(command.endsWith(".exe") || command.endsWith(".bat"))) //$NON-NLS-1$ //$NON-NLS-2$
					{
						commandPath = Paths.get(dir, command + ".exe"); //$NON-NLS-1$
						if (Files.exists(commandPath))
						{
							return commandPath;
						}
					}
				}
			}
			IToolChain tc = getToolChain();
			if (tc instanceof IToolChain2)
			{
				// we may have a Container build...default to Path based on command
				return Paths.get(command);
			}
		}
		catch (InvalidPathException e)
		{
			// ignore
		}
		catch (CoreException e)
		{
			// ignore
		}
		return null;
	}
	
	
	private File getScannerInfoCacheFile() {
		return CCorePlugin.getDefault().getStateLocation().append("infoCache") //$NON-NLS-1$
				.append(getProject().getName()).append(name + ".json").toFile(); //$NON-NLS-1$
	}

	private static class IExtendedScannerInfoCreator implements JsonDeserializer<IExtendedScannerInfo> {
		@Override
		public IExtendedScannerInfo deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)
				throws JsonParseException {
			JsonObject infoObj = element.getAsJsonObject();

			Map<String, String> definedSymbols = null;
			if (infoObj.has("definedSymbols")) { //$NON-NLS-1$
				JsonObject definedSymbolsObj = infoObj.get("definedSymbols").getAsJsonObject(); //$NON-NLS-1$
				definedSymbols = new HashMap<>();
				for (Entry<String, JsonElement> entry : definedSymbolsObj.entrySet()) {
					definedSymbols.put(entry.getKey(), entry.getValue().getAsString());
				}
			}

			String[] includePaths = null;
			if (infoObj.has("includePaths")) { //$NON-NLS-1$
				JsonArray includePathsArray = infoObj.get("includePaths").getAsJsonArray(); //$NON-NLS-1$
				List<String> includePathsList = new ArrayList<>(includePathsArray.size());
				for (Iterator<JsonElement> i = includePathsArray.iterator(); i.hasNext();) {
					includePathsList.add(i.next().getAsString());
				}
				includePaths = includePathsList.toArray(new String[includePathsList.size()]);
			}

			IncludeExportPatterns includeExportPatterns = null;
			if (infoObj.has("includeExportPatterns")) { //$NON-NLS-1$
				JsonObject includeExportPatternsObj = infoObj.get("includeExportPatterns").getAsJsonObject(); //$NON-NLS-1$
				String exportPattern = null;
				if (includeExportPatternsObj.has("includeExportPattern")) { //$NON-NLS-1$
					exportPattern = includeExportPatternsObj.get("includeExportPattern") //$NON-NLS-1$
							.getAsJsonObject().get("pattern").getAsString(); //$NON-NLS-1$
				}

				String beginExportsPattern = null;
				if (includeExportPatternsObj.has("includeBeginExportPattern")) { //$NON-NLS-1$
					beginExportsPattern = includeExportPatternsObj.get("includeBeginExportPattern") //$NON-NLS-1$
							.getAsJsonObject().get("pattern").getAsString(); //$NON-NLS-1$
				}

				String endExportsPattern = null;
				if (includeExportPatternsObj.has("includeEndExportPattern")) { //$NON-NLS-1$
					endExportsPattern = includeExportPatternsObj.get("includeEndExportPattern") //$NON-NLS-1$
							.getAsJsonObject().get("pattern").getAsString(); //$NON-NLS-1$
				}

				includeExportPatterns = new IncludeExportPatterns(exportPattern, beginExportsPattern,
						endExportsPattern);
			}

			ExtendedScannerInfo info = new ExtendedScannerInfo(definedSymbols, includePaths);
			info.setIncludeExportPatterns(includeExportPatterns);
			info.setParserSettings(new ParserSettings2());
			return info;
		}
	}

	/**
	 * @since 6.1
	 */
	protected void loadScannerInfoCache() {
		synchronized (scannerInfoLock) {
			if (scannerInfoCache == null) {
				File cacheFile = getScannerInfoCacheFile();
				if (cacheFile != null && cacheFile.exists()) {
					try (FileReader reader = new FileReader(cacheFile)) {
						GsonBuilder gsonBuilder = new GsonBuilder();
						gsonBuilder.registerTypeAdapter(IExtendedScannerInfo.class, new IExtendedScannerInfoCreator());
						Gson gson = gsonBuilder.create();
						scannerInfoCache = gson.fromJson(reader, ScannerInfoCache.class);
					} catch (IOException e) {
						CCorePlugin.log(e);
						scannerInfoCache = new ScannerInfoCache();
					}
				} else {
					scannerInfoCache = new ScannerInfoCache();
				}
				scannerInfoCache.initCache();
			}
		}
	}

	/**
	 * @since 6.1
	 */
	protected synchronized void saveScannerInfoCache() {
		File cacheFile = getScannerInfoCacheFile();
		if (!cacheFile.getParentFile().exists()) {
			try {
				Files.createDirectories(cacheFile.getParentFile().toPath());
			} catch (IOException e) {
				CCorePlugin.log(e);
				return;
			}
		}

		try (FileWriter writer = new FileWriter(getScannerInfoCacheFile())) {
			Gson gson = new Gson();
			synchronized (scannerInfoLock) {
				gson.toJson(scannerInfoCache, writer);
			}
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
	}

	/**
	 * @since 6.1
	 */
	protected ScannerInfoCache getScannerInfoCache() {
		return scannerInfoCache;
	}

	private IExtendedScannerInfo getBaseScannerInfo(IResource resource) throws CoreException {
		IPath resPath = resource.getFullPath();
		IIncludeEntry[] includeEntries = CoreModel.getIncludeEntries(resPath);
		String[] includes = new String[includeEntries.length];
		for (int i = 0; i < includeEntries.length; ++i) {
			includes[i] = includeEntries[i].getFullIncludePath().toOSString();
		}

		IIncludeFileEntry[] includeFileEntries = CoreModel.getIncludeFileEntries(resPath);
		String[] includeFiles = new String[includeFileEntries.length];
		for (int i = 0; i < includeFiles.length; ++i) {
			includeFiles[i] = includeFileEntries[i].getFullIncludeFilePath().toOSString();
		}

		IMacroEntry[] macros = CoreModel.getMacroEntries(resPath);
		Map<String, String> symbolMap = new HashMap<>();
		for (int i = 0; i < macros.length; ++i) {
			symbolMap.put(macros[i].getMacroName(), macros[i].getMacroValue());
		}

		IMacroFileEntry[] macroFileEntries = CoreModel.getMacroFileEntries(resPath);
		String[] macroFiles = new String[macroFileEntries.length];
		for (int i = 0; i < macroFiles.length; ++i) {
			macroFiles[i] = macroFileEntries[i].getFullMacroFilePath().toOSString();
		}
		return new ExtendedScannerInfo(symbolMap, includes, includeFiles, macroFiles);
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		loadScannerInfoCache();
		IExtendedScannerInfo info = null;
		synchronized (scannerInfoLock) {
			info = scannerInfoCache.getScannerInfo(resource);
		}
		if (info == null || info.getIncludePaths().length == 0) {
			ICElement celement = CCorePlugin.getDefault().getCoreModel().create(resource);
			if (celement instanceof ITranslationUnit) {
				try {
					ITranslationUnit tu = (ITranslationUnit) celement;
					info = getToolChain().getDefaultScannerInfo(getBuildConfiguration(), getBaseScannerInfo(resource),
							tu.getLanguage(), getBuildDirectoryURI());
					synchronized (scannerInfoLock) {
						scannerInfoCache.addScannerInfo(DEFAULT_COMMAND, info, resource);
					}
					saveScannerInfoCache();
				} catch (CoreException e) {
					CCorePlugin.log(e.getStatus());
				}
			}
		}
		return info;
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		// check if the path entries changed in the project and clear the cache if so
		processElementDelta(event.getDelta());
	}

	private void processElementDelta(ICElementDelta delta) {
		if (delta == null) {
			return;
		}

		int flags = delta.getFlags();
		int kind = delta.getKind();
		if (kind == ICElementDelta.CHANGED) {
			if ((flags
					& (ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE | ICElementDelta.F_CHANGED_PATHENTRY_MACRO)) != 0) {
				IResource resource = delta.getElement().getResource();
				if (resource.getProject().equals(getProject())) {
					loadScannerInfoCache();
					synchronized (scannerInfoLock) {
						if (scannerInfoCache.hasResource(DEFAULT_COMMAND, resource)) {
							scannerInfoCache.removeResource(resource);
						} else {
							// Clear the whole command and exit the delta
							scannerInfoCache.removeCommand(DEFAULT_COMMAND);
							return;
						}
					}
				}
			}
		}

		ICElementDelta[] affectedChildren = delta.getAffectedChildren();
		for (int i = 0; i < affectedChildren.length; i++) {
			processElementDelta(affectedChildren[i]);
		}
	}

	private boolean infoChanged = false;

	private class ScannerInfoJob extends Job {
		private IToolChain toolchain;
		private List<String> command;
		private List<String> commandStrings;
		private IResource resource;
		private URI buildDirectoryURI;

		public ScannerInfoJob(String msg, IToolChain toolchain, List<String> command, IResource resource,
				URI buildDirectoryURI, List<String> commandStrings) {
			super(msg);
			this.toolchain = toolchain;
			this.command = command;
			this.commandStrings = commandStrings;
			this.resource = resource;
			this.buildDirectoryURI = buildDirectoryURI;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IExtendedScannerInfo info = toolchain.getScannerInfo(getBuildConfiguration(), command, null, resource,
					buildDirectoryURI);
			synchronized (scannerInfoLock) {
				scannerInfoCache.addScannerInfo(commandStrings, info, resource);
				infoChanged = true;
			}
			return Status.OK_STATUS;
		}
	}

	/**
	 * @since 6.5
	 * @throws CoreException
	 */
	protected void refreshScannerInfo() throws CoreException {
		CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(getProject()));
		infoChanged = false;
	}

	@Override
	public void shutdown() {
		// TODO persist changes

		// Trigger a reindex if anything changed
		// TODO be more surgical
		if (infoChanged) {
			saveScannerInfoCache();
			CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(getProject()));
			infoChanged = false;
		}
	}
	
	
}
