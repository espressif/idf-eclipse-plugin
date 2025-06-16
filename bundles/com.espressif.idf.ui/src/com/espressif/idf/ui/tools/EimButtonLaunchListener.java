package com.espressif.idf.ui.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.DownloadListener;
import com.espressif.idf.core.tools.EimDownloader;
import com.espressif.idf.core.tools.ToolInitializer;
import com.espressif.idf.core.tools.watcher.EimJsonWatchService;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;

public class EimButtonLaunchListener extends SelectionAdapter
{
	private ESPIDFMainTablePage espidfMainTablePage;
	private Display display;
	private Preferences preferences;
	private ToolInitializer toolInitializer;
	private IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
	private MessageConsoleStream standardConsoleStream;
	private MessageConsoleStream errorConsoleStream;
	private EimDownloader eimDownloader;

	public EimButtonLaunchListener(ESPIDFMainTablePage espidfMainTablePage, Display display,
			MessageConsoleStream standardConsoleStream, MessageConsoleStream errorConsoleStream)
	{
		this.espidfMainTablePage = espidfMainTablePage;
		this.display = display;
		this.standardConsoleStream = standardConsoleStream;
		this.errorConsoleStream = errorConsoleStream;
		preferences = org.eclipse.core.runtime.preferences.InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
		toolInitializer = new ToolInitializer(preferences);
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
					eimDownloader = new EimDownloader(new EimDownlaodListener());
					eimDownloader.downloadEim(monitor);
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
					Process process = launchEim(idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.EIM_PATH));
					new Thread(() -> {
						try
						{
							process.waitFor();
							display.asyncExec(() -> {
								try
								{
									standardConsoleStream.write("EIM has been closed.\n");
									refreshAfterEimClose();
								}
								catch (IOException e)
								{
									Logger.log(e);
								}
							});
						}
						catch (InterruptedException e)
						{
							Logger.log(e);
						}
					}).start();
				}
				catch (IOException e)
				{
					Logger.log(e);
				}
			});
		}
	}

	private boolean isEimInstalled()
	{
		return false; // toolInitializer.isEimInstalled();
	}

	private void installAndLaunchDmg(Path dmgPath) throws IOException, InterruptedException
	{
		standardConsoleStream.write("Mounting DMG...\n");
		ProcessBuilder mountBuilder = new ProcessBuilder("hdiutil", "attach", dmgPath.toString());
		Process mountProcess = mountBuilder.start();

		String volumePath = null;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(mountProcess.getInputStream())))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.contains("/Volumes/"))
				{
					String[] parts = line.split("\t");
					for (String part : parts)
					{
						if (part.startsWith("/Volumes/"))
						{
							volumePath = part.trim();
							break;
						}
					}
				}
			}
		}

		if (volumePath == null)
			throw new IOException("Failed to mount DMG: Volume path not found.");

		File[] apps = new File(volumePath).listFiles((dir, name) -> name.endsWith(".app"));
		if (apps == null || apps.length == 0)
			throw new FileNotFoundException("No .app found inside DMG.");

		File appBundle = apps[0];
		Path targetAppPath = Paths.get("/Applications", appBundle.getName());

		standardConsoleStream.write("Copying app to /Applications...\n");

		// Copy to /Applications
		ProcessBuilder copyBuilder = new ProcessBuilder("cp", "-R", appBundle.getAbsolutePath(),
				targetAppPath.toString());
		copyBuilder.inheritIO().start().waitFor();

		standardConsoleStream.write("Unmounting DMG...\n");
		new ProcessBuilder("hdiutil", "detach", volumePath).start().waitFor();

		standardConsoleStream.write("Launching app from /Applications...\n");

		Process openProcess = new ProcessBuilder("open", "-W", "-a", targetAppPath.toString()).start();
		new Thread(() -> {
			try
			{
				openProcess.waitFor();
				display.asyncExec(() -> {
					try
					{
						standardConsoleStream.write("EIM has been closed.\n");
						refreshAfterEimClose();
					}
					catch (IOException e)
					{
						Logger.log(e);
					}
				});
			}
			catch (InterruptedException e)
			{
				Logger.log(e);
			}
		}).start();
	}

	private void refreshAfterEimClose()
	{
		display.asyncExec(() -> {
			try
			{
				standardConsoleStream.write("Refreshing UI after EIM closed...\n");
				espidfMainTablePage.refreshEditorUI();
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		});
	}

	private Process launchEim(String eimPath) throws IOException
	{
		if (!Files.exists(Paths.get(eimPath)))
			throw new FileNotFoundException("EIM path not found: " + eimPath);

		String os = Platform.getOS();
		List<String> command;

		if (os.equals(Platform.OS_WIN32))
		{
			// Windows .exe or .msi
			command = List.of("cmd.exe", "/c", eimPath.toString());
		}
		else if (os.equals(Platform.OS_MACOSX))
		{
			command = List.of("open", "-a", eimPath.toString());
		}
		else if (os.equals(Platform.OS_LINUX))
		{
			command = List.of("bash", "-c", "\"" + eimPath.toString() + "\"");
		}
		else
		{
			throw new UnsupportedOperationException("Unsupported OS: " + os);
		}

		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

		standardConsoleStream.write("Launched EIM application: " + eimPath + "\n");

		return process;
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
						installAndLaunchDmg(Paths.get(filePath));
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
						process = launchEim(filePath);
						new Thread(() -> {
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
						}).start();
					}
					catch (IOException e)
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
