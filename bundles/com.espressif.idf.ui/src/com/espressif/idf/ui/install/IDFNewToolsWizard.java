/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFVersion;
import com.espressif.idf.core.ZipUtility;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.ToolSetConfigurationManager;
import com.espressif.idf.core.tools.vo.IDFToolSet;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.tools.ToolsActivationJob;
import com.espressif.idf.ui.tools.ToolsActivationJobListener;
import com.espressif.idf.ui.tools.ToolsInstallationJob;
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>, Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class IDFNewToolsWizard extends Wizard
{
	private static final int BUFFER_SIZE = 4096; // $NON-NLS-1$
	private IDFDownloadPage downloadPage;
	private ESPIDFMainTablePage espidfMainTablePage;
	private ToolSetConfigurationManager toolSetConfigurationManager;
	private ToolsInstallationJob toolsInstallationJob;
	private String pythonPath;
	private String gitPath;

	public IDFNewToolsWizard()
	{
	}

	public IDFNewToolsWizard(ESPIDFMainTablePage espidfMainTablePage)
	{
		this.espidfMainTablePage = espidfMainTablePage;
		toolSetConfigurationManager = new ToolSetConfigurationManager();
	}

	@Override
	public boolean performFinish()
	{
		IDFVersion version = downloadPage.Version();
		String destinationLocation = downloadPage.getDestinationLocation();
		boolean configureExistingEnabled = downloadPage.isConfigureExistingEnabled();
		pythonPath = downloadPage.getPythonExePath();
		gitPath = downloadPage.getGitExecutablePath();

		if (configureExistingEnabled)
		{
			String localIdfLocation = downloadPage.getExistingIDFLocation();

			toolsInstallationJob = new ToolsInstallationJob(pythonPath, gitPath, localIdfLocation);
			toolsInstallationJob.addJobChangeListener(new ToolsInstallationJobChangeListener());
			toolsInstallationJob.schedule();
			
		}
		else
		{
			new File(destinationLocation).mkdirs();
			String url = version.getUrl();
			if (version.getName().equals("master") || version.getName().startsWith("release/")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				Job job = new Job(MessageFormat.format(Messages.IDFDownloadWizard_CloningJobMsg, version.getName()))
				{
					@Override
					protected IStatus run(IProgressMonitor monitor)
					{
						String localIdfLocation = repositoryClone(version.getName(), url, destinationLocation, monitor);
						toolsInstallationJob = new ToolsInstallationJob(pythonPath, gitPath, localIdfLocation);
						toolsInstallationJob.addJobChangeListener(new ToolsInstallationJobChangeListener());
						toolsInstallationJob.schedule();
						return Status.OK_STATUS;
					}
				};

				job.setUser(true);
				job.schedule();
			}
			else
			{
				Job job = new Job(MessageFormat.format(Messages.IDFDownloadWizard_DownloadingJobMsg, version.getName()))
				{
					@Override
					protected IStatus run(IProgressMonitor monitor)
					{
						String localIdfLocation = download(monitor, url, destinationLocation);
						toolsInstallationJob = new ToolsInstallationJob(pythonPath, gitPath, localIdfLocation);
						toolsInstallationJob.addJobChangeListener(new ToolsInstallationJobChangeListener());
						toolsInstallationJob.schedule();
						return Status.OK_STATUS;
					}
				};

				job.setUser(true);
				job.schedule();

			}

			// Show the progress in Progress View
			openProgressView();
		}

		return true;
	}

	private void openProgressView()
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$
		}
		catch (PartInitException e)
		{
			Logger.log(e);
		}

	}

	protected String download(IProgressMonitor monitor, String url, String destinationLocation)
	{
		try
		{
			String downloadFile = downloadFile(url, destinationLocation, monitor);
			if (downloadFile != null)
			{
				unZipFile(downloadFile, destinationLocation);
				new File(downloadFile).delete();

				// extracts file name from URL
				String folderName = new File(url).getName().replace(".zip", ""); //$NON-NLS-1$ //$NON-NLS-2$

				return new File(destinationLocation, folderName).getAbsolutePath();
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
			showErrorMessage(e.getLocalizedMessage());
		}

		return StringUtil.EMPTY;
	}

	protected String repositoryClone(String version, String url, String destinationLocation, IProgressMonitor monitor)
	{
		GitRepositoryBuilder gitBuilder = new GitRepositoryBuilder(false, null);
		StringBuilder destinationLocationPath = new StringBuilder();
		destinationLocationPath.append(destinationLocation);
		destinationLocationPath.append("/esp-idf-"); //$NON-NLS-1$
		if (version.startsWith("release/")) //$NON-NLS-1$
		{
			destinationLocationPath.append(version.substring("release/".length())); //$NON-NLS-1$
		}
		else
		{
			destinationLocationPath.append(version);
		}

		gitBuilder.repositoryURI(url);
		gitBuilder.repositoryDirectory(new File(destinationLocationPath.toString()));
		gitBuilder.activeBranch(version);
		gitBuilder.setProgressMonitor(monitor);

		try
		{
			gitBuilder.repositoryClone();
			return destinationLocationPath.toString();

		}
		catch (Exception e)
		{
			Logger.log(e);
			showErrorMessage(e.getLocalizedMessage());
		}

		return StringUtil.EMPTY;
	}

	private void unZipFile(String downloadFile, String destinationLocation)
	{
		new ZipUtility().decompress(new File(downloadFile), new File(destinationLocation));
	}

	private void showErrorMessage(String errorMessage)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.IDFDownloadWizard_ErrorTitle,
						errorMessage);
			}
		});
	}

	public void init(IWorkbench aWorkbench, IStructuredSelection currentSelection)
	{
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages()
	{
		downloadPage = new IDFDownloadPage("Download page"); //$NON-NLS-1$
		addPage(downloadPage);
	}

	protected String downloadFile(String fileURL, String saveDir, IProgressMonitor monitor) throws IOException
	{

		String msg = MessageFormat.format(Messages.IDFDownloadWizard_DownloadingMessage, fileURL);
		Logger.log(msg);
		monitor.beginTask(msg, 100);

		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		int responseCode = httpConn.getResponseCode();

		// always check HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK)
		{
			String fileName = ""; //$NON-NLS-1$
			String disposition = httpConn.getHeaderField("Content-Disposition"); //$NON-NLS-1$
			String contentType = httpConn.getContentType();
			int contentLength = httpConn.getContentLength();

			if (disposition != null)
			{
				// extracts file name from header field
				String identifier = "filename="; //$NON-NLS-1$
				int index = disposition.indexOf(identifier);
				if (index > 0)
				{
					fileName = disposition.substring(index + identifier.length(), disposition.length());
				}
			}
			else
			{
				// extracts file name from URL
				fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length()); //$NON-NLS-1$
			}

			Logger.log("Content-Type = " + contentType); //$NON-NLS-1$
			Logger.log("Content-Disposition = " + disposition); //$NON-NLS-1$
			Logger.log("Content-Length = " + contentLength); //$NON-NLS-1$
			Logger.log("fileName = " + fileName); //$NON-NLS-1$

			// opens input stream from the HTTP connection
			InputStream inputStream = httpConn.getInputStream();
			String saveFilePath = saveDir + File.separator + fileName;

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);

			float downloaded = 0f;
			int bytesRead = -1;
			int noOfUnitedUpdated = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1)
			{
				outputStream.write(buffer, 0, bytesRead);
				downloaded = downloaded + BUFFER_SIZE;
				int unitsDownloadedSofar = (int) ((downloaded / contentLength) * 100);
				if (unitsDownloadedSofar > noOfUnitedUpdated)
				{
					int needToBeUpdated = unitsDownloadedSofar - noOfUnitedUpdated;
					noOfUnitedUpdated = noOfUnitedUpdated + needToBeUpdated;
					String taskName = MessageFormat.format(msg + "({0}/{1})", convertToMB(downloaded), //$NON-NLS-1$
							convertToMB(contentLength));
					monitor.setTaskName(taskName);
					monitor.worked(needToBeUpdated);
				}
				if (monitor.isCanceled())
				{
					Logger.log("File download cancelled"); //$NON-NLS-1$
					saveFilePath = null;
					break;
				}
			}

			outputStream.close();
			inputStream.close();

			return saveFilePath;
		}
		else
		{
			Logger.log("No file to download. Server replied HTTP code: " + responseCode); //$NON-NLS-1$
		}
		httpConn.disconnect();
		return null;
	}

	protected String convertToMB(float value)
	{
		return String.format("%.2f", (value / (1024 * 1024))) + " MB"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	
	private class ToolsInstallationJobChangeListener extends JobChangeAdapter
	{
		@Override
		public void done(IJobChangeEvent event)
		{
			List<IDFToolSet> idfToolSets = toolSetConfigurationManager.getIdfToolSets(false);
			Display.getDefault().asyncExec(() -> {
				if (espidfMainTablePage != null)
				{
					espidfMainTablePage.refreshTable();
				}
			});
			
			if (idfToolSets != null && idfToolSets.size() == 1)
			{
				ToolsActivationJob toolsActivationJob = new ToolsActivationJob(idfToolSets.get(0), pythonPath, gitPath);
				ToolsActivationJobListener toolsActivationJobListener = new ToolsActivationJobListener(espidfMainTablePage);
				toolsActivationJob.addJobChangeListener(toolsActivationJobListener);
				toolsActivationJob.schedule();
			}
			
		}
	}
}
