/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.watcher.EimJsonWatchService;
import com.espressif.idf.core.util.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class is responsible for downloading and launching the EIM. 
 * The clients using this must take care of UI refreshes and pausing any listeners.
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class EimLoader
{
	private static final String URL_JSON = "https://dl.espressif.com/dl/eim/eim_unified_release.json"; //$NON-NLS-1$
	private static final Path DOWNLOAD_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "eim_gui"); //$NON-NLS-1$ //$NON-NLS-2$

	private String os;
	private String arch;
	private DownloadListener listener;
	private MessageConsoleStream standardConsoleStream;
	private MessageConsoleStream errorConsoleStream;
	private Display display;
	private long windowsPid;

	public EimLoader(DownloadListener listener, MessageConsoleStream standardConsoleStream, MessageConsoleStream errorConsoleStream, Display display)
	{
		os = Platform.getOS();
		arch = Platform.getOSArch();
		this.listener = listener;
		this.standardConsoleStream = standardConsoleStream;
		this.errorConsoleStream = errorConsoleStream;
		this.display = display;
	}
	
	private void logMessage(String message)
	{
		display.asyncExec(()->{
			try
			{
				standardConsoleStream.write(message);
			}
			catch (IOException e)
			{
				Logger.log(e);
				logError(e.getMessage());
			}
		});
		
		Logger.log(message);
	}
	
	private void logError(String message)
	{
		display.asyncExec(()->{
			try
			{
				errorConsoleStream.write(message);
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		});
		
		Logger.log(message);
	}
	
	public Process launchEim(String eimPath) throws IOException
	{
		if (!Files.exists(Paths.get(eimPath)))
			throw new FileNotFoundException("EIM path not found: " + eimPath); //$NON-NLS-1$

		String os = Platform.getOS();
		List<String> command;

		if (os.equals(Platform.OS_WIN32))
		{
			 String powershellCmd = String.format(
				        "Start-Process -FilePath \"%s\" -PassThru  | Select-Object -ExpandProperty Id", //$NON-NLS-1$
				        eimPath
				    );
			command = List.of("powershell.exe",  //$NON-NLS-1$
					"-Command",  //$NON-NLS-1$
					powershellCmd);
		}
		else if (os.equals(Platform.OS_MACOSX))
		{
			command = List.of("open", "-W",  "-a", eimPath);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		}
		else if (os.equals(Platform.OS_LINUX))
		{
			command = List.of("bash", "-c", "\"" + eimPath + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		else
		{
			throw new UnsupportedOperationException("Unsupported OS: " + os); //$NON-NLS-1$
		}
		
		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
		if (os.equals(Platform.OS_WIN32))
		{
			// store the PID returned by powershell query to a variable
			storePid(process);
		}

		logMessage("Launched EIM application: " + eimPath + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		return process;
	}
	
	private void storePid(Process powershellProcess)
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(powershellProcess.getInputStream())))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (!line.isEmpty())
				{
					try
					{
						windowsPid = Long.parseLong(line);
						
					}
					catch (NumberFormatException ignored)
					{
						// skipping invalid lines
					}
				}
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	public String installAndLaunchDmg(Path dmgPath) throws IOException, InterruptedException
	{
		logMessage("Mounting DMG…\n"); //$NON-NLS-1$
		Process mountProcess = new ProcessBuilder("hdiutil", "attach", dmgPath.toString()) //$NON-NLS-1$ //$NON-NLS-2$
				.redirectErrorStream(true).start();

		int mountExit = mountProcess.waitFor();
		if (mountExit != 0)
			throw new IOException("hdiutil attach failed (exit " + mountExit + "): " //$NON-NLS-1$ //$NON-NLS-2$
					+ readProcessOutput(mountProcess));

		String volumePath = parseVolumePath(mountProcess.getInputStream());
		if (volumePath == null)
			throw new IOException("Failed to mount DMG: volume path not found."); //$NON-NLS-1$

		File[] apps = new File(volumePath).listFiles((dir, name) -> name.endsWith(".app")); //$NON-NLS-1$
		if (apps == null || apps.length == 0)
			throw new FileNotFoundException("No .app found inside DMG."); //$NON-NLS-1$

		File appBundle = apps[0];
		Path targetAppPath = Paths.get("/Applications", appBundle.getName()); //$NON-NLS-1$

		logMessage("Copying app to /Applications…\n"); //$NON-NLS-1$
		Process copyProcess = new ProcessBuilder("cp", "-R", appBundle.getAbsolutePath(), //$NON-NLS-1$ //$NON-NLS-2$
				targetAppPath.toString()).redirectErrorStream(true).start();

		int copyExit = copyProcess.waitFor();
		if (copyExit != 0)
			throw new IOException("Copy failed (exit " + copyExit + "): " //$NON-NLS-1$ //$NON-NLS-2$
					+ readProcessOutput(copyProcess));

		logMessage("Unmounting DMG…\n"); //$NON-NLS-1$
		Process detachProcess = new ProcessBuilder("hdiutil", "detach", volumePath) //$NON-NLS-1$ //$NON-NLS-2$
				.redirectErrorStream(true).start();

		int detachExit = detachProcess.waitFor();
		if (detachExit != 0)
			throw new IOException("hdiutil detach failed (exit " + detachExit + "): " //$NON-NLS-1$ //$NON-NLS-2$
					+ readProcessOutput(detachProcess));

		String eimPath = targetAppPath.resolve("Contents/MacOS/eim").toString(); //$NON-NLS-1$
		new IDFEnvironmentVariables().addEnvVariable(IDFEnvironmentVariables.EIM_PATH, eimPath);
		return eimPath;
	}
	
	private String readProcessOutput(Process p) throws IOException
	{
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream())))
	    {
	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = br.readLine()) != null) sb.append(line).append(System.lineSeparator());
	        return sb.toString();
	    }
	}

	private String parseVolumePath(InputStream mountOut) throws IOException
	{
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(mountOut)))
	    {
	        String line;
	        while ((line = reader.readLine()) != null)
	        {
	            if (line.contains("/Volumes/")) //$NON-NLS-1$
	            {
	                for (String part : line.split("\t")) //$NON-NLS-1$
	                    if (part.startsWith("/Volumes/")) return part.trim(); //$NON-NLS-1$
	            }
	        }
	    }
	    return null;
	}


	public void downloadEim(IProgressMonitor monitor)
	{
		try
		{
			JsonObject root = fetchJson();
			JsonArray assets = root.getAsJsonArray("assets"); //$NON-NLS-1$
			Optional<JsonObject> match = findMatchingAsset(assets);

			if (match.isEmpty())
			{
				listener.onError("No suitable EIM GUI asset found.", null); //$NON-NLS-1$
				monitor.done();
				return;
			}

			JsonObject asset = match.get();
			String name = asset.get("name").getAsString(); //$NON-NLS-1$
			String downloadUrl = asset.get("browser_download_url").getAsString(); //$NON-NLS-1$

			Files.createDirectories(DOWNLOAD_DIR);
			cleanupDownloadDirectory();
			Path downloadPath = DOWNLOAD_DIR.resolve(name);

			downloadFile(downloadUrl, downloadPath, listener, monitor);
			Path eimPath = Paths.get(EimConstants.USER_EIM_DIR);
			Files.createDirectories(eimPath);
			
			if (name.endsWith(".zip")) //$NON-NLS-1$
			{
				Path extracted = unzip(downloadPath, eimPath);
				listener.onCompleted(extracted.toAbsolutePath().toString());
			}
			else if (name.endsWith(".exe")) //$NON-NLS-1$
			{
				eimPath = Paths.get(eimPath.toString(), name);
				Files.copy(downloadPath, eimPath, StandardCopyOption.REPLACE_EXISTING);
				listener.onCompleted(eimPath.toString());
			}
			else 
			{
				listener.onCompleted(downloadPath.toAbsolutePath().toString());
			}
		}
		catch (IOException e)
		{
			listener.onError("Download failed", e); //$NON-NLS-1$
		} finally
		{
			monitor.done();
		}

	}
	
	private IStatus waitForProcessWindows()
	{
		while (isWindowsProcessAlive(windowsPid))
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				Logger.log(e);
			}
		}
		
		return Status.OK_STATUS;
	}
	
	private boolean isWindowsProcessAlive(long pid)
	{
		try
		{
			Process check = new ProcessBuilder("cmd.exe",  //$NON-NLS-1$
					"/c",  //$NON-NLS-1$
					"tasklist", //$NON-NLS-1$
					"/FI", //$NON-NLS-1$
					"\"PID eq " + pid + "\"").redirectErrorStream(true).start(); //$NON-NLS-1$ //$NON-NLS-2$
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(check.getInputStream())))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					if (line.contains(String.valueOf(windowsPid)))
					{
						return true;
					}
				}
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		
		return false;
	}
	
	private IStatus waitForProcess(Process process)
	{
		try
		{
			process.waitFor();
			return Status.OK_STATUS;
		}
		catch (InterruptedException e)
		{
			return Status.CANCEL_STATUS;
		}
		catch (Exception e)
		{
			Logger.log(e);
			return Status.error(e.getMessage());
		}
	}
	
	public void waitForEimClosure(Process process, Runnable callback)
	{
		Job waitJob = new Job("Wait for EIM Closure") //$NON-NLS-1$
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				return os.equals(Platform.OS_WIN32) ? waitForProcessWindows() : waitForProcess(process);
			}
		};
		waitJob.setSystem(true);
		
		
		waitJob.addJobChangeListener(new JobChangeAdapter()
		{
			@Override
			public void aboutToRun(IJobChangeEvent event) 
			{
				EimJsonWatchService.getInstance().pauseListeners();
			}
			
			@Override
			public void done(IJobChangeEvent event)
			{
				Display.getDefault().asyncExec(() -> {
					try
					{
						standardConsoleStream.write("EIM has been closed.\n"); //$NON-NLS-1$
					}
					catch (IOException e)
					{
						Logger.log(e);
					}
				});
				
				if (callback != null)
				{
					callback.run();
				}
				
				EimJsonWatchService.getInstance().unpauseListeners();
			}
		});
		
		waitJob.schedule();
	}

	private JsonObject fetchJson() throws IOException
	{
		URL url = new URL(URL_JSON);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("accept", "application/json"); //$NON-NLS-1$//$NON-NLS-2$
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);

		try (InputStreamReader reader = new InputStreamReader(connection.getInputStream()))
		{
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}

	private void cleanupDownloadDirectory()
	{
	    try
	    {
	        Files.list(DOWNLOAD_DIR)
	             .filter(Files::isRegularFile)
	             .forEach(path -> {
	                 try
	                 {
	                     Files.deleteIfExists(path);
	                 }
	                 catch (IOException e)
	                 {
	                     Logger.log("Failed to delete old download: " + path); //$NON-NLS-1$
	                 }
	             });
	    }
	    catch (IOException e)
	    {
	        Logger.log("Failed to clean up download directory: " + e.getMessage()); //$NON-NLS-1$
	    }
	}
	
	private Optional<JsonObject> findMatchingAsset(JsonArray assets)
	{
		String osToken = switch (os)
		{
		case Platform.OS_WIN32 -> "windows"; //$NON-NLS-1$
		case Platform.OS_MACOSX -> "macos"; //$NON-NLS-1$
		case Platform.OS_LINUX -> "linux"; //$NON-NLS-1$
		default -> StringUtil.EMPTY;
		};

		String archToken = switch (arch)
		{
		case Platform.ARCH_X86_64 -> "x64"; //$NON-NLS-1$
		case Platform.ARCH_AARCH64, "arm64" -> "aarch64"; //$NON-NLS-1$ //$NON-NLS-2$
		default -> StringUtil.EMPTY;
		};

		for (int i = 0; i < assets.size(); i++)
		{
			JsonObject asset = assets.get(i).getAsJsonObject();
			String name = asset.get("name").getAsString().toLowerCase(); //$NON-NLS-1$
			if (name.contains("eim-gui") && //$NON-NLS-1$
					name.contains(osToken) && name.contains(archToken)
					&& (name.endsWith(".exe") || name.endsWith(".dmg") || name.endsWith(".zip"))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{
				return Optional.of(asset);
			}
		}

		return Optional.empty();
	}

	private void downloadFile(String fileURL, Path targetPath, DownloadListener listener, IProgressMonitor monitor)
			throws IOException
	{
		URL url = new URL(fileURL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);

		int contentLength = connection.getContentLength();
		monitor.beginTask("Downloading " + targetPath.getFileName(), contentLength); //$NON-NLS-1$

		try (InputStream in = connection.getInputStream(); OutputStream out = Files.newOutputStream(targetPath))
		{

			byte[] buffer = new byte[8192];
			int bytesRead;
			long totalRead = 0;
			int lastPercent = 0;

			while ((bytesRead = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, bytesRead);
				totalRead += bytesRead;
				if (contentLength > 0)
				{
					int percent = (int) ((totalRead * 100) / contentLength);
					if (percent != lastPercent)
					{
						listener.onProgress(percent);
						lastPercent = percent;
						monitor.worked(bytesRead);
					}
				}
			}
		}
	}

	private Path unzip(Path zipPath, Path destDir) throws IOException
	{
		Files.createDirectories(destDir);
		Path firstExecutable = null;

		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile())))
		{
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null)
			{
				Path newPath = destDir.resolve(entry.getName());
				if (entry.isDirectory())
				{
					Files.createDirectories(newPath);
				}
				else
				{
					Files.createDirectories(newPath.getParent());
					Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
					if (firstExecutable == null && Files.isRegularFile(newPath))
					{
						newPath.toFile().setExecutable(true);
						firstExecutable = newPath;
					}
				}
			}
		}
		return firstExecutable != null ? firstExecutable : destDir;
	}

}
