/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Queue;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.IDFVersion;
import com.espressif.idf.core.ZipUtility;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.install.GitRepositoryBuilder;
import com.espressif.idf.ui.tools.wizard.pages.InstallEspIdfPage;

/**
 * Git clone thread and also used for downloading the idf tools
 * 
 * @author Ali Azam Rana
 *
 */
public class GitDownloadAndCloneThread extends Thread
{
	private static final int BUFFER_SIZE = 4096;

	private IDFVersion version;
	private String url;
	private String downloadLocation;
	private final Queue<String> logMessages;
	private GitWizardRepProgressMonitor gitWizardRepProgressMonitor;
	private boolean cancelled;
	private InstallEspIdfPage installEspIdfPage;
	private boolean cloning;
	private int size;
	private int completedSize;
	private ProgressBar progressBar;
	private Display display;

	public GitDownloadAndCloneThread(IDFVersion version, String url, String downloadLocation, Queue<String> logMessages,
			InstallEspIdfPage installEspIdfPage, ProgressBar progressBar)
	{
		super(MessageFormat.format(Messages.GitCloningJobMsg, version.getName()));
		this.url = url;
		this.downloadLocation = downloadLocation;
		this.version = version;
		this.logMessages = logMessages;
		this.installEspIdfPage = installEspIdfPage;
		this.progressBar = progressBar;
		display = progressBar.getDisplay();
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setSelection(0);
			}
		});
	}

	@Override
	public void run()
	{
		installEspIdfPage.setCloningOrDownloading(true);
		if (version.getName().equals("master")) //$NON-NLS-1$
		{
			cloning = true;
			repositoryClone(version.getName(), url, downloadLocation);
		}
		else
		{
			download(url, downloadLocation);
		}

		installEspIdfPage.getControlsContainer().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				installEspIdfPage.getBtnCancel().setVisible(false);
				installEspIdfPage.getBtnExisting().setSelection(true);
				installEspIdfPage.getTxtIdfpath()
						.setText(new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PATH));
				installEspIdfPage.enableAllControls(false, true, false);
				installEspIdfPage.getBtnNew().setEnabled(true);
				installEspIdfPage.getBtnNew().setSelection(false);
				installEspIdfPage.setCloningOrDownloading(false);
				installEspIdfPage.setPageComplete(true); // Triggers internal flow to check for page completion
				logMessages.clear();
				logMessages.add(".... Operations Completed!");
			}
		});

	}

	private void download(String url, String destinationLocation)
	{
		try
		{
			String downloadFile = downloadFile(url, destinationLocation);
			if (downloadFile != null)
			{
				String folderName = new File(url).getName().replace(".zip", ""); //$NON-NLS-1$ //$NON-NLS-2$
				logMessages.add(MessageFormat.format(Messages.IDFDownloadWizard_DownloadCompleteMsg, folderName));
				// extracts file name from URL
				unZipFile(downloadFile, destinationLocation);
				logMessages.add(Messages.IDFDownloadWizard_DecompressingCompleted);
				new File(downloadFile).delete();
				configurePath(destinationLocation, folderName);

			}
		}
		catch (IOException e)
		{
			logMessages.add(e.getLocalizedMessage());
		}
	}
	
	private void initializeMaxProgressbar(int max)
	{
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setVisible(true);
				progressBar.setMaximum(max);
			}
		});
	}

	private void updateProgressBar(int updateValue)
	{
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setSelection(updateValue);
			}
		});
	}
	
	private void setProgressBarVisibility(boolean visible)
	{
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setVisible(visible);
			}
		});
	}		
	
	private void configurePath(String destinationDir, String folderName)
	{
		String idf_path = new File(destinationDir, folderName).getAbsolutePath();
		Logger.log("Setting IDF_PATH to:" + idf_path); //$NON-NLS-1$
		logMessages.add(MessageFormat.format(Messages.IDFDownloadWizard_UpdatingIDFPathMessage, idf_path));

		// Configure IDF_PATH
		new IDFEnvironmentVariables().addEnvVariable("IDF_PATH", //$NON-NLS-1$
				new File(destinationDir, folderName).getAbsolutePath());
	}
	
	private void configurePath(String destinationDir)
	{
		String idf_path = new File(destinationDir).getAbsolutePath();
		Logger.log("Setting IDF_PATH to:" + idf_path); //$NON-NLS-1$
		logMessages.add(MessageFormat.format(Messages.IDFDownloadWizard_UpdatingIDFPathMessage, idf_path));

		// Configure IDF_PATH
		new IDFEnvironmentVariables().addEnvVariable("IDF_PATH", //$NON-NLS-1$
				idf_path);
	}

	private void unZipFile(String downloadFile, String destinationLocation)
	{
		logMessages.add(Messages.IDFDownloadWizard_DecompressingMsg);
		new ZipUtility().decompress(new File(downloadFile), new File(destinationLocation));
	}

	private String downloadFile(String fileURL, String saveDir) throws IOException
	{

		String msg = MessageFormat.format(Messages.IDFDownloadWizard_DownloadingMessage, fileURL);
		logMessages.add(msg);

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
			setProgressBarVisibility(true);
			initializeMaxProgressbar(contentLength);
			size = contentLength;
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

			logMessages.add("Content-Type = " + contentType); //$NON-NLS-1$
			logMessages.add("Content-Disposition = " + disposition); //$NON-NLS-1$
			logMessages.add("Content-Length = " + contentLength); //$NON-NLS-1$
			logMessages.add("fileName = " + fileName); //$NON-NLS-1$

			// opens input stream from the HTTP connection
			InputStream inputStream = httpConn.getInputStream();
			String saveFilePath = saveDir + File.separator + fileName;

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);
			float downloaded = 0f;
			int bytesRead = -1;
			int noOfUnitedUpdated = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			while (((bytesRead = inputStream.read(buffer)) != -1) && !cancelled)
			{
				outputStream.write(buffer, 0, bytesRead);
				downloaded = downloaded + BUFFER_SIZE;
				completedSize = (int) downloaded;
				int unitsDownloadedSofar = (int) ((downloaded / contentLength) * 100);
				if (unitsDownloadedSofar > noOfUnitedUpdated)
				{
					int needToBeUpdated = unitsDownloadedSofar - noOfUnitedUpdated;
					noOfUnitedUpdated = noOfUnitedUpdated + needToBeUpdated;
					String taskName = MessageFormat.format(msg + "({0}/{1})", convertToMB(downloaded), //$NON-NLS-1$
							convertToMB(contentLength));
					logMessages.add(taskName);
				}
				updateProgressBar((int) downloaded);
			}

			if (cancelled)
			{
				Logger.log("File download cancelled"); //$NON-NLS-1$
				logMessages.add("File download cancelled"); //$NON-NLS-1$
				saveFilePath = null;
				setProgressBarVisibility(false);
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

	private String convertToMB(float value)
	{
		return String.format("%.2f", (value / (1024 * 1024))) + " MB"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void repositoryClone(String version, String url, String destinationLocation)
	{
		gitWizardRepProgressMonitor = new GitWizardRepProgressMonitor(logMessages, progressBar);
		GitRepositoryBuilder gitBuilder = new GitRepositoryBuilder(true, gitWizardRepProgressMonitor);
		gitBuilder.repositoryURI(url);
		gitBuilder.repositoryDirectory(new File(destinationLocation));
		gitBuilder.activeBranch(version);

		try
		{
			gitBuilder.repositoryClone();
			configurePath(destinationLocation);
			logMessages.add(Messages.CloningCompletedMsg);

		}
		catch (Exception e)
		{
			Logger.log(e);
			logMessages.add(e.getLocalizedMessage());
		}
	}

	public void setCancelled(boolean isCancelled)
	{
		this.cancelled = isCancelled;
		gitWizardRepProgressMonitor.setJobCancelled(isCancelled);
	}

	public boolean isCloning()
	{
		return cloning;
	}

	public int totalSize()
	{
		return size;
	}

	public int getDownloadedSize()
	{
		return completedSize;
	}
}
