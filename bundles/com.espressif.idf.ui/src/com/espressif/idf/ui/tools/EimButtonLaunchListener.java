/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.ide.IDE;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.DownloadListener;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.EimLoader;
import com.espressif.idf.core.tools.ToolInitializer;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.watcher.EimJsonWatchService;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerEditor;
import com.espressif.idf.ui.tools.manager.EimEditorInput;
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;

/**
 * This is a class that other UI elements can also use to trigger 
 * a simulated event on any widget to launch or download the EIM.
 * The primary usage is in {@link ESPIDFMainTablePage}
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class EimButtonLaunchListener extends SelectionAdapter
{
	private ESPIDFMainTablePage espidfMainTablePage;
	private Display display;
	private Preferences preferences;
	private ToolInitializer toolInitializer;
	private IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
	private MessageConsoleStream standardConsoleStream;
	private MessageConsoleStream errorConsoleStream;
	private EimLoader eimLoader;

	public EimButtonLaunchListener(ESPIDFMainTablePage espidfMainTablePage, Display display,
			MessageConsoleStream standardConsoleStream, MessageConsoleStream errorConsoleStream)
	{
		this.espidfMainTablePage = espidfMainTablePage;
		this.display = display;
		this.standardConsoleStream = standardConsoleStream;
		this.errorConsoleStream = errorConsoleStream;
		preferences = org.eclipse.core.runtime.preferences.InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
		toolInitializer = new ToolInitializer(preferences);
		eimLoader = new EimLoader(new EimDownlaodListener(), standardConsoleStream, errorConsoleStream, display);
	}
	
	@Override
	public void widgetSelected(SelectionEvent selectionEvent)
	{
		if (!isEimInstalled())
		{
			Job downloadJob = new Job("Download and Launch EIM")
			{

				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					eimLoader.downloadEim(monitor);
					return Status.OK_STATUS;
				}
			};
			downloadJob.setUser(true);
			downloadJob.schedule();
		}
		else
		{
			EimJsonWatchService.withPausedListeners(() -> {
				try
				{
					Process process = eimLoader.launchEim(idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.EIM_PATH));
					waitForEimClosure(process);
				}
				catch (IOException | InterruptedException e)
				{
					Logger.log(e);
				}
			});
		}
	}

	private boolean isEimInstalled()
	{
		return toolInitializer.isEimInstalled();
	}

	private void waitForEimClosure(Process process) throws InterruptedException
	{
		Thread t = new Thread(() -> {
			try
			{
				process.waitFor();
				display.asyncExec(() -> {
					try
					{
						standardConsoleStream.write("EIM has been closed.\n");
					}
					catch (IOException e)
					{
						Logger.log(e);
					}
					refreshAfterEimClose();
				});
			}
			catch (Exception ex)
			{
				Logger.log(ex);
			}
		});
		
		t.start();
		t.join();
	}
	
	private void refreshAfterEimClose()
	{
		display.asyncExec(() -> {
			try
			{
				launchEspIdfManager();
				standardConsoleStream.write("Refreshing UI after EIM closed...\n");
				espidfMainTablePage.refreshEditorUI();
			}
			catch (IOException | PartInitException e)
			{
				Logger.log(e);
			}
		});
	}
	
	private void launchEspIdfManager() throws PartInitException
	{
		Display.getDefault().asyncExec(() -> {
			IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
			if (activeww == null || activeww.getActivePage() == null)
			{
				Logger.log("Cannot open ESP-IDF Manager. No active workbench window or active page.");
				return;
			}
			
			try
			{
				EimIdfConfiguratinParser eimIdfConfiguratinParser = new EimIdfConfiguratinParser();
				EimJson eimJson = eimIdfConfiguratinParser.getEimJson(true);
				IDE.openEditor(activeww.getActivePage(), new EimEditorInput(eimJson), ESPIDFManagerEditor.EDITOR_ID,
						true);
			}
			catch (PartInitException | IOException e)
			{
				Logger.log("Failed to open ESP-IDF Manager Editor.");
				Logger.log(e);
			}
		});

	}

	private class EimDownlaodListener implements DownloadListener
	{
		@Override
		public void onProgress(int percent)
		{
			display.asyncExec(() -> {
				try
				{
					int blocks = percent / 10;
					String bar = "[" + "#".repeat(blocks) + " ".repeat(10 - blocks) + "] " + percent + "%";
					standardConsoleStream.write("\r" + bar);
				}
				catch (IOException e)
				{
					Logger.log(e);
				}
			});
		}

		@Override
		public void onCompleted(String filePath)
		{
			EimJsonWatchService.withPausedListeners(() -> {
				display.syncExec(() -> {
					try
					{
						standardConsoleStream.write("\nEIM Downloaded to: " + filePath + "\nLaunching...\n");
					}
					catch (IOException e)
					{
						Logger.log(e);
					}
				});

				if (filePath.endsWith(".dmg"))
				{
					try
					{
						Process process = eimLoader.installAndLaunchDmg(Paths.get(filePath));
						waitForEimClosure(process);
					}
					catch (
							IOException
							| InterruptedException e)
					{
						Logger.log(e);
					}
				}
				else
				{
					Process process;
					try
					{
						idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.EIM_PATH, filePath);
						process = eimLoader.launchEim(filePath);
						waitForEimClosure(process);
					}
					catch (IOException | InterruptedException e)
					{
						Logger.log(e);
					}
				}

				
			});
		}

		@Override
		public void onError(String message, Exception e)
		{
			display.asyncExec(() -> {
				try
				{
					errorConsoleStream.write("Download Failed: " + e.getMessage());
				}
				catch (IOException e1)
				{
					Logger.log(e1);
				}
			});
		}

	}

}
