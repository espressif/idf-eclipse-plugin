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
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.tools.EimIdfJsonPathResolver;

/**
 * eim_idf.json watch service. The service will only watch for changes. Any handling must be done by the listeners to
 * this service.
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class EimJsonWatchService extends Thread
{
	private final WatchService watchService;
	private final Path watchDirectoryPath;
	private final List<EimJsonChangeListener> eimJsonChangeListeners = new CopyOnWriteArrayList<>();
	private volatile boolean running = true;
	private volatile boolean paused = false;
	private volatile Instant lastModifiedTime;

	private EimJsonWatchService() throws IOException
	{
		EimIdfJsonPathResolver r = new EimIdfJsonPathResolver();
		Path json = r.resolveEimIdfJsonFile();
		Path def = r.getDefaultEimIdfJsonFile();
		if (json.getParent() == null)
		{
			throw new IOException("Invalid eim_idf.json path"); //$NON-NLS-1$
		}
		watchDirectoryPath = json.getParent();
		if (json.toAbsolutePath().normalize().equals(def.toAbsolutePath().normalize()))
		{
			if (!Files.exists(watchDirectoryPath))
			{
				Files.createDirectories(watchDirectoryPath);
			}
		}
		if (!Files.exists(watchDirectoryPath))
		{
			throw new IOException("Directory for eim_idf.json does not exist: " + watchDirectoryPath); //$NON-NLS-1$
		}
		watchService = FileSystems.getDefault().newWatchService();
		watchDirectoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

		Logger.log("Watcher added to the directory: " + watchDirectoryPath); //$NON-NLS-1$
		setName("EimJsonWatchService"); //$NON-NLS-1$
		setDaemon(true);
		start();
	}

	private static EimJsonWatchService INSTANCE;

	public static synchronized EimJsonWatchService getInstance()
	{
		if (INSTANCE == null)
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
		return INSTANCE;
	}

	public static synchronized void restartAfterEimIdfPathChange()
	{
		EimJsonWatchService old = INSTANCE;
		List<EimJsonChangeListener> toCopy = (old == null) ? new ArrayList<>() : new ArrayList<>(old.eimJsonChangeListeners);
		if (old != null)
		{
			old.requestStop();
		}
		INSTANCE = null;
		try
		{
			EimJsonWatchService fresh = new EimJsonWatchService();
			for (EimJsonChangeListener l : toCopy)
			{
				fresh.addEimJsonChangeListener(l);
			}
			INSTANCE = fresh;
		}
		catch (IOException e)
		{
			Logger.log("Failed to restart EimJsonWatchService"); //$NON-NLS-1$
			Logger.log(e);
		}
	}

	private void requestStop()
	{
		running = false;
		try
		{
			watchService.close();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		try
		{
			join(10_000);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
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
		EimJsonWatchService watch = getInstance();
		if (watch == null)
		{
			try
			{
				task.run();
			}
			catch (Exception e)
			{
				Logger.log(e);
			}
			return;
		}
		boolean wasPaused = watch.paused;
		watch.pauseListeners();

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
				watch.unpauseListeners();
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
					try
					{
						Instant currentModified = Files.getLastModifiedTime(fullPath).toInstant()
								.truncatedTo(ChronoUnit.SECONDS);

						if (lastModifiedTime != null && currentModified.compareTo(lastModifiedTime) <= 0)
						{
							continue; // skip duplicate or same-second event
						}

						lastModifiedTime = currentModified;

						for (EimJsonChangeListener listener : eimJsonChangeListeners)
						{
							listener.onJsonFileChanged(fullPath, paused);
						}
					}
					catch (IOException e)
					{
						Logger.log(e);
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
