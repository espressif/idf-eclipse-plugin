/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.watcher;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;

/**
 * eim_idf.json watch service
 * The service will only watch for changes subsequent changes
 * or any handling must be done by the listeners to this service 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class EimJsonWatchService extends Thread
{
	private static EimJsonWatchService INSTANCE;
	private WatchService watchService;
	private WatchKey watchKey;
	private Path watchDirectoryPath;
	private List<EimJsonChangeListener> eimJsonChangeListeners;
	private volatile boolean running = true;
	
	private EimJsonWatchService() throws Exception
	{
		String directoryPathString = Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_DIR
				: EimConstants.EIM_POSIX_DIR;
		watchService = FileSystems.getDefault().newWatchService();
		watchDirectoryPath = Paths.get(directoryPathString);
		watchDirectoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		Logger.log("Watcher added to the directory: " + directoryPathString); //$NON-NLS-1$

	}

	public static EimJsonWatchService getInstance() throws Exception
	{
		if (INSTANCE == null)
		{
			INSTANCE = new EimJsonWatchService();
		}

		return INSTANCE;
	}
	
	public void addEimJsonChangeListener(EimJsonChangeListener eimJsonChangeListener)
	{
		if (eimJsonChangeListeners == null)
		{
			eimJsonChangeListeners = new LinkedList<EimJsonChangeListener>();
		}
		eimJsonChangeListeners.add(eimJsonChangeListener);
	}
	
	public void removeAllListeners()
	{
		if (eimJsonChangeListeners != null)
		{
			eimJsonChangeListeners.clear();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		while (running)
		{
			try
			{
				watchKey = watchService.take();
			}
			catch (InterruptedException e)
			{
				Logger.log("Watch Service Interrupted"); //$NON-NLS-1$
				Logger.log(e);
				return;
			}
			
			for (WatchEvent<?> event: watchKey.pollEvents())
			{
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileNamePath = ev.context();
                if (!fileNamePath.toString().contains(EimConstants.EIM_JSON))
                {
                	continue;
                }
                
                // changes to the file detected
                // call the listeners to the service
                for (EimJsonChangeListener eimJsonChangeListener : eimJsonChangeListeners)
                {
                	eimJsonChangeListener.onJsonFileChanged(watchDirectoryPath.resolve(fileNamePath));
                }
			}
		}
	}
	
	@Override
	public void interrupt()
	{
		this.running = false;
		super.interrupt();
	}

	public void shutdown()
	{
		running = false;
		this.interrupt();
	}
}
