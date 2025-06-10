/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.watcher;

import java.io.IOException;
import java.nio.file.*;
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

	private EimJsonWatchService() throws IOException
	{
		String directoryPathString = Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_DIR
				: EimConstants.EIM_POSIX_DIR;

		watchDirectoryPath = Paths.get(directoryPathString);
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
					for (EimJsonChangeListener listener : eimJsonChangeListeners)
					{
						listener.onJsonFileChanged(fullPath);
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
