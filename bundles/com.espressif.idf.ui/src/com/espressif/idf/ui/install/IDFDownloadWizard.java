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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.IDFVersion;
import com.espressif.idf.core.ZipUtility;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.update.InstallToolsHandler;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFDownloadWizard extends Wizard
{
	private static final int BUFFER_SIZE = 4096; // $NON-NLS-1$
	private IDFDownloadPage downloadPage;

	@Override
	public boolean performFinish()
	{
		IDFVersion version = downloadPage.Version();
		String destinationLocation = downloadPage.getDestinationLocation();
		boolean configureExistingEnabled = downloadPage.isConfigureExistingEnabled();
		if (configureExistingEnabled)
		{
			String existingIDFLocation = downloadPage.getExistingIDFLocation();
			Logger.log("Setting IDF_PATH to :" + existingIDFLocation); //$NON-NLS-1$

			// Configure IDF_PATH
			new IDFEnvironmentVariables().addEnvVariable("IDF_PATH", existingIDFLocation); //$NON-NLS-1$

			showMessage(MessageFormat.format(Messages.IDFDownloadWizard_ConfigMessage, existingIDFLocation));

		}
		else
		{
			new File(destinationLocation).mkdirs();
			String url = version.getUrl();

			if (version.getName().equals("master")) //$NON-NLS-1$
			{
				Job job = new Job(MessageFormat.format(Messages.IDFDownloadWizard_CloningJobMsg, version.getName()))
				{
					@Override
					protected IStatus run(IProgressMonitor monitor)
					{
						repositoryClone(version.getName(), url, destinationLocation, monitor);
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
						download(monitor, url, destinationLocation);
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

	protected void download(IProgressMonitor monitor, String url, String destinationLocation)
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

				configurePath(destinationLocation, folderName);
				showMessage(MessageFormat.format(Messages.IDFDownloadWizard_DownloadCompleteMsg, folderName));
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
			showErrorMessage(e.getLocalizedMessage());
		}
	}

	protected void repositoryClone(String version, String url, String destinationLocation, IProgressMonitor monitor)
	{
		GitRepositoryBuilder gitBuilder = new GitRepositoryBuilder();
		gitBuilder.repositoryURI(url);
		gitBuilder.repositoryDirectory(new File(destinationLocation + "/" + "esp-idf")); //$NON-NLS-1$ //$NON-NLS-2$
		gitBuilder.activeBranch(version);
		gitBuilder.setProgressMonitor(monitor);

		try
		{
			gitBuilder.repositoryClone();
			configurePath(destinationLocation, "esp-idf"); //$NON-NLS-1$
			showMessage(MessageFormat.format(Messages.IDFDownloadWizard_CloningCompletedMsg, version));

		}
		catch (Exception e)
		{
			Logger.log(e);
			showErrorMessage(e.getLocalizedMessage());
		}
	}

	private void configurePath(String destinationDir, String folderName)
	{
		String idf_path = new File(destinationDir, folderName).getAbsolutePath();
		Logger.log("Setting IDF_PATH to:" + idf_path); //$NON-NLS-1$

		// Configure IDF_PATH
		new IDFEnvironmentVariables().addEnvVariable("IDF_PATH", //$NON-NLS-1$
				new File(destinationDir, folderName).getAbsolutePath());
	}

	private void unZipFile(String downloadFile, String destinationLocation)
	{
		new ZipUtility().decompress(new File(downloadFile), new File(destinationLocation));
	}

	private void showMessage(final String message)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
						Messages.IDFDownloadWizard_MessageTitle, message);
				if (isYes)
				{
					InstallToolsHandler installToolsHandler = new InstallToolsHandler();
					try
					{
						installToolsHandler.setCommandId("com.espressif.idf.ui.command.install"); //$NON-NLS-1$
						installToolsHandler.execute(null);
					}
					catch (ExecutionException e)
					{
						Logger.log(e);
					}
				}
			}
		});
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

}
