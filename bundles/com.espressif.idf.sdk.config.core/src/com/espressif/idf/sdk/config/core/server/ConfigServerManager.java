/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ConfigServerManager
{
	public static ConfigServerManager INSTANCE = new ConfigServerManager();
	private Map<ProjectFileMapKey, JsonConfigServer> jsonServermap = new HashMap<ProjectFileMapKey, JsonConfigServer>();

	public void clearAll()
	{
		jsonServermap.clear();
	}

	public void deleteServer(IProject project, IFile file)
	{
		ProjectFileMapKey projectFileMapKey = new ProjectFileMapKey(project, file);
		jsonServermap.remove(projectFileMapKey);
	}

	/**
	 * @param project
	 * @return
	 * @throws IOException 
	 */
	public synchronized JsonConfigServer getServer(final IProject project, final IFile file) throws IOException
	{ 
		ProjectFileMapKey projectFileMapKey = new ProjectFileMapKey(project, file);
		
		JsonConfigServer jsonConfigServer = jsonServermap.get(projectFileMapKey);
		if (jsonConfigServer == null)
		{
			jsonConfigServer = new JsonConfigServer(project, file);
			jsonServermap.put(projectFileMapKey, jsonConfigServer);
			jsonConfigServer.start();
			return jsonConfigServer;
		}

		return jsonConfigServer;
	}
	
	
	private class ProjectFileMapKey
	{
		private IProject project;
		private IFile file;
		
		private ProjectFileMapKey(IProject project, IFile file)
		{
			this.file = file;
			this.project = project;
		}
		
		@Override
	    public boolean equals(Object o) 
		{
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        ProjectFileMapKey that = (ProjectFileMapKey) o;
	        
			return project.getName().equals(that.project.getName())
					&& file.getLocation().equals(that.file.getLocation());
	    }
		
		@Override
		public int hashCode()
		{
			return Objects.hash(project.getName(), file.getLocation());
		}
	}
}
