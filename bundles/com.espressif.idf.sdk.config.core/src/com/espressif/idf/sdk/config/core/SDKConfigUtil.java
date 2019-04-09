package com.espressif.idf.sdk.config.core;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration2;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.espressif.idf.core.IDFConstants;

public class SDKConfigUtil
{

	/**
	 * @param buildDirectory
	 * @return
	 * @throws Exception 
	 */
	public String getConfigMenuFilePath(IProject project) throws Exception
	{
		File buildDirectory = getBuildDirectory(project);
		if (buildDirectory == null || !buildDirectory.exists())
		{
			throw new Exception(MessageFormat.format("Could not find the build directory {0}", buildDirectory));
		}
		return buildDirectory.getAbsolutePath() + IPath.SEPARATOR + IDFConstants.CONFIG_FOLDER + IPath.SEPARATOR
				+ IDFConstants.KCONFIG_MENUS_JSON;
	}

	/**
	 * Default build directory which used by CDT
	 * 
	 * @return
	 * @throws CoreException
	 */
	public File getBuildDirectory(IProject project) throws CoreException
	{
		assert (project == null || !project.exists()) : "Either project is null or doesn't exist: " + project;

		IBuildConfiguration config = project.getActiveBuildConfig();

		ICBuildConfigurationManager manager = CCorePlugin.getService(ICBuildConfigurationManager.class);
		ICBuildConfiguration coreConfig = manager.getBuildConfiguration(config);
		if (coreConfig == null)
		{
			return null;
		}

		if (coreConfig instanceof ICBuildConfiguration2)
		{
			URI uri = ((ICBuildConfiguration2) coreConfig).getBuildDirectoryURI();
			if (uri != null)
			{
				return new File(uri);
			}
		}
		return null;
	}

}
