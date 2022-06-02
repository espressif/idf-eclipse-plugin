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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.tools.vo.ToolsVO;
import com.espressif.idf.ui.tools.vo.VersionsVO;
import com.espressif.idf.ui.tools.wizard.pages.ManageToolsInstallationWizardPage;

/**
 * Class to carry out download and install of tools
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsInstallationHandler extends Thread
{
	private static final String PATH_SPLITOR = "/"; //$NON-NLS-1$
	private static final String GZ_EXT = "gz"; //$NON-NLS-1$
	private static final String ZIP_EXT = "zip"; //$NON-NLS-1$
	private Queue<String> logQueue;
	private ManageToolsInstallationWizardPage manageToolsInstallationWizardPage;
	private ExecutorService executorService;
	private boolean cancelled;
	private Future<Boolean> threadResponse;
	private boolean completed;

	public ToolsInstallationHandler(Queue<String> logQueue,
			ManageToolsInstallationWizardPage manageToolsInstallationWizardPage)
	{
		this.logQueue = logQueue;
		this.manageToolsInstallationWizardPage = manageToolsInstallationWizardPage;
		executorService = Executors.newSingleThreadExecutor();
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep(100);
			while (!cancelled && !completed)
			{
				Thread.sleep(200);
			}

			if (cancelled) // cancel initiated by user, we need to take care of the submitted threads
			{
				initiateCancellation();
				logQueue.clear();
				return;
			}
			
			if (!threadResponse.get().booleanValue())
			{
				logQueue.add("Some errors have occurred in operation");
			}
			else 
			{
				logQueue.add("Operations completed!");	
			}
			
			setControlsEnabled(true);
			showProgressBarAndCancelBtn(false);
			refreshTree();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	public void installTools(Map<ToolsVO, List<VersionsVO>> selectedItems)
	{
		cancelled = false;
		completed = false;
		setControlsEnabled(false);
		showProgressBarAndCancelBtn(true);
		executorService = Executors.newSingleThreadExecutor();
		InstallToolsThread installToolsThread = new InstallToolsThread(selectedItems);
		threadResponse = executorService.submit(installToolsThread);
	}

	public void deleteTools(Map<ToolsVO, List<VersionsVO>> selectedItems)
	{
		cancelled = false;
		completed = false;
		setControlsEnabled(false);
		showProgressBarAndCancelBtn(true);
		DeleteToolsThread deleteToolsThread = new DeleteToolsThread(selectedItems);
		threadResponse = executorService.submit(deleteToolsThread);
	}

	public boolean isCancelled()
	{
		return cancelled;
	}

	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
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
			if (cancelled)
			{
				logQueue.add(Messages.OperationCancelledByUser);
				return;
			}
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

	private void setControlsEnabled(boolean enabled)
	{
		manageToolsInstallationWizardPage.getShell().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				manageToolsInstallationWizardPage.disableControls(enabled);
			}
		});
	}

	private void refreshTree()
	{
		manageToolsInstallationWizardPage.getShell().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					manageToolsInstallationWizardPage.refreshTree();
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
			}
		});
	}
	
	private void showProgressBarAndCancelBtn(boolean show)
	{
		manageToolsInstallationWizardPage.getShell().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				manageToolsInstallationWizardPage.getProgressBar().setVisible(show);
				manageToolsInstallationWizardPage.visibleCancelBtn(show);
			}
		});
	}

	private void setProgressBarMaximum(int max)
	{
		manageToolsInstallationWizardPage.getShell().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				manageToolsInstallationWizardPage.getProgressBar().setMaximum(max);
			}
		});
	}

	private void updateProgressBar(int selection)
	{
		manageToolsInstallationWizardPage.getShell().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				manageToolsInstallationWizardPage.getProgressBar().setSelection(selection);
			}
		});
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

	private void installTool(VersionsVO versionsVO, String toolName, List<String> exportPaths)
	{
		if (cancelled)
		{
			logQueue.add(Messages.OperationCancelledByUser);
			return;	
		}
			
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
					if (cancelled)
					{
						logQueue.add(Messages.OperationCancelledByUser);
						return;	
					}
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
		StringBuilder exportPathBuilder = new StringBuilder();
		exportPathBuilder.append(toolPath);
		removeExistingToolPath(toolName);
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		for (String exportPath : exportPaths)
		{
			exportPathBuilder.append(exportPath);
			exportPathBuilder.append(PATH_SPLITOR);
		}

		Path pathToExport = Paths.get(exportPathBuilder.toString()); // for correcting the path error in windows

		logQueue.add(Messages.UpdateToolPathMessage.concat(pathToExport.toAbsolutePath().toString()));
		idfEnvironmentVariables.prependEnvVariableValue(IDFEnvironmentVariables.PATH, pathToExport.toString());
		logQueue.add(Messages.SystemPathMessage.concat(idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH)));
		try
		{
			Thread.sleep(50); // wait for the variable to persist
		}
		catch (InterruptedException e)
		{
			Logger.log(e);
		}
	}
	
	private void removeExistingToolPath(String toolName)
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		String pathValue = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
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
			setProgressBarMaximum(httpConnection.getContentLength());
			BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
			FileOutputStream fos = new FileOutputStream(dirToDownloadTo.concat(PATH_SPLITOR).concat(name));
			BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
			byte[] data = new byte[4096];
			int x = 0;
			logQueue.add(Messages.DownloadFileText.concat(versionsVO.getVersionOsMap().get(key).getUrl()));
			while ((x = in.read(data, 0, 4096)) >= 0 && !cancelled)
			{
				completedSize += x;
				bout.write(data, 0, x);
				updateProgressBar((int) completedSize);
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

	private void initiateCancellation()
	{
		setControlsEnabled(true);
		showProgressBarAndCancelBtn(false);
	}

	private class InstallToolsThread implements Callable<Boolean>
	{
		private Map<ToolsVO, List<VersionsVO>> selectedItems;

		public InstallToolsThread(Map<ToolsVO, List<VersionsVO>> selectedItems)
		{
			this.selectedItems = selectedItems;
		}

		@Override
		public Boolean call() throws Exception
		{
			for (ToolsVO toolsVo : selectedItems.keySet())
			{
				if (Thread.interrupted())
					return Boolean.FALSE;
				for (VersionsVO versionsVO : selectedItems.get(toolsVo))
				{
					installTool(versionsVO, toolsVo.getName(), toolsVo.getExportPaths());
				}
			}
			
			completed = true;
			return Boolean.TRUE;
		}
	}

	private class DeleteToolsThread implements Callable<Boolean>
	{
		private Map<ToolsVO, List<VersionsVO>> selectedItems;

		private DeleteToolsThread(Map<ToolsVO, List<VersionsVO>> selectedItems)
		{
			this.selectedItems = selectedItems;
		}

		@Override
		public Boolean call() throws Exception
		{
			setProgressBarMaximum(selectedItems.keySet().size());
			int progress = 0;
			
			for (ToolsVO toolsVO : selectedItems.keySet())
			{
				if (Thread.interrupted())
				{
					logQueue.add(Messages.OperationCancelledByUser);
					return Boolean.FALSE;
				}
				
				for (VersionsVO versionsVO : selectedItems.get(toolsVO))
				{
					deleteTool(versionsVO, toolsVO.getName());
				}
				
				updateProgressBar(++progress);
			}
			completed = true;
			return Boolean.TRUE;
		}
	}
}
