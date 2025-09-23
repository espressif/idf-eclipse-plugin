/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD.
 * All rights reserved. Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.bug;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.SystemExecutableFinder;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.FileUtil;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * This class generates a bug report zip file containing: 1. Installed tools information 2. Product information 3. Basic
 * system information (OS, Arch, Memory) 4. IDE metadata logs 5. eim logs (if available)
 * 
 * The generated zip file is named with a timestamp and stored in the workspace directory.
 * 
 * @author Ali Azam Rana
 *
 */
public class BugReportGenerator
{
	private static final String JAVA_RUNTIME_VERSION_MSG = "Java Runtime Version:"; //$NON-NLS-1$
	private static final String OPERATING_SYSTEM_MSG = "Operating System:"; //$NON-NLS-1$
	private static final String ECLIPSE_CDT_MSG = "Eclipse CDT Version:"; //$NON-NLS-1$
	private static final String IDF_ECLIPSE_PLUGIN_VERSION_MSG = "IDF Eclipse Plugin Version:"; //$NON-NLS-1$
	private static final String ECLIPSE_VERSION_MSG = "Eclipse Version:"; //$NON-NLS-1$
	private static final String PYTHON_IDF_ENV_MSG = "Python set for IDF_PYTHON_ENV:"; //$NON-NLS-1$
	private static final String NOT_FOUND_MSG = "<NOT FOUND>"; //$NON-NLS-1$

	private static final String ECLIPSE_LOG_FILE_NAME = ".log"; //$NON-NLS-1$
	private static final String ECLIPSE_METADATA_DIRECTORY = ".metadata"; //$NON-NLS-1$
	private static final String UNKNOWN = "Unknown"; //$NON-NLS-1$
	private static final String BUG_REPORT_DIRECTORY_PREFIX = "bug_report_"; //$NON-NLS-1$
	private File bugReportDirectory;

	private enum ByteUnit
	{
		B("B"), //$NON-NLS-1$
		KB("KB"), //$NON-NLS-1$
		MB("MB"), //$NON-NLS-1$
		GB("GB"), //$NON-NLS-1$
		TB("TB"), //$NON-NLS-1$
		PB("PB"); //$NON-NLS-1$

		final String label;

		ByteUnit(String label)
		{
			this.label = label;
		}

		ByteUnit next()
		{
			int i = ordinal();
			return i < PB.ordinal() ? values()[i + 1] : PB;
		}
	}

	public BugReportGenerator()
	{
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")); //$NON-NLS-1$
		bugReportDirectory = getWorkspaceDirectory().resolve(BUG_REPORT_DIRECTORY_PREFIX + timestamp + File.separator)
				.toFile();
	}

	private File getEimLogPath()
	{
		String eimPath = StringUtil.EMPTY;
		switch (Platform.getOS())
		{
		case Platform.OS_WIN32:
			eimPath = System.getenv("APPDATA"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(eimPath))
			{
				eimPath = eimPath + "\\Local\\eim\\logs"; //$NON-NLS-1$
			}
			break;
		case Platform.OS_MACOSX:
			eimPath = System.getProperty("user.home"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(eimPath))
			{
				eimPath = eimPath + "/Library/Application Support/eim/logs"; //$NON-NLS-1$
			}
			break;
		case Platform.OS_LINUX:
			eimPath = System.getProperty("user.home"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(eimPath))
			{
				eimPath = eimPath + "/.local/share/.eim/logs"; //$NON-NLS-1$
			}
			break;
		default:
			break;
		}

		return new File(eimPath);
	}

