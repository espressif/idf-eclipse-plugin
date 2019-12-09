/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ConfigServerManager
{
	public static ConfigServerManager INSTANCE = new ConfigServerManager();
	private Map<IProject, JsonConfigServer> jsonServermap = new HashMap<IProject, JsonConfigServer>();

	public void clearAll()
	{
		jsonServermap.clear();
	}

	public void deleteServer(IProject project)
	{
		jsonServermap.remove(project);
	}

	/**
	 * @param project
	 * @return
	 * @throws IOException 
	 */
	public synchronized JsonConfigServer getServer(IProject project) throws IOException
	{
		JsonConfigServer jsonConfigServer = jsonServermap.get(project);
		if (jsonConfigServer == null)
		{
			jsonConfigServer = new JsonConfigServer(project);
			jsonServermap.put(project, jsonConfigServer);
			jsonConfigServer.start();
		}

		return jsonConfigServer;
	}
}
