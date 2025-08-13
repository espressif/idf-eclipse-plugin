/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents;

import org.eclipse.ui.IStartup;

import com.espressif.idf.core.component.registry.http.ComponentHttpServer;
import com.espressif.idf.core.logging.Logger;

/**
 * Startup class to initialize the Component HTTP Server when Eclipse starts. This server handles requests related to
 * component management in the IDF environment.
 * 
 * @author Ali Azam Rana
 */
public class ComponentsHttpServerStartup implements IStartup
{
	private ComponentHttpServer componentHttpServer;
	private ComponentQueueListenJob componentQueueListenJob;

	@Override
	public void earlyStartup()
	{
		componentHttpServer = new ComponentHttpServer();
		componentQueueListenJob = new ComponentQueueListenJob();
		try
		{
			componentHttpServer.start();
		}
		catch (Exception e)
		{
			Logger.log("Failed to start Component HTTP Server: " + e.getMessage()); //$NON-NLS-1$
			return;
		}
		Logger.log("Component HTTP Server started successfully."); //$NON-NLS-1$ s
		Logger.log("Starting Component Queue Listen Job..."); //$NON-NLS-1$
		componentQueueListenJob.schedule();
		
	}

}