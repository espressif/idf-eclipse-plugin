/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.toolchain.ESPToolChainManager;
import com.espressif.idf.core.tools.util.ToolsUtility;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.vo.IdfInstalled;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * This job is responsible for setting up the esp-idf in the IDE
 * @author Ali Azam Rana
 *
 */
public class SetupToolsInIde extends Job
{
	private EimJson eimJson;
	private IdfInstalled idfInstalled;
	private MessageConsoleStream errorConsoleStream;
	private MessageConsoleStream standardConsoleStream;
	private Map<String, String> envVarsFromActivationScriptMap;
	private Map<String, String> existingEnvVarsInIdeForRollback;
	
	public SetupToolsInIde(IdfInstalled idfInstalled, EimJson eimJson, MessageConsoleStream errorConsoleStream, MessageConsoleStream standardConsoleStream)
	{
		super("Tools Setup"); //$NON-NLS-1$
		this.idfInstalled = idfInstalled;
		this.eimJson = eimJson;
		this.errorConsoleStream = errorConsoleStream;
		this.standardConsoleStream = standardConsoleStream;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		return setupTools(monitor);
	}

	public void rollback()
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		
		for (Entry<String, String> entry : existingEnvVarsInIdeForRollback.entrySet())
		{
			idfEnvironmentVariables.addEnvVariable(entry.getKey(), entry.getValue());
		}
		IDFUtil.updateEspressifPrefPageOpenocdPath();
		try
		{
			setUpToolChainsAndTargets(true);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}
	
	private IStatus setupTools(IProgressMonitor monitor)
	{
		monitor.beginTask("Setting up tools in IDE", 7); //$NON-NLS-1$
		monitor.worked(1);
		List<String> arguemnts = new ArrayList<>();
		Map<String, String> env = new HashMap<>(System.getenv());
		addGitToEnvironment(env, eimJson.getGitPath());
		arguemnts = ToolsUtility.getExportScriptCommand(idfInstalled.getActivationScript());
		final ProcessBuilderFactory processBuilderFactory = new ProcessBuilderFactory();
		try
		{
			monitor.setTaskName("Running Activation Script"); //$NON-NLS-1$
			IStatus status = processBuilderFactory.runInBackground(arguemnts, Path.ROOT, System.getenv());
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status returned null from activation script process", null)); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Status returned null from activation script process", null); //$NON-NLS-1$
			}
			
			if (status.getSeverity() == IStatus.ERROR)
			{
				log(status.getException() != null ? status.getException().getMessage() : status.getMessage(), IStatus.ERROR);
				return status;
			}
			
			// now setup in IDE
			
			String activationScriptOutput = status.getMessage();
			if (StringUtil.isEmpty(activationScriptOutput))
			{
				log("Activation Script Output must not be empty", IStatus.ERROR); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Activation Script Output must not be empty", null); //$NON-NLS-1$
			}
			
			monitor.worked(1);
			
			log(activationScriptOutput, IStatus.OK);
			monitor.setTaskName("Processing output from activation script"); //$NON-NLS-1$
			log("Processing output from activation script", IStatus.INFO); //$NON-NLS-1$
			envVarsFromActivationScriptMap = parseEnvKeys(activationScriptOutput);
			monitor.worked(1);
			
			monitor.setTaskName("Setting up IDE environment"); //$NON-NLS-1$
			log("Setting up IDE environment variables", IStatus.INFO); //$NON-NLS-1$
			setupEnvVarsInEclipse();
			monitor.worked(1);

			monitor.setTaskName("Setting up toolchains"); //$NON-NLS-1$
			log("Setting up toolchains", IStatus.INFO); //$NON-NLS-1$
			setUpToolChainsAndTargets(false);
			monitor.worked(1);
			
			monitor.setTaskName("Installing pyhton dependency web-socket"); //$NON-NLS-1$
			log("Installing pyhton dependency web-socket", IStatus.INFO); //$NON-NLS-1$
			handleWebSocketClientInstall();
			monitor.worked(1);
			
			monitor.setTaskName("Copying OpenOCD Rules"); //$NON-NLS-1$
			log("Copying OpenOCD Rules", IStatus.INFO); //$NON-NLS-1$
			copyOpenOcdRules();
			monitor.worked(1);
			
			log("Tools Setup complete", IStatus.INFO); //$NON-NLS-1$
			
			return Status.OK_STATUS;
		}
		catch (IOException e)
		{
			Logger.log(e);
			return IDFCorePlugin.errorStatus(e.getMessage(), e);
		}
	}
	
	private void copyOpenOcdRules()
	{
		if (Platform.getOS().equals(Platform.OS_LINUX)
				&& !IDFUtil.getOpenOCDLocation().equalsIgnoreCase(StringUtil.EMPTY))
		{
			log(Messages.InstallToolsHandler_CopyingOpenOCDRules, IStatus.OK);
			// Copy the rules to the idf
			StringBuilder pathToRules = new StringBuilder();
			pathToRules.append(IDFUtil.getOpenOCDLocation());
			pathToRules.append("/../share/openocd/contrib/60-openocd.rules"); //$NON-NLS-1$
			File rulesFile = new File(pathToRules.toString());
			if (rulesFile.exists())
			{
				java.nio.file.Path source = Paths.get(pathToRules.toString());
				java.nio.file.Path target = Paths.get("/etc/udev/rules.d/60-openocd.rules"); //$NON-NLS-1$
				log(String.format(Messages.InstallToolsHandler_OpenOCDRulesCopyPaths, source.toString(),
						target.toString()), IStatus.OK);

				Display.getDefault().syncExec(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if (target.toFile().exists())
							{
								MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(),
										SWT.ICON_WARNING | SWT.YES | SWT.NO);
								messageBox.setText(Messages.InstallToolsHandler_OpenOCDRulesCopyWarning);
								messageBox.setMessage(Messages.InstallToolsHandler_OpenOCDRulesCopyWarningMessage);
								int response = messageBox.open();
								if (response == SWT.YES)
								{
									Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
								}
								else
								{
									log(Messages.InstallToolsHandler_OpenOCDRulesNotCopied, IStatus.ERROR);
									return;
								}
							}
							else
							{
								Files.copy(source, target);
							}

							log(Messages.InstallToolsHandler_OpenOCDRulesCopied, IStatus.OK);
						}
						catch (IOException e)
						{
							Logger.log(e);
							log(Messages.InstallToolsHandler_OpenOCDRulesCopyError, IStatus.ERROR);
						}
					}
				});
			}
		}
	}
	
	private IStatus handleWebSocketClientInstall()
	{
		String websocketClient = "websocket-client"; //$NON-NLS-1$
		// pip install websocket-client
		List<String> arguments = new ArrayList<String>();
		final String pythonEnvPath = idfInstalled.getPython();
		if (pythonEnvPath == null || !new File(pythonEnvPath).exists())
		{
			log(String.format("%s executable not found. Unable to run `%s -m pip install websocket-client`", //$NON-NLS-1$
					IDFConstants.PYTHON_CMD, IDFConstants.PYTHON_CMD), IStatus.ERROR);
			return IDFCorePlugin.errorStatus(
					String.format("%s executable not found. Unable to run `%s -m pip install websocket-client`", //$NON-NLS-1$
							IDFConstants.PYTHON_CMD, IDFConstants.PYTHON_CMD),
					null);
		}
		arguments.add(pythonEnvPath);
		arguments.add("-m"); //$NON-NLS-1$
		arguments.add("pip"); //$NON-NLS-1$
		arguments.add("list"); //$NON-NLS-1$

		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();

		try
		{
			log("Executing " + arguments.toString(), IStatus.OK); //$NON-NLS-1$)

			Map<String, String> environment = new HashMap<>(System.getenv());
			prepEnvMap(environment);
			Logger.log(environment.toString());

			IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(),
						IDFCorePlugin.errorStatus("Unable to get the process status.", null)); //$NON-NLS-1$
				log("Unable to get the process status", IStatus.ERROR); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Unable to get the process status.", null); //$NON-NLS-1$
			}

			String cmdOutput = status.getMessage();
			if (cmdOutput.contains(websocketClient))
			{
				return IDFCorePlugin.okStatus("websocket-client already installed", null); //$NON-NLS-1$
			}

			// websocket client not installed so installing it now.
			arguments.remove(arguments.size() - 1);
			arguments.add("install"); //$NON-NLS-1$
			arguments.add(websocketClient);

			status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(),
						IDFCorePlugin.errorStatus("Unable to get the process status.", null)); //$NON-NLS-1$
				log("Unable to get the process status", IStatus.ERROR); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Unable to get the process status.", null); //$NON-NLS-1$
			}

			log(status.getMessage(), IStatus.OK);

			return status;
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
			if (errorConsoleStream != null)
			{
				errorConsoleStream.println(e1.getLocalizedMessage());
			}
			return IDFCorePlugin.errorStatus(e1.getLocalizedMessage(), e1); // $NON-NLS-1$;
		}
	}
	
	private String replacePathVariable(String value)
	{
		// Get system PATH
		Map<String, String> systemEnv = new HashMap<>(System.getenv());
		String pathEntry = systemEnv.get("PATH"); //$NON-NLS-1$
		if (pathEntry == null)
		{
			pathEntry = systemEnv.get("Path"); // for Windows //$NON-NLS-1$
			if (pathEntry == null) // no idea
			{
				Logger.log(new Exception("No PATH found in the system environment variables")); //$NON-NLS-1$
			}
		}

		if (!StringUtil.isEmpty(pathEntry))
		{
			value = value.concat(File.pathSeparator).concat(pathEntry);
		}
		
		if (Platform.getOS().equals(Platform.OS_MACOSX))
		{
			value = value.concat(File.pathSeparator).concat("/opt/homebrew/bin").concat(File.pathSeparator).concat("/usr/local/bin"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return value;
	}
	
	private IStatus loadTargetsAvailableFromIdfInCurrentToolSet(boolean rollback)
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(rollback
				? existingEnvVarsInIdeForRollback.get(IDFEnvironmentVariables.PYTHON_EXE_PATH)
				: idfInstalled.getPython());
		arguments
				.add(IDFUtil
						.getIDFPythonScriptFile(
								rollback ? existingEnvVarsInIdeForRollback.get(IDFEnvironmentVariables.IDF_PATH)
										: envVarsFromActivationScriptMap.get(IDFEnvironmentVariables.IDF_PATH))
						.getAbsolutePath());
		arguments.add(IDFConstants.IDF_LIST_TARGETS_CMD);
		log("Executing:" + arguments.toString(), IStatus.OK); //$NON-NLS-1$
		
		Map<String, String> env = new HashMap<String, String>(System.getenv());
		prepEnvMap(env);
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		StringBuilder output = new StringBuilder();
		int waitCount = 10;
		try
		{
			Process process = processRunner.run(arguments, org.eclipse.core.runtime.Path.ROOT, env);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
			{
				output.append(line).append(System.lineSeparator());
			}
			
			while (process.isAlive() && waitCount > 0)
			{
				try
				{
					Thread.sleep(300);					
				}
				catch (InterruptedException e)
				{
					Logger.log(e);
				}
				waitCount--;
			}
			
			if (waitCount == 0)
			{
				log("Process possibly stuck", IStatus.ERROR); //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			}
			
			IStatus status = new Status(process.exitValue() == 0 ? IStatus.OK : IStatus.ERROR, IDFCorePlugin.PLUGIN_ID,
					process.exitValue(), output.toString(), null);
			if (status.getSeverity() == IStatus.ERROR)
			{
				log(status.getException() != null ? status.getException().getMessage() : status.getMessage(), IStatus.ERROR);
			}
			
			log(status.getMessage(), IStatus.OK);

			return status;
		}
		catch (IOException e)
		{
			log(e.getMessage(), e);
			return IDFCorePlugin.errorStatus(e.getMessage(), e);
		}
	}
	
	private void prepEnvMap(Map<String, String> env)
	{
		env.put("PYTHONUNBUFFERED", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		loadIdfPathWithSystemPath(env);
		addGitToEnvironment(env, eimJson.getGitPath());
	}
	
	private void loadIdfPathWithSystemPath(Map<String, String> systemEnv)
	{
		String idfExportPath = envVarsFromActivationScriptMap.get(IDFEnvironmentVariables.PATH);
		String pathVar = "PATH"; // for Windows //$NON-NLS-1$
		String pathEntry = systemEnv.get(pathVar); // $NON-NLS-1$
		if (pathEntry == null)
		{
			pathVar = "Path"; //$NON-NLS-1$
			pathEntry = systemEnv.get(pathVar);
			if (pathEntry == null) // no idea
			{
				Logger.log(new Exception("No PATH found in the system environment variables")); //$NON-NLS-1$
			}
		}

		if (!StringUtil.isEmpty(pathEntry))
		{
			idfExportPath = idfExportPath.replace("$PATH", pathEntry); // macOS //$NON-NLS-1$
			idfExportPath = idfExportPath.replace("%PATH%", pathEntry); // Windows //$NON-NLS-1$
		}
		systemEnv.put(pathVar, idfExportPath);
		for (Entry<String, String> entry : envVarsFromActivationScriptMap.entrySet())
		{
			if (entry.getKey().equals(IDFEnvironmentVariables.PATH))
				continue;

			systemEnv.put(entry.getKey(), entry.getValue());
		}
	}
	
	private void setUpToolChainsAndTargets(boolean rollback)
	{
		IStatus status = loadTargetsAvailableFromIdfInCurrentToolSet(rollback);
		if (status.getSeverity() == IStatus.ERROR)
		{
			Logger.log("Unable to get IDF targets from current toolset"); //$NON-NLS-1$
			return;
		}

		List<String> targets = extractTargets(status.getMessage());
		ESPToolChainManager espToolChainManager = new ESPToolChainManager();
		espToolChainManager.removeLaunchTargetsNotPresent(targets);
		espToolChainManager.removeCmakeToolChains();
		espToolChainManager.removeStdToolChains();
		espToolChainManager.configureToolChain(targets);
	}
	
	private List<String> extractTargets(String input)
	{
		List<String> targets = new ArrayList<String>();
		Pattern pattern = Pattern.compile("^esp32.*", Pattern.MULTILINE); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(input);
		while (matcher.find())
		{
			targets.add(matcher.group());
		}
		return targets;
	}
	
	private void setupEnvVarsInEclipse()
	{
		createExistingBackup();
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		for (Entry<String, String> entry : envVarsFromActivationScriptMap.entrySet())
		{
			idfEnvironmentVariables.addEnvVariable(entry.getKey(), entry.getValue());
		}
		
		String path = replacePathVariable(envVarsFromActivationScriptMap.get(IDFEnvironmentVariables.PATH));
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.PATH, path);
		
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.IDF_COMPONENT_MANAGER, "1"); //$NON-NLS-1$
		// IDF_MAINTAINER=1 to be able to build with the clang toolchain
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.IDF_MAINTAINER, "1"); //$NON-NLS-1$
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.ESP_IDF_EIM_ID, idfInstalled.getId());
		
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.PYTHON_EXE_PATH, idfInstalled.getPython());
		
		IDFUtil.updateEspressifPrefPageOpenocdPath();
	}

	private void createExistingBackup()
	{
		if (existingEnvVarsInIdeForRollback == null)
		{
			existingEnvVarsInIdeForRollback = new HashMap<>();
		}
		
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		existingEnvVarsInIdeForRollback.putAll(idfEnvironmentVariables.getEnvMap());
	}

	private Map<String, String> parseEnvKeys(String activationScriptOutput)
	{
		return Arrays.stream(activationScriptOutput.split(System.lineSeparator())).map(String::trim)
				.filter(line -> line.contains("=")) //$NON-NLS-1$
				.map(line -> line.split("=", 2)) //$NON-NLS-1$
				.filter(parts -> parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty())
				.collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));
	}

	private void log(final String message, final int severity)
	{
		Logger.log(message);
		if (severity == IStatus.ERROR)
		{
			printToErrorConsole(message);
		}
		else
		{
			printToStandardConsole(message);
		}
		
	}
	
	private void log(final String message, Exception e)
	{
		Logger.log(message);
		Logger.log(e);
		printToErrorConsole(message);

	}
	
	private void printToErrorConsole(String message)
	{
		if (errorConsoleStream != null)
		{
			errorConsoleStream.println(message);
		}
	}
	
	private void printToStandardConsole(String message)
	{
		if (standardConsoleStream != null)
		{
			standardConsoleStream.println(message);
		}
	}
	
	private void addGitToEnvironment(Map<String, String> envMap, String executablePath)
	{
		IPath gitPath = new Path(executablePath);
		if (gitPath.toFile().exists())
		{
			String gitDir = gitPath.removeLastSegments(1).toOSString();
			String path1 = envMap.get("PATH"); //$NON-NLS-1$
			String path2 = envMap.get("Path"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(path1) && !path1.contains(gitDir)) // Git not found on the PATH environment
			{
				path1 = gitDir.concat(";").concat(path1); //$NON-NLS-1$
				envMap.put("PATH", path1); //$NON-NLS-1$
			}
			else if (!StringUtil.isEmpty(path2) && !path2.contains(gitDir)) // Git not found on the Path environment
			{
				path2 = gitDir.concat(";").concat(path2); //$NON-NLS-1$
				envMap.put("Path", path2); //$NON-NLS-1$
			}
		}
	}
	
}