	private Path getWorkspaceDirectory()
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		File workspaceRoot = workspace.getRoot().getLocation().toFile();
		return workspaceRoot.toPath();
	}

	private List<File> getIdeMetadataLogsFile()
	{
		File metadataDir = getWorkspaceDirectory().resolve(ECLIPSE_METADATA_DIRECTORY).toFile();
		File[] allFiles = metadataDir.listFiles();
		List<File> logFiles = new LinkedList<>();
		if (!metadataDir.exists() || !metadataDir.isDirectory() || allFiles == null)
		{
			return logFiles;
		}
		File activeLog = new File(metadataDir, ECLIPSE_LOG_FILE_NAME);
		LocalDate refDate = null;
		if (activeLog.exists() && activeLog.isFile())
		{
			refDate = Instant.ofEpochMilli(activeLog.lastModified()).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		for (File file : allFiles)
		{
			if (file.isDirectory())
			{
				continue;
			}

			String fileName = file.getName();
			if (fileName.equals("version.ini")) //$NON-NLS-1$
			{
				logFiles.add(file);
				continue;
			}

			if (fileName.endsWith(ECLIPSE_LOG_FILE_NAME))
			{
				if (fileName.equals(ECLIPSE_LOG_FILE_NAME))
				{
					logFiles.add(file);
					continue;
				}

				// Including only same day log files or one day earlier to ignore any late night logs.
				if (refDate != null)
				{
					LocalDate fileDate = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault())
							.toLocalDate();

					if (fileDate.equals(refDate) || fileDate.equals(refDate.minusDays(1)))
					{
						logFiles.add(file);
					}
				}

			}
		}

		return logFiles;
	}

	private File createBasicSystemInfoFile() throws IOException
	{
		String osName = System.getProperty("os.name", UNKNOWN); //$NON-NLS-1$
		String osVersion = System.getProperty("os.version", UNKNOWN); //$NON-NLS-1$
		String arch = System.getProperty("os.arch", UNKNOWN); //$NON-NLS-1$

		long freePhys = -1L;
		long totalPhys = -1L;
		try
		{
			com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
					.getOperatingSystemMXBean();
			freePhys = osBean.getFreeMemorySize();
			totalPhys = osBean.getTotalMemorySize();
		}
		catch (Throwable t)
		{
			// jdk.management module not present or different VM; leave values as -1.
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Basic System Info").append(System.lineSeparator()); //$NON-NLS-1$
		sb.append("==================").append(System.lineSeparator()); //$NON-NLS-1$
		sb.append("Generated: ").append(LocalDateTime.now()).append(System.lineSeparator()); //$NON-NLS-1$
		sb.append("OS       : ").append(osName).append(" ").append(osVersion).append(System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("Arch     : ").append(arch).append(System.lineSeparator()); //$NON-NLS-1$
		if (totalPhys >= 0 && freePhys >= 0)
		{
			long used = totalPhys - freePhys;
			sb.append("Memory   :").append(System.lineSeparator()); //$NON-NLS-1$
			sb.append("  Total         : ").append(humanBytes(totalPhys)).append(" (").append(totalPhys) //$NON-NLS-1$ //$NON-NLS-2$
					.append(" bytes)").append(System.lineSeparator()); //$NON-NLS-1$
			sb.append("  Available     : ").append(humanBytes(freePhys)).append(" (").append(freePhys).append(" bytes)") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.append(System.lineSeparator());
			sb.append("  In Use        : ").append(humanBytes(used)).append(" (").append(used).append(" bytes)") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.append(System.lineSeparator());
		}
		else
		{
			sb.append("Memory   : Unavailable (OS-level physical memory query unsupported on this JVM)") //$NON-NLS-1$
					.append(System.lineSeparator());
		}

		// Use your existing helper that writes content to a temp file with the given name.
		// This will create a file named exactly "basic_system_info".
		return FileUtil.createFileWithContentsInDirectory("basic_system_info", sb.toString(), //$NON-NLS-1$
				bugReportDirectory.getAbsolutePath());
	}

	private static String humanBytes(long bytes)
	{
		double v = (double) bytes;
		ByteUnit unit = ByteUnit.B;
		while (v >= 1024.0 && unit != ByteUnit.PB)
		{
			v /= 1024.0;
			unit = unit.next();
		}
		return String.format(Locale.ROOT, "%.2f %s", v, unit.label); //$NON-NLS-1$
	}

	/**
	 * Generates a bug report zip and also returns the path of the generated zip file.
	 * 
	 * @return
	 */
	public String generateBugReport()
	{
		if (!bugReportDirectory.exists())
		{
			bugReportDirectory.mkdirs();
		}

		try
		{
			File installedToolsFile = getInstalledToolsInfoFile();
			File productInfoFile = getProductInfoFile();
			File basicSysInfoFile = createBasicSystemInfoFile();

			List<File> filesToZip = new LinkedList<>();
			filesToZip.add(installedToolsFile);
			filesToZip.add(productInfoFile);
			filesToZip.add(basicSysInfoFile);

			List<File> metadataLogsFile = getIdeMetadataLogsFile();
			File ideLogDir = new File(bugReportDirectory.getAbsolutePath() + File.separator + "ide_metadata_logs"); //$NON-NLS-1$ )
			if (!ideLogDir.exists())
			{
				ideLogDir.mkdirs();
			}

			for (File logFile : metadataLogsFile)
			{
				FileUtil.copyFile(logFile, new File(ideLogDir.getAbsolutePath() + File.separator + logFile.getName()));
			}

			File eimLogPath = getEimLogPath();
			Logger.log("EIM log path: " + eimLogPath.getAbsolutePath()); //$NON-NLS-1$
			File eimLogDir = new File(bugReportDirectory.getAbsolutePath() + File.separator + "eim_logs"); //$NON-NLS-1$ )
			FileUtil.copyDirectory(eimLogPath, eimLogDir);

			// Zip the bug report directory
			FileUtil.zipDirectory(bugReportDirectory, bugReportDirectory.getAbsolutePath() + ".zip"); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		return bugReportDirectory.getAbsolutePath() + ".zip"; //$NON-NLS-1$
	}

	private File getInstalledToolsInfoFile() throws IOException
	{
		File installedToolsFile = new File(bugReportDirectory, "installed_tools.txt"); //$NON-NLS-1$

		List<String> arguments = new ArrayList<String>();
		IPath gitPath = new SystemExecutableFinder().find("git"); //$NON-NLS-1$
		Logger.log("GIT path:" + gitPath); //$NON-NLS-1$
		String gitExecutablePath = gitPath.getDevice();
		if (StringUtil.isEmpty(gitExecutablePath))
		{
			gitExecutablePath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.GIT_PATH);
		}
		
		arguments.add(IDFUtil.getIDFPythonEnvPath());
		arguments.add(IDFUtil.getIDFToolsScriptFile().getAbsolutePath());
		arguments.add(IDFConstants.TOOLS_LIST_CMD);
		Logger.log("Executing command: " + String.join(" ", arguments)); //$NON-NLS-1$ //$NON-NLS-2$
		Map<String, String> environment = new HashMap<>(IDFUtil.getSystemEnv());
		Logger.log(environment.toString());
		environment.put("PYTHONUNBUFFERED", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		environment.put("IDF_GITHUB_ASSETS", //$NON-NLS-1$
				Platform.getPreferencesService().getString(IDFCorePlugin.PLUGIN_ID,
						IDFCorePreferenceConstants.IDF_GITHUB_ASSETS,
						IDFCorePreferenceConstants.IDF_GITHUB_ASSETS_DEFAULT_GLOBAL, null));

		environment.put("PIP_EXTRA_INDEX_URL", //$NON-NLS-1$
				Platform.getPreferencesService().getString(IDFCorePlugin.PLUGIN_ID,
						IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL,
						IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL_DEFAULT_GLOBAL, null));

		if (StringUtil.isEmpty(gitExecutablePath))
		{
			Logger.log("Git executable path is empty. Please check GIT_PATH environment variable."); //$NON-NLS-1$
		}
		else
		{
			addGitToEnvironment(environment, gitExecutablePath);
		}
		String output = runCommand(arguments, environment);
		Files.write(installedToolsFile.toPath(), output.getBytes(), StandardOpenOption.CREATE_NEW);
		return installedToolsFile;
	}

	private void addGitToEnvironment(Map<String, String> environment, String gitExecutablePath)
	{
		IPath gitPath = new org.eclipse.core.runtime.Path(gitExecutablePath);
		if (gitPath.toFile().exists())
		{
			String gitDir = gitPath.removeLastSegments(1).toOSString();
			String path1 = environment.get("PATH"); //$NON-NLS-1$
			String path2 = environment.get("Path"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(path1) && !path1.contains(gitDir)) // Git not found on the PATH environment
			{
				path1 = gitDir.concat(";").concat(path1); //$NON-NLS-1$
				environment.put("PATH", path1); //$NON-NLS-1$
			}
			else if (!StringUtil.isEmpty(path2) && !path2.contains(gitDir)) // Git not found on the Path environment
			{
				path2 = gitDir.concat(";").concat(path2); //$NON-NLS-1$
				environment.put("Path", path2); //$NON-NLS-1$
			}
		}
	}

	private File getProductInfoFile() throws IOException
	{
		File productInfoFile = new File(bugReportDirectory, "product_information.txt"); //$NON-NLS-1$

		String pythonExe = IDFUtil.getIDFPythonEnvPath();
		String idfPath = IDFUtil.getIDFPath();
		if (StringUtil.isEmpty(pythonExe) || StringUtil.isEmpty(idfPath))
		{
			Files.write(productInfoFile.toPath(), "IDF_PATH and IDF_PYTHON_ENV_PATH are not found".getBytes(), //$NON-NLS-1$
					StandardOpenOption.CREATE_NEW);
			return productInfoFile;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(System.lineSeparator());
		sb.append(OPERATING_SYSTEM_MSG + System.getProperty("os.name").toLowerCase()); //$NON-NLS-1$
		sb.append(System.lineSeparator());
		sb.append(JAVA_RUNTIME_VERSION_MSG
				+ (Optional.ofNullable(System.getProperty("java.runtime.version")).orElse(NOT_FOUND_MSG))); //$NON-NLS-1$
		sb.append(System.lineSeparator());
		sb.append(ECLIPSE_VERSION_MSG + (Optional.ofNullable(Platform.getBundle("org.eclipse.platform")) //$NON-NLS-1$
				.map(o -> o.getVersion().toString()).orElse(NOT_FOUND_MSG))); // $NON-NLS-1$
		sb.append(System.lineSeparator());
		sb.append(ECLIPSE_CDT_MSG + (Optional.ofNullable(Platform.getBundle("org.eclipse.cdt")) //$NON-NLS-1$
				.map(o -> o.getVersion().toString()).orElse(NOT_FOUND_MSG))); // $NON-NLS-1$
		sb.append(System.lineSeparator());
		sb.append(
				IDF_ECLIPSE_PLUGIN_VERSION_MSG + (Optional.ofNullable(Platform.getBundle("com.espressif.idf.branding")) //$NON-NLS-1$
						.map(o -> o.getVersion().toString()).orElse(NOT_FOUND_MSG))); // $NON-NLS-1$
		sb.append(System.lineSeparator());
		showEspIdfVersion();
		sb.append(PYTHON_IDF_ENV_MSG
				+ (Optional.ofNullable(getPythonExeVersion(IDFUtil.getIDFPythonEnvPath())).orElse(NOT_FOUND_MSG)));
		sb.append(System.lineSeparator());
		
		Files.write(productInfoFile.toPath(), sb.toString().getBytes(), StandardOpenOption.CREATE_NEW);
		return productInfoFile;
	}

	private void showEspIdfVersion()
	{
		if (IDFUtil.getIDFPath() != null && IDFUtil.getIDFPythonEnvPath() != null)
		{
			List<String> commands = new ArrayList<>();
			commands.add(IDFUtil.getIDFPythonEnvPath());
			commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
			commands.add("--version"); //$NON-NLS-1$
			Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();
			Logger.log(runCommand(commands, envMap));
		}
		else
		{
			Logger.log("ESP-IDF version cannot be checked. IDF_PATH or IDF_PYTHON_ENV_PATH  are not set."); //$NON-NLS-1$
		}
	}

	private String runCommand(List<String> arguments, Map<String, String> env)
	{
		String exportCmdOp = ""; //$NON-NLS-1$
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
			}

			// process export command output
			exportCmdOp = status.getMessage();
			Logger.log(exportCmdOp);
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return exportCmdOp;
	}

	private String getPythonExeVersion(String pythonExePath)
	{
		List<String> commands = new ArrayList<>();
		commands.add(pythonExePath);
		commands.add("--version"); //$NON-NLS-1$
		return pythonExePath != null ? runCommand(commands, IDFUtil.getSystemEnv()) : null;
	}
}