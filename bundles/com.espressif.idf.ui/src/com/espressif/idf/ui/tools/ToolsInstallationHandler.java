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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.build.ESPToolChainManager;
import com.espressif.idf.core.build.ESPToolChainProvider;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.PyWinRegistryReader;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.vo.ToolsVO;
import com.espressif.idf.ui.tools.vo.VersionsVO;
import com.espressif.idf.ui.tools.wizard.IToolsInstallationWizardConstants;
import com.espressif.idf.ui.tools.wizard.pages.ManageToolsInstallationWizardPage;

/**
 * Class to carry out download and install of tools
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
public class ToolsInstallationHandler extends Thread
{
	public static final int DELETING_TOOLS = 0;
	public static final int INSTALLING_TOOLS = 1;

	private static final String PATH_SPLITOR = "/"; //$NON-NLS-1$
	private static final String GZ_EXT = "gz"; //$NON-NLS-1$
	private static final String ZIP_EXT = "zip"; //$NON-NLS-1$
	private static final String SHA256 = "SHA-256"; //$NON-NLS-1$
	private Queue<String> logQueue;
	private ManageToolsInstallationWizardPage manageToolsInstallationWizardPage;
	private ExecutorService executorService;
	private boolean cancelled;
	private Future<Boolean> threadResponse;
	private IDFEnvironmentVariables idfEnvironmentVariables;
	private Preferences scopedPreferenceStore;
	private int currentOperation;

	public ToolsInstallationHandler(Queue<String> logQueue,
			ManageToolsInstallationWizardPage manageToolsInstallationWizardPage,
			IDFEnvironmentVariables idfEnvironmentVariables)
	{
		this.logQueue = logQueue;
		this.manageToolsInstallationWizardPage = manageToolsInstallationWizardPage;
		executorService = Executors.newSingleThreadExecutor();
		this.idfEnvironmentVariables = idfEnvironmentVariables;
		scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep(100);
			while (threadResponse != null && !threadResponse.isDone())
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

			try
			{
				scopedPreferenceStore.putBoolean(IToolsInstallationWizardConstants.INSTALL_TOOLS_FLAG,
						threadResponse.get().booleanValue());
				manageToolsInstallationWizardPage.getShell().getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							manageToolsInstallationWizardPage.setPageComplete(threadResponse.get().booleanValue());
							if (currentOperation == DELETING_TOOLS)
							{
								manageToolsInstallationWizardPage.afterDeleteToolMessage();
							}
							else
							{
								manageToolsInstallationWizardPage.restoreFinishButton();
							}

						}
						catch (Exception e)
						{
							Logger.log(e);
						}
					}
				});
			}
			catch (Exception e)
			{
				Logger.log(e);
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

	/**
	 * Pass the arguments and select the operation to perform on the passed tools valid options INSTALLING_TOOLS and
	 * DELETING_TOOLS constants in the class. The method just tells the class of the operation to perfrom start on the
	 * thread must be called explicitly
	 * 
	 * @param selectedItems      the selected items
	 * @param forceDownload      force download flag
	 * @param operationToPerform operations to perfrom
	 * @throws Exception
	 */
	public void operationToPerform(Map<ToolsVO, List<VersionsVO>> selectedItems, boolean forceDownload,
			int operationToPerform) throws Exception
	{
		this.currentOperation = operationToPerform;
		cancelled = false;
		setControlsEnabled(false);
		showProgressBarAndCancelBtn(true);
		executorService = Executors.newSingleThreadExecutor();
		Callable<Boolean> operationsThread = operationToPerform == INSTALLING_TOOLS
				? new InstallToolsThread(selectedItems, idfEnvironmentVariables, forceDownload)
				: operationToPerform == DELETING_TOOLS ? new DeleteToolsThread(selectedItems) : null;
		if (operationsThread == null)
		{
			throw new Exception("Invalid operation passed"); //$NON-NLS-1$
		}

		threadResponse = executorService.submit(operationsThread);
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

	private void installTool(ToolsVO toolsVO, VersionsVO versionsVO, boolean forceDownload)
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

			logQueue.add(Messages.InstallingToolMessage.concat(toolsVO.getName()));
			if (!ToolsUtility.isToolInstalled(toolsVO.getName(), versionsVO.getName()) && !versionsVO.isAvailable())
			{
				try
				{
					String nameOfDownloadedFile = downloadTool(key, versionsVO, forceDownload);
					if (cancelled)
					{
						logQueue.add(Messages.OperationCancelledByUser);
						return;
					}
					String extractionDir = extractDownloadedFile(nameOfDownloadedFile, toolsVO.getName(),
							versionsVO.getName());
					updatePaths(extractionDir, toolsVO.getName(), toolsVO.getExportPaths());
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
			}
			else
			{
				updatePaths(versionsVO.getAvailablePath(), toolsVO.getName(), toolsVO.getExportPaths());
			}
		}
	}

	private void updatePaths(String toolPath, String toolName, List<String> exportPaths)
	{
		if (StringUtil.isEmpty(toolPath))
			return;

		logQueue.add(Messages.UpdatingPathMessage);
		StringBuilder exportPathBuilder = new StringBuilder();
		exportPathBuilder.append(toolPath);
		removeExistingToolPath(toolName);
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		if (exportPaths != null && exportPaths.size() > 0)
		{
			for (String exportPath : exportPaths)
			{
				exportPathBuilder.append(exportPath);
				exportPathBuilder.append(PATH_SPLITOR);
			}
		}
		
		Path pathToExport = Paths.get(exportPathBuilder.toString()); // for correcting the path error in windows
		String currentPath = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
		StringBuilder finalPathToExport = new StringBuilder(currentPath);
		finalPathToExport.append(EnvironmentVariableManager.getDefault().getDefaultDelimiter());
		finalPathToExport.append(pathToExport.toAbsolutePath().toString());

		logQueue.add(Messages.UpdateToolPathMessage.concat(pathToExport.toAbsolutePath().toString()));
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.PATH, finalPathToExport.toString());
		logQueue.add(
				Messages.SystemPathMessage.concat(idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH)));
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

	private String downloadTool(String key, VersionsVO versionsVO, boolean forceDownload) throws Exception
	{
		String[] split = versionsVO.getVersionOsMap().get(key).getUrl().split(PATH_SPLITOR);
		String sha256 = versionsVO.getVersionOsMap().get(key).getSha256();
		URL url = new URL(versionsVO.getVersionOsMap().get(key).getUrl());
		String dirToDownloadTo = ToolsUtility.ESPRESSIF_HOME_DIR;
		String name = split[split.length - 1];
		File file = new File(dirToDownloadTo.concat(PATH_SPLITOR).concat(name));
		if (file.exists() && !forceDownload)
		{
			MessageDigest shaDigest = MessageDigest.getInstance(SHA256);
			if (sha256.equalsIgnoreCase(ToolsUtility.getFileChecksum(shaDigest, file)))
			{
				logQueue.add(Messages.ToolAreadyPresent);
				return name;
			}

		}
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
		private boolean forceDownload;

		public InstallToolsThread(Map<ToolsVO, List<VersionsVO>> selectedItems,
				IDFEnvironmentVariables idfEnvironmentVariables, boolean forceDownload)
		{
			this.selectedItems = selectedItems;
			this.forceDownload = forceDownload;
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
					installTool(toolsVo, versionsVO, forceDownload);
				}
			}

			runPythonEnvCommand();
			runToolsExport(getPythonExecutablePath(),
					idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.GIT_PATH));
			handleWebSocketClientInstall();
			configureToolChain();
			configEnv();
			copyOpenOcdRules();
			return Boolean.TRUE;
		}

		private void configEnv()
		{
			// Enable IDF_COMPONENT_MANAGER by default
			idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.IDF_COMPONENT_MANAGER, "1");
		}

		private void runToolsExport(final String pythonExePath, final String gitExePath)
		{
			final List<String> arguments = new ArrayList<>();
			arguments.add(pythonExePath);
			arguments.add(IDFUtil.getIDFToolsScriptFile().getAbsolutePath());
			arguments.add(IDFConstants.TOOLS_EXPORT_CMD);
			arguments.add(IDFConstants.TOOLS_EXPORT_CMD_FORMAT_VAL);

			final String cmd = Messages.AbstractToolsHandler_ExecutingMsg + " " + getCommandString(arguments); //$NON-NLS-1$
			logQueue.add(cmd);

			final Map<String, String> environment = new HashMap<>(System.getenv());
			if (gitExePath != null)
			{
				addGitToEnvironment(environment, gitExePath);
			}
			final ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
			try
			{
				final IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT,
						environment);
				if (status == null)
				{
					Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
					return;
				}

				// process export command output
				final String exportCmdOp = status.getMessage();
				logQueue.add(exportCmdOp);
				processExportCmdOutput(exportCmdOp, gitExePath);
			}
			catch (IOException e1)
			{
				Logger.log(IDFCorePlugin.getPlugin(), e1);
			}

		}

		private void processExportCmdOutput(final String exportCmdOp, final String gitExecutablePath)
		{
			// process export command output
			final String[] exportEntries = exportCmdOp.split("\n"); //$NON-NLS-1$
			for (String entry : exportEntries)
			{
				entry = entry.replaceAll("\\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
				String[] keyValue = entry.split("="); //$NON-NLS-1$
				if (keyValue.length == 2) // 0 - key, 1 - value
				{
					final String msg = MessageFormat.format("Key: {0} Value: {1}", keyValue[0], keyValue[1]); //$NON-NLS-1$
					Logger.log(msg);

					final IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
					String key = keyValue[0];
					String value = keyValue[1];
					if (key.equals(IDFEnvironmentVariables.PATH))
					{
						value = replacePathVariable(value);
						value = appendGitToPath(value, gitExecutablePath);
					}

					// add new or replace old entries
					idfEnvMgr.addEnvVariable(key, value);
				}

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
				value = value.replace("$PATH", pathEntry); // macOS //$NON-NLS-1$
				value = value.replace("%PATH%", pathEntry); // Windows //$NON-NLS-1$
			}
			return value;
		}

		private String appendGitToPath(String path, String gitExecutablePath)
		{
			IPath gitPath = new org.eclipse.core.runtime.Path(gitExecutablePath);
			if (!gitPath.toFile().exists())
			{
				Logger.log(NLS.bind("{0} doesn't exist", gitExecutablePath)); //$NON-NLS-1$
				return path;
			}

			String gitDir = gitPath.removeLastSegments(1).toOSString(); // ../bin/git
			if (!StringUtil.isEmpty(path) && !path.contains(gitDir)) // Git not found on the CDT build PATH environment
			{
				return path.concat(";").concat(gitDir); // append git path //$NON-NLS-1$
			}
			return path;
		}

		private void addGitToEnvironment(Map<String, String> envMap, String executablePath)
		{
			IPath gitPath = new org.eclipse.core.runtime.Path(executablePath);
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

		private void configureToolChain()
		{
			IToolChainManager tcManager = CCorePlugin.getService(IToolChainManager.class);
			ICMakeToolChainManager cmakeTcManager = CCorePlugin.getService(ICMakeToolChainManager.class);

			ESPToolChainManager toolchainManager = new ESPToolChainManager();
			toolchainManager.initToolChain(tcManager, ESPToolChainProvider.ID);
			toolchainManager.initCMakeToolChain(tcManager, cmakeTcManager);
		}

		private void copyOpenOcdRules()
		{
			if (Platform.getOS().equals(Platform.OS_LINUX)
					&& !IDFUtil.getOpenOCDLocation().equalsIgnoreCase(StringUtil.EMPTY))
			{
				Logger.log(Messages.InstallToolsHandler_CopyingOpenOCDRules);
				logQueue.add(Messages.InstallToolsHandler_CopyingOpenOCDRules);
				// Copy the rules to the idf
				StringBuilder pathToRules = new StringBuilder();
				pathToRules.append(IDFUtil.getOpenOCDLocation());
				pathToRules.append("/../share/openocd/contrib/60-openocd.rules"); //$NON-NLS-1$
				File rulesFile = new File(pathToRules.toString());
				if (rulesFile.exists())
				{
					Path source = Paths.get(pathToRules.toString());
					Path target = Paths.get("/etc/udev/rules.d/60-openocd.rules"); //$NON-NLS-1$
					Logger.log(String.format(Messages.InstallToolsHandler_OpenOCDRulesCopyPaths, source.toString(),
							target.toString()));
					logQueue.add(String.format(Messages.InstallToolsHandler_OpenOCDRulesCopyPaths, source.toString(),
							target.toString()));

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
										Logger.log(Messages.InstallToolsHandler_OpenOCDRulesNotCopied);
										logQueue.add(Messages.InstallToolsHandler_OpenOCDRulesNotCopied);
										return;
									}
								}
								else
								{
									Files.copy(source, target);
								}

								Logger.log(Messages.InstallToolsHandler_OpenOCDRulesCopied);
								logQueue.add(Messages.InstallToolsHandler_OpenOCDRulesCopied);
							}
							catch (IOException e)
							{
								Logger.log(e);
								Logger.log(Messages.InstallToolsHandler_OpenOCDRulesCopyError);
								logQueue.add(Messages.InstallToolsHandler_OpenOCDRulesCopyError);
							}
						}
					});
				}
			}
		}

		private String getCommandString(List<String> arguments)
		{
			StringBuilder builder = new StringBuilder();
			arguments.forEach(entry -> builder.append(entry + " ")); //$NON-NLS-1$

			return builder.toString().trim();
		}

		private String getPythonExecutablePath()
		{
			// find python from IDF_PYTHON_ENV_PATH env path
			String pythonExecutablenPath = IDFUtil.getIDFPythonEnvPath();
			if (!StringUtil.isEmpty(pythonExecutablenPath))
			{
				return pythonExecutablenPath;
			}

			// Get Python
			if (Platform.OS_WIN32.equals(Platform.getOS()))
			{
				PyWinRegistryReader pyWinRegistryReader = new PyWinRegistryReader();
				Map<String, String> pythonVersions = pyWinRegistryReader.getPythonVersions();
				if (pythonVersions.isEmpty())
				{
					Logger.log("No Python installations found in the system."); //$NON-NLS-1$
				}
				if (pythonVersions.size() == 1)
				{
					Map.Entry<String, String> entry = pythonVersions.entrySet().iterator().next();
					pythonExecutablenPath = entry.getValue();
				}
			}
			else
			{
				pythonExecutablenPath = IDFUtil.getPythonExecutable();
			}
			return pythonExecutablenPath;
		}

		private void runPythonEnvCommand()
		{
			List<String> arguments = new ArrayList<String>();

			ProcessBuilderFactory processRunner = new ProcessBuilderFactory();

			try
			{
				arguments.add(idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PYTHON_EXE_PATH));
				arguments.add(IDFUtil.getIDFToolsScriptFile().getAbsolutePath());
				arguments.add(IDFConstants.TOOLS_INSTALL_PYTHON_CMD);

				String cmdMsg = Messages.AbstractToolsHandler_ExecutingMsg + " " + getCommandString(arguments);
				logQueue.add(cmdMsg);

				Logger.log(cmdMsg);

				Map<String, String> environment = new HashMap<>(System.getenv());
				logQueue.add(environment.toString());

				IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT,
						environment);
				if (status == null)
				{
					Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
					return;
				}

				logQueue.add(status.getMessage());
				logQueue.add(System.lineSeparator());
			}
			catch (Exception e1)
			{
				Logger.log(IDFCorePlugin.getPlugin(), e1);

			}
		}

		private void handleWebSocketClientInstall()
		{
			List<String> arguments = new ArrayList<String>();
			final String pythonEnvPath = IDFUtil.getIDFPythonEnvPath();
			if (pythonEnvPath == null || !new File(pythonEnvPath).exists())
			{
				logQueue.add(String.format("%s executable not found. Unable to run `%s -m pip install websocket-client`", //$NON-NLS-1$
						IDFConstants.PYTHON_CMD, IDFConstants.PYTHON_CMD));
				return;
			}
			arguments.add(pythonEnvPath);
			arguments.add("-m"); //$NON-NLS-1$
			arguments.add("pip"); //$NON-NLS-1$
			
			arguments.add("install"); //$NON-NLS-1$
			arguments.add("websocket-client"); //$NON-NLS-1$

			ProcessBuilderFactory processRunner = new ProcessBuilderFactory();

			try
			{
				String cmdMsg = "Executing " + getCommandString(arguments); //$NON-NLS-1$
				logQueue.add(cmdMsg);
				Logger.log(cmdMsg);

				Map<String, String> environment = new HashMap<>(System.getenv());
				Logger.log(environment.toString());

				IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT,
						environment);
				if (status == null)
				{
					Logger.log(IDFCorePlugin.getPlugin(),
							IDFCorePlugin.errorStatus("Unable to get the process status.", null)); //$NON-NLS-1$
					logQueue.add("Unable to get the process status.");
					return;
				}

				logQueue.add(status.getMessage());

			}
			catch (Exception e1)
			{
				Logger.log(IDFCorePlugin.getPlugin(), e1);
				logQueue.add(e1.getLocalizedMessage());

			}
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
			return Boolean.TRUE;
		}
	}
}
