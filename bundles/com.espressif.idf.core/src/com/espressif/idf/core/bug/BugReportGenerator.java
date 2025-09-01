/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD.
 * All rights reserved. Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.bug;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.FileUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * This class generates a bug report zip file containing:
 * 1. Installed tools information
 * 2. Product information
 * 3. Basic system information (OS, Arch, Memory)
 * 4. IDE metadata logs
 * 5. eim logs (if available)
 * 
 * The generated zip file is named with a timestamp and stored in the workspace directory.
 * @author Ali Azam Rana
 *
 */
public class BugReportGenerator
{
	private static final String UNKNOWN = "Unknown"; //$NON-NLS-1$
	private static final String BUG_REPORT_DIRECTORY_PREFIX = "bug_report_"; //$NON-NLS-1$
	private String installedToolsCommandOutput;
	private String productInformationCommandOutput;
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

	public BugReportGenerator(String installedToolsCommandOutput, String productInformationCommandOutput)
	{
		this.installedToolsCommandOutput = installedToolsCommandOutput;
		this.productInformationCommandOutput = productInformationCommandOutput;
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

	private File getIdeMetadataLogsFile()
	{
		File metadataDir = getWorkspaceDirectory().resolve(".metadata").toFile(); //$NON-NLS-1$
		return metadataDir;
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
			File installedToolsFile = FileUtil.createFileWithContentsInDirectory("installed_tools.txt", //$NON-NLS-1$
					installedToolsCommandOutput, bugReportDirectory.getAbsolutePath());
			File productInfoFile = FileUtil.createFileWithContentsInDirectory("product_information.txt", //$NON-NLS-1$
					productInformationCommandOutput, bugReportDirectory.getAbsolutePath());
			File basicSysInfoFile = createBasicSystemInfoFile();

			List<File> filesToZip = new LinkedList<>();
			filesToZip.add(installedToolsFile);
			filesToZip.add(productInfoFile);
			filesToZip.add(basicSysInfoFile);

			File metadataLogsFile = getIdeMetadataLogsFile();
			File ideLogDir = new File(bugReportDirectory.getAbsolutePath() + File.separator + "ide_metadata_logs"); //$NON-NLS-1$ )
			if (!ideLogDir.exists())
			{
				ideLogDir.mkdirs();
			}

			FileUtil.copyDirectory(metadataLogsFile, ideLogDir);

			File eimLogPath = getEimLogPath();
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
}