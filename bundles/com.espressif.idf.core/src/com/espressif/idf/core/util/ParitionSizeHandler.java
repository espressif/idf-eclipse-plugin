/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.espressif.idf.core.IDFConstants;
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
		if (getMapFilePath(project) != null)
		{
			startIdfSizeProcess();
			checkRemainingSize();			
		}
	}
	
	private String getPartitionTable() throws IOException
	{
		List<String> commands;
		commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFPath() + "/components/partition_table/gen_esp32part.py"); //$NON-NLS-1$
		commands.add(project.getLocation() + "/build/partition_table/partition-table.bin"); //$NON-NLS-1$

		Process process = startProcess(commands);
		String partitionTableContent = new String(process.getInputStream().readAllBytes());
		return partitionTableContent;
	}

	private Process startProcess(List<String> commands)
			throws IOException
	{
		infoStream.write(String.join(" ", commands) + '\n'); //$NON-NLS-1$
		org.eclipse.core.runtime.Path workingDir = (org.eclipse.core.runtime.Path) project.getLocation();
		ProcessBuilder processBuilder = new ProcessBuilder(commands).directory(workingDir.toFile());
		Process process = processBuilder.start();
		return process;
	}
	
	private void startIdfSizeProcess()
			throws IOException, CoreException
	{
		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFSizeScriptFile().getAbsolutePath());
		commands.add(getMapFilePath(project).toString());

		Process process = startProcess(commands);
		if (process != null)
		{
			console.getOutputStream().write(process.getInputStream().readAllBytes());
		}

	}
	
	private IPath getMapFilePath(IProject project)
	{
		GenericJsonReader jsonReader = new GenericJsonReader(project,
				IDFConstants.BUILD_FOLDER + File.separator + "project_description.json"); //$NON-NLS-1$
		String value = jsonReader.getValue("app_elf"); //$NON-NLS-1$
		if (!StringUtil.isEmpty(value))
		{
			value = value.replace(".elf", ".map"); // Assuming .elf and .map files have the //$NON-NLS-1$ //$NON-NLS-2$
													// same file name
			return project.getFile(new org.eclipse.core.runtime.Path("build").append(value)).getLocation(); //$NON-NLS-1$
		}
		return null;
	}
	
	private void checkRemainingSize()
			throws IOException, CoreException
	{
		String partitionTableContent = getPartitionTable();
		Path path = Paths.get(project.getLocation() + File.separator + IDFConstants.BUILD_FOLDER + File.separator
				+ project.getName().replace(" ", "_") + ".bin"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		long imageSize = Files.size(path);

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
