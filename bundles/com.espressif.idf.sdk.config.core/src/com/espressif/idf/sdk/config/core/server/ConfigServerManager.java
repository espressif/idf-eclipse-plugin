/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.espressif.idf.core.util.IDFUtil;

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

	public void deleteServer(IProject project, IFile file, String buildDirectory)
	{
		ProjectFileMapKey projectFileMapKey = new ProjectFileMapKey(project, file,
				normalizeBuildDirectory(buildDirectory));
		jsonServermap.remove(projectFileMapKey);
	}

	/**
	 * @deprecated Supply the build directory used to start the server.
	 */
	@Deprecated(forRemoval = true)
	public void deleteServer(IProject project, IFile file)
	{
		jsonServermap.keySet().removeIf(key -> key.project.getName().equals(project.getName())
				&& key.file.getLocation().equals(file.getLocation()));
	}

	/**
	 * @param project
	 * @return
	 * @throws IOException 
	 */
	public synchronized JsonConfigServer getServer(final IProject project, final IFile file,
			final String buildDirectory) throws IOException
	{
		String normalizedBuildDirectory = normalizeBuildDirectory(buildDirectory);
		ProjectFileMapKey projectFileMapKey = new ProjectFileMapKey(project, file, normalizedBuildDirectory);

		JsonConfigServer jsonConfigServer = jsonServermap.get(projectFileMapKey);
		if (jsonConfigServer == null)
		{
			jsonConfigServer = new JsonConfigServer(project, file, normalizedBuildDirectory);
			jsonServermap.put(projectFileMapKey, jsonConfigServer);
			jsonConfigServer.start();
			return jsonConfigServer;
		}

		return jsonConfigServer;
	}

	/**
	 * @deprecated Supply an explicit build directory to keep multi-config servers isolated.
	 */
	@Deprecated(forRemoval = true)
	public synchronized JsonConfigServer getServer(final IProject project, final IFile file) throws IOException
	{
		try
		{
			return getServer(project, file, IDFUtil.getBuildDir(project));
		}
		catch (CoreException e)
		{
			throw new IOException(e);
		}
	}

	private static String normalizeBuildDirectory(String buildDirectory)
	{
		return Paths.get(buildDirectory).toAbsolutePath().normalize().toString();
	}

	private class ProjectFileMapKey
	{
		private IProject project;
		private IFile file;
		private String buildDirectory;

		private ProjectFileMapKey(IProject project, IFile file, String buildDirectory)
		{
			this.file = file;
			this.project = project;
			this.buildDirectory = buildDirectory;
		}

		@Override
		public boolean equals(Object object)
		{
			if (this == object)
			{
				return true;
			}
			if (object == null || getClass() != object.getClass())
			{
				return false;
			}
			ProjectFileMapKey that = (ProjectFileMapKey) object;

			return project.getName().equals(that.project.getName())
					&& file.getLocation().equals(that.file.getLocation())
					&& buildDirectory.equals(that.buildDirectory);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(project.getName(), file.getLocation(), buildDirectory);
		}
	}
}
