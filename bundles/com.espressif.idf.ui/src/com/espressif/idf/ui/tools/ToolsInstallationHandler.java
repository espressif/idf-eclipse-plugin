/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.tools.vo.ToolsVO;
import com.espressif.idf.ui.tools.vo.VersionsVO;

/**
 * Class to carry out download and install of tools
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsInstallationHandler
{
	private static final String TOOL_INSTALLATION_JOB = "Tool installation job for "; //$NON-NLS-1$
	private static final String TOOL_DELETE_JOB = "Tool delete job for "; //$NON-NLS-1$
	private static final String PATH_SPLITOR = "/"; //$NON-NLS-1$
	private static final String GZ_EXT = "gz"; //$NON-NLS-1$
	private static final String ZIP_EXT = "zip"; //$NON-NLS-1$
	private Map<ToolsVO, List<VersionsVO>> selectedItems;
	private IDFConsole idfConsole;
	private MessageConsoleStream console;
	private Queue<Job> installToolsJobs;
	private Queue<Job> deleteToolsJobs;
	private Queue<String> logQueue;

	public ToolsInstallationHandler(Map<ToolsVO, List<VersionsVO>> selectedItems, Queue<String> logQueue)
	{
		this.selectedItems = selectedItems;
		this.idfConsole = new IDFConsole();
		this.console = idfConsole.getConsoleStream();
		installToolsJobs = new ConcurrentLinkedQueue<Job>();
		deleteToolsJobs = new ConcurrentLinkedQueue<Job>();
		this.logQueue = logQueue;
	}

	public void deleteTools()
	{
		for (ToolsVO toolsVO : selectedItems.keySet())
		{
			for (VersionsVO versionsVO : selectedItems.get(toolsVO))
			{
				Job job = new Job(TOOL_DELETE_JOB.concat(toolsVO.getName()))
				{
					@Override
					protected IStatus run(IProgressMonitor monitor)
					{
						deleteTool(versionsVO, toolsVO.getName());
						return Status.OK_STATUS;
					}
				};

				job.schedule();
				deleteToolsJobs.add(job);
			}
		}
	}

	private void deleteTool(VersionsVO versionsVO, String toolName)
	{
		for (String key : versionsVO.getVersionOsMap().keySet())
		{
			if (!versionsVO.getVersionOsMap().get(key).isSelected())
			{
				continue;
			}

			removeToolFromPath(toolName);
			removeToolDirectory(toolName.concat(PATH_SPLITOR).concat(versionsVO.getName()));
		}
	}

	private void removeToolDirectory(String toolName)
	{
		try
		{
			logQueue.add("Removing Directory for Tool: ".concat(toolName)); //$NON-NLS-1$
			ToolsUtility.removeToolDirectory(toolName);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	private void removeToolFromPath(String toolName)
	{
		logQueue.add(Messages.UpdatingPathMessage);
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		String pathValue = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
		StringBuilder updatedPath = new StringBuilder();
		String[] splittedPaths = pathValue.split(File.pathSeparator);
		int i = 0;
		for (String path : splittedPaths)
		{
			i++;
			if (path.contains(toolName))
			{
				logQueue.add(Messages.RemovedPathMessage.concat(path));
				continue;
			}
			else
			{
				updatedPath.append(path);
				if (i < splittedPaths.length)
				{
					updatedPath.append(File.pathSeparator);
				}
			}
		}

		logQueue.add(Messages.SystemPathMessage.concat(updatedPath.toString()));
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.PATH, updatedPath.toString());
	}

	public void installTools()
	{
		for (ToolsVO toolsVo : selectedItems.keySet())
		{
			for (VersionsVO versionsVO : selectedItems.get(toolsVo))
			{
				Job job = new Job(TOOL_INSTALLATION_JOB.concat(toolsVo.getName()))
				{
					@Override
					protected IStatus run(IProgressMonitor monitor)
					{
						installTool(versionsVO, toolsVo.getName(), toolsVo.getExportPaths());
						return Status.OK_STATUS;
					}
				};

				job.schedule();
				installToolsJobs.add(job);
			}
		}
	}

	private void installTool(VersionsVO versionsVO, String toolName, List<String> exportPaths)
	{
		for (String key : versionsVO.getVersionOsMap().keySet())
		{
			if (!versionsVO.getVersionOsMap().get(key).isSelected())
			{
				continue;
			}

			boolean download = !ToolsUtility.isToolInstalled(toolName, versionsVO.getName());
			download = true;
			logQueue.add(Messages.InstallingToolMessage.concat(toolName));
			if (!ToolsUtility.isToolInstalled(toolName, versionsVO.getName()) && !versionsVO.isAvailable())
			{
				try
				{
					String nameOfDownloadedFile = downloadTool(key, versionsVO);
					String extractionDir = extractDownloadedFile(nameOfDownloadedFile, toolName, versionsVO.getName());
					updatePaths(extractionDir, toolName, exportPaths);
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
			}
			else
			{
				updatePaths(versionsVO.getAvailablePath(), toolName, exportPaths);
			}
		}
	}

	private void updatePaths(String toolPath, String toolName, List<String> exportPaths)
	{
		logQueue.add(Messages.UpdatingPathMessage);
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		String pathValue = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
		StringBuilder exportPathBuilder = new StringBuilder();
		exportPathBuilder.append(toolPath);
		StringBuilder updatedPath = new StringBuilder();
		if (!StringUtil.isEmpty(pathValue))
		{
			String[] splittedPaths = pathValue.split(File.pathSeparator);
			int i = 0;

			for (String path : splittedPaths)
			{
				i++;
				if (path.contains(toolName))
				{
					logQueue.add(Messages.PreviousToolMessage.concat(path));
				}
				else
				{
					updatedPath.append(path);
					if (i < splittedPaths.length || splittedPaths.length == 1)
					{
						updatedPath.append(File.pathSeparator);
					}
				}
			}
		}

		for (String exportPath : exportPaths)
		{
			exportPathBuilder.append(exportPath);
			exportPathBuilder.append(PATH_SPLITOR);
		}
		if (updatedPath.toString().split(File.pathSeparator).length > 1)
		{
			updatedPath.append(File.pathSeparator);
		}

		Path pathToExport = Paths.get(exportPathBuilder.toString()); // for correcting the path error in windows

		logQueue.add(Messages.UpdateToolPathMessage.concat(pathToExport.toAbsolutePath().toString()));

		updatedPath.append(pathToExport.toAbsolutePath().toString());

		logQueue.add(Messages.SystemPathMessage.concat(updatedPath.toString()));
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.PATH, updatedPath.toString());
	}

	private String extractDownloadedFile(String downloadedName, String toolFolderName, String extractionName)
			throws Exception
	{
		File toolsFolder = new File(ToolsUtility.ESPRESSIF_HOME_TOOLS_DIR.concat(PATH_SPLITOR).concat(toolFolderName));
		if (!toolsFolder.exists())
		{
			toolsFolder.mkdir();
		}

		logQueue.add(Messages.ExtractionTextMessage.concat(downloadedName));

		extractionName = ToolsUtility.ESPRESSIF_HOME_TOOLS_DIR.concat(PATH_SPLITOR).concat(toolFolderName)
				.concat(PATH_SPLITOR).concat(extractionName).concat(PATH_SPLITOR);
		String extension = ToolsUtility.getFileExtension(downloadedName);
		if (extension.equals(ZIP_EXT))
		{
			ToolsUtility.extractZip(ToolsUtility.ESPRESSIF_HOME_DIR.concat(PATH_SPLITOR).concat(downloadedName),
					extractionName);
		}
		else if (extension.equals(GZ_EXT))
		{
			ToolsUtility.extractTarGz(ToolsUtility.ESPRESSIF_HOME_DIR.concat(PATH_SPLITOR).concat(downloadedName),
					extractionName);
		}

		logQueue.add(Messages.ExtractionCompletedMessage);
		return extractionName;
	}

	private String downloadTool(String key, VersionsVO versionsVO) throws Exception
	{
		String[] split = versionsVO.getVersionOsMap().get(key).getUrl().split(PATH_SPLITOR);
		URL url = new URL(versionsVO.getVersionOsMap().get(key).getUrl());
		String dirToDownloadTo = ToolsUtility.ESPRESSIF_HOME_DIR;
		String name = split[split.length - 1];
		double totalSize = versionsVO.getVersionOsMap().get(key).getSize();
		double completedSize = 0;
		try
		{
			HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
			BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
			FileOutputStream fos = new FileOutputStream(dirToDownloadTo.concat(PATH_SPLITOR).concat(name));
			BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
			byte[] data = new byte[4096];
			int x = 0;
			logQueue.add(Messages.DownloadFileText.concat(versionsVO.getVersionOsMap().get(key).getUrl()));
			while ((x = in.read(data, 0, 4096)) >= 0)
			{
				completedSize += x;
				bout.write(data, 0, x);
				logQueue.add(Messages.DownloadProgressText + ToolsUtility.getReadableSizeMB(completedSize)
						+ PATH_SPLITOR + ToolsUtility.getReadableSizeMB(totalSize));
			}

			bout.close();
			in.close();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}

		return name;
	}

	public Collection<Job> getRunningJobs()
	{
		return Collections.unmodifiableCollection(installToolsJobs);
	}

	public Collection<Job> getDeleteToolsJobs()
	{
		return Collections.unmodifiableCollection(deleteToolsJobs);
	}
}
