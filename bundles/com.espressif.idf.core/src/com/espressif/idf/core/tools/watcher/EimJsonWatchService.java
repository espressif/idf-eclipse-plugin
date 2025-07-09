/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.watcher;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;

/**
 * eim_idf.json watch service. The service will only watch for changes. Any handling must be done by the listeners to
 * this service.
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public class EimJsonWatchService extends Thread
{
	private final WatchService watchService;
	private final Path watchDirectoryPath;
	private final List<EimJsonChangeListener> eimJsonChangeListeners = new CopyOnWriteArrayList<>();
	private volatile boolean running = true;
	private volatile boolean paused = false;

	private EimJsonWatchService() throws IOException
	{
		String directoryPathString = Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_DIR
				: EimConstants.EIM_POSIX_DIR;

		watchDirectoryPath = Paths.get(directoryPathString);
		if (!Files.exists(watchDirectoryPath))
		{
			Files.createDirectories(watchDirectoryPath);
		}
		watchService = FileSystems.getDefault().newWatchService();
		watchDirectoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

		Logger.log("Watcher added to the directory: " + directoryPathString); //$NON-NLS-1$
		setName("EimJsonWatchService"); //$NON-NLS-1$
		setDaemon(true);
		start();
	}

	private static class Holder
	{
		private static EimJsonWatchService INSTANCE;

		static
		{
			try
			{
				INSTANCE = new EimJsonWatchService();
			}
			catch (IOException e)
			{
				Logger.log("Failed to initialize EimJsonWatchService"); //$NON-NLS-1$
				Logger.log(e);
			}
		}
	}

	public static EimJsonWatchService getInstance()
	{
		return Holder.INSTANCE;
	}

	public void addEimJsonChangeListener(EimJsonChangeListener listener)
	{
		if (listener != null)
		{
			eimJsonChangeListeners.add(listener);
		}
	}

	public void removeAllListeners()
	{
		eimJsonChangeListeners.clear();
	}

	public static void withPausedListeners(Runnable task)
	{
		EimJsonWatchService watchService = getInstance();
		boolean wasPaused = watchService.paused;
		watchService.pauseListeners();

		try
		{
			task.run();
		}
		catch (Exception e)
		{
			Logger.log(e);
		} finally
		{
			if (!wasPaused)
				watchService.unpauseListeners();
		}
	}

	public void pauseListeners()
	{
		Logger.log("Listeners are paused"); //$NON-NLS-1$
		paused = true;
	}

	public void unpauseListeners()
	{
		Logger.log("Listeners are resumed"); //$NON-NLS-1$
		paused = false;
	}

	@Override
	public void run()
	{
		while (running)
		{
			WatchKey key;
			try
			{
				key = watchService.take();
			}
			catch (InterruptedException e)
			{
				Logger.log("Watch Service Interrupted"); //$NON-NLS-1$
				Thread.currentThread().interrupt();
				break;
			}
			catch (ClosedWatchServiceException cwse)
			{
				break;
			}

			try
			{
				// Prevent handling multiple ENTRY_MODIFY events (e.g., when eim_idf.json is edited in a text editor
				// like VS Code)
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				Logger.log(e);
				Thread.currentThread().interrupt();
			}
			for (WatchEvent<?> event : key.pollEvents())
			{
				if (event.kind() == StandardWatchEventKinds.OVERFLOW)
				{
					continue;
				}

				Object context = event.context();
				if (context instanceof Path path && path.toString().equals(EimConstants.EIM_JSON))
				{
					Path fullPath = watchDirectoryPath.resolve(path);
					for (EimJsonChangeListener listener : eimJsonChangeListeners)
					{
						listener.onJsonFileChanged(fullPath, paused);
					}
				}
			}

			boolean valid = key.reset();
			if (!valid)
			{
				break;
			}
		}

		// clean up
		try
		{
			watchService.close();
			Logger.log("File Watch Service close"); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			Logger.log("Failed to close WatchService"); //$NON-NLS-1$
			Logger.log(e);
		}
	}

	@Override
	public void interrupt()
	{
		running = false;
		super.interrupt();
	}

	public void shutdown()
	{
		running = false;
		interrupt();
	}
}
