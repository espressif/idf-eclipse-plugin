/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.espressif.idf.core.resources.OpenDialogListenerSupport;

public class ParitionSizeHandler
{
	private IProject project;
	private ConsoleOutputStream infoStream;
	private IConsole console;

	public ParitionSizeHandler(IProject project, ConsoleOutputStream infoStream, IConsole console)
	{
		this.project = project;
		this.infoStream = infoStream;
		this.console = console;
	}

	// checking the size consists of the idf_size.py command and checking the remaining size from the partition table
	public void startCheckingSize() throws IOException, CoreException
	{
		if (IDFUtil.getMapFilePath(project) != null)
		{
			startIdfSizeProcess();
		}
		IPath binPath = IDFUtil.getBinFilePath(project);
		if (binPath != null)
		{
			checkRemainingSize(binPath);
		}
	}

	private String getPartitionTable() throws IOException, CoreException
	{
		List<String> commands;
		commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFPath() + File.separator + "components" + File.separator + "partition_table" //$NON-NLS-1$ //$NON-NLS-2$
				+ File.separator + "gen_esp32part.py"); //$NON-NLS-1$
		commands.add(IDFUtil.getBuildDir(project) + File.separator + "partition_table" + File.separator //$NON-NLS-1$
				+ "partition-table.bin"); //$NON-NLS-1$

		Process process = startProcess(commands);
		String partitionTableContent = new String(process.getInputStream().readAllBytes());
		return partitionTableContent;
	}

	private Process startProcess(List<String> commands) throws IOException
	{
		infoStream.write(String.join(" ", commands) + '\n'); //$NON-NLS-1$ //$NON-NLS-2$
		Path workingDir = (Path) project.getLocation();
		ProcessBuilder processBuilder = new ProcessBuilder(commands).directory(workingDir.toFile());
		Process process = processBuilder.start();
		return process;
	}

	private void startIdfSizeProcess() throws IOException, CoreException
	{
		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFSizeScriptFile().getAbsolutePath());
		commands.add(IDFUtil.getMapFilePath(project).toString());

		Process process = startProcess(commands);
		if (process != null)
		{
			console.getOutputStream().write(process.getInputStream().readAllBytes());
		}

	}

	private void checkRemainingSize(IPath path) throws IOException, CoreException
	{
		String partitionTableContent = getPartitionTable();
		long imageSize = path.toFile().length();
		String[] lines = partitionTableContent.split("\n"); //$NON-NLS-1$
		for (String line : lines)
		{
			if (!line.contains("app")) //$NON-NLS-1$
			{
				continue;
			}
			String[] columns = line.split(","); //$NON-NLS-1$
			double maxSize = DataSizeUtil.parseSize(columns[4]);
			double remainSize = (maxSize - imageSize) / (maxSize);
			if (remainSize < 0.3)
			{
				OpenDialogListenerSupport.getSupport().firePropertyChange(null, maxSize, remainSize * maxSize);
				break;
			}
		}
	}

}
