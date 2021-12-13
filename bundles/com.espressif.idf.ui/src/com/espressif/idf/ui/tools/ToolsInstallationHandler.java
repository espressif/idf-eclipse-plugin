/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.eclipse.ui.console.MessageConsoleStream;

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
			if (download)
			{
				try
				{
					String nameOfDownloadedFile = downloadTool(key, versionsVO);
					String extractionDir = extractDownloadedFile(nameOfDownloadedFile,
							versionsVO.getVersionOsMap().get(key).getParentName(), versionsVO.getName());
					// TO DO calling updatePaths

				}
				catch (Exception e)
				{
					Logger.log(e);
				}
			}
		}
	}

	private void updatePaths(String toolPath, String toolName, List<String> exportPaths)
	{
		// TO DO
	}

	private String extractDownloadedFile(String downloadedName, String toolFolderName, String extractionName)
			throws Exception
	{
		File toolsFolder = new File(ToolsUtility.ESPRESSIF_HOME_TOOLS_DIR.concat(PATH_SPLITOR).concat(toolFolderName));
		if (!toolsFolder.exists())
		{
			toolsFolder.mkdir();
		}

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

		return extractionName;
	}

	private String downloadTool(String key, VersionsVO versionsVO) throws Exception
	{
		FileDownloader fileDownloader = new FileDownloader();
		String url = versionsVO.getVersionOsMap().get(key).getUrl();
		fileDownloader.url = new URL(url);
		String[] split = fileDownloader.url.getPath().split(PATH_SPLITOR);
		fileDownloader.dirToDownloadTo = ToolsUtility.ESPRESSIF_HOME_DIR;
		fileDownloader.name = split[split.length - 1];
		fileDownloader.totalSize = versionsVO.getVersionOsMap().get(key).getSize();

		Thread downloadingThread = new Thread(fileDownloader);
		downloadingThread.start();

		while (fileDownloader.downloading)
		{
			console.println(Messages.DownloadFileText.concat(url));
			console.println(Messages.DownloadProgressText + fileDownloader.completedSize + PATH_SPLITOR
					+ fileDownloader.totalSize);
			Thread.sleep(500);
			idfConsole.clearConsole();
		}

		return fileDownloader.name;
	}

	private class FileDownloader implements Runnable
	{
		private URL url;
		private String dirToDownloadTo;
		private String name;
		private boolean downloading;
		private double totalSize;
		private double completedSize;

		@Override
		public void run()
		{
			try
			{
				downloading = true;
				HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
				BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
				FileOutputStream fos = new FileOutputStream(dirToDownloadTo.concat(PATH_SPLITOR).concat(name));
				BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
				byte[] data = new byte[1024];
				int x = 0;
				while ((x = in.read(data, 0, 1024)) >= 0)
				{
					completedSize += x;
					bout.write(data, 0, x);
				}

				bout.close();
				in.close();
			}
			catch (Exception e)
			{
				Logger.log(e);
				downloading = false;
			}

			downloading = false;
		}

	}
}
