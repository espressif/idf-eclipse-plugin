package com.espressif.idf.ui.installcomponents;

import org.eclipse.ui.IStartup;

import com.espressif.idf.core.component.registry.http.ComponentHttpServer;
import com.espressif.idf.core.logging.Logger;

public class ComponentsHttpServerStartup implements IStartup
{

	@Override
	public void earlyStartup()
	{
		ComponentHttpServer server = new ComponentHttpServer();
		try
		{
			server.start();
		}
		catch (Exception e)
		{
			Logger.log("Failed to start Component HTTP Server: " + e.getMessage()); //$NON-NLS-1$
		}
		
	}

}
