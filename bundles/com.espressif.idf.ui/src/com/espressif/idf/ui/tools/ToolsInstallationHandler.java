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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.tools.vo.VersionsVO;

/**
 * Class to carry out download and install of tools
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsInstallationHandler
{
	private static final String TOOL_INSTALLATION_JOB = "ToolInstallationJobFor"; //$NON-NLS-1$
	private static final String PATH_SPLITOR = "/"; //$NON-NLS-1$
	private static final String GZ_EXT = "gz"; //$NON-NLS-1$
	private static final String ZIP_EXT = "zip"; //$NON-NLS-1$
	private List<VersionsVO> versionsVOs;
	private IDFConsole idfConsole;
	private MessageConsoleStream console;

	public ToolsInstallationHandler(List<VersionsVO> versionsVOs)
	{
		this.versionsVOs = versionsVOs;
		this.idfConsole = new IDFConsole();
		this.console = idfConsole.getConsoleStream();
	}

	public void deleteTools()
	{
		for (VersionsVO versionsVO : versionsVOs)
		{
			deleteTool(versionsVO);
		}
	}

	private void deleteTool(VersionsVO versionsVO)
	{
		for (String key : versionsVO.getVersionOsMap().keySet())
		{
			if (!versionsVO.getVersionOsMap().get(key).isSelected())
			{
				continue;
			}

			removeToolFromPath(versionsVO.getVersionOsMap().get(key).getParentName());
			removeToolDirectory(versionsVO.getVersionOsMap().get(key).getParentName().concat(PATH_SPLITOR).concat(versionsVO.getName()));
		}
	}

	private void removeToolDirectory(String toolName)
	{
		try
		{
			console.println("Removing Directory for Tool: ".concat(toolName));
			ToolsUtility.removeToolDirectory(toolName);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	private void removeToolFromPath(String toolName)
	{
		console.println(Messages.UpdatingPathMessage);
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		String pathValue = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
		StringBuilder updatedPath = new StringBuilder();
		String[] splittedPaths = pathValue.split(File.pathSeparator);
		int i = splittedPaths.length;
		for (String path : splittedPaths)
		{
			i++;
			if (path.contains(toolName))
			{
				console.println(Messages.RemovedPathMessage.concat(path));
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

		console.println(Messages.SystemPathMessage.concat(updatedPath.toString()));
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.PATH, updatedPath.toString());
	}

	public void installTools()
	{
		for (VersionsVO versionsVO : versionsVOs)
		{
			installTool(versionsVO);
		}
	}

	private void installTool(VersionsVO versionsVO)
	{
		for (String key : versionsVO.getVersionOsMap().keySet())
		{
			if (!versionsVO.getVersionOsMap().get(key).isSelected())
			{
				continue;
			}

			boolean download = !ToolsUtility.isToolInstalled(versionsVO.getVersionOsMap().get(key).getParentName(),
					versionsVO.getName());
			download = true;
			console.println(
					Messages.InstallingToolMessage.concat(versionsVO.getVersionOsMap().get(key).getParentName()));
			if (download)
			{
				Job job = new Job(TOOL_INSTALLATION_JOB.concat(versionsVO.getVersionOsMap().get(key).getParentName()))
				{
					@Override
					protected IStatus run(IProgressMonitor monitor)
					{
						try
						{
							String nameOfDownloadedFile = downloadTool(key, versionsVO);
							String extractionDir = extractDownloadedFile(nameOfDownloadedFile,
									versionsVO.getVersionOsMap().get(key).getParentName(), versionsVO.getName());
							updatePaths(extractionDir, versionsVO.getVersionOsMap().get(key).getParentName(),
									versionsVO.getVersionOsMap().get(key).getExportPaths());
						}
						catch (Exception e)
						{
							Logger.log(e);
							return Status.error(e.getMessage());
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		}
	}

	private void updatePaths(String toolPath, String toolName, List<String> exportPaths)
	{
		console.println(Messages.UpdatingPathMessage);
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		String pathValue = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
		StringBuilder updatedPath = new StringBuilder();
		String[] splittedPaths = pathValue.split(File.pathSeparator);
		int i = 0;
		StringBuilder exportPathBuilder = new StringBuilder();
		exportPathBuilder.append(toolPath);
		for (String path : splittedPaths)
		{
			i++;
			if (path.contains(toolName))
			{
				console.println(Messages.PreviousToolMessage.concat(path));
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

		for (String exportPath : exportPaths)
		{
			exportPathBuilder.append(exportPath);
			exportPathBuilder.append(PATH_SPLITOR);
		}

		Path pathToExport = Paths.get(exportPathBuilder.toString()); // for correcting the path error in windows

		console.println(Messages.UpdateToolPathMessage.concat(pathToExport.toAbsolutePath().toString()));
		updatedPath.append(File.pathSeparator);
		updatedPath.append(pathToExport.toAbsolutePath().toString());

		console.println(Messages.SystemPathMessage.concat(updatedPath.toString()));
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

		console.println(Messages.ExtractionTextMessage.concat(downloadedName));

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

		console.println(Messages.ExtractionCompletedMessage);
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
			console.println(Messages.DownloadFileText.concat(versionsVO.getVersionOsMap().get(key).getUrl()));
			while ((x = in.read(data, 0, 4096)) >= 0)
			{
				completedSize += x;
				bout.write(data, 0, x);
				console.println(Messages.DownloadProgressText + ToolsUtility.getReadableSizeMB(completedSize)
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
}
