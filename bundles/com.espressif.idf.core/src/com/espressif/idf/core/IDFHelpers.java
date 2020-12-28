package com.espressif.idf.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.build.IDFBuildConfiguration;
import com.espressif.idf.core.build.IDFBuildConfigurationProvider;

/**
 * Helper class
 *
 * Contains helper functions.
 */
public final class IDFHelpers
{

	public static class Build
	{

		private static ICBuildConfigurationManager cdtConfigManager = CCorePlugin
				.getService(ICBuildConfigurationManager.class);

		public static IBuildConfiguration getBuildConfiguration(IProject project) throws CoreException
		{
			// Get active build configuration
			IBuildConfiguration buildCfg = project.getActiveBuildConfig();

			/* Check if the active build configuration is of IDF origin */
			if (buildCfg.getName().startsWith(IDFBuildConfigurationProvider.ID))
			{
				return buildCfg;
			}

			// search through build configurations
			IBuildConfiguration[] configs = project.getBuildConfigs();
			for (IBuildConfiguration icb : configs)
			{
				if (icb.getName().startsWith(IDFBuildConfigurationProvider.ID))
				{
					return icb;
				}
			}

			/*
			 * IDF build configuration is not in the list. This means the project is not create correctly. So throw
			 * exception.
			 */
			throw new CoreException(IDFCorePlugin.errorStatus("Project was not created correctly.",
					new RuntimeException("No IDF build configuration in project.")));
		}

		public static IDFBuildConfiguration getIDFBuildConfiguration(IProject project) throws CoreException
		{
			IBuildConfiguration buildCfg = getBuildConfiguration(project);
			ICBuildConfiguration appBuildCfg = cdtConfigManager.getBuildConfiguration(buildCfg);
			if (appBuildCfg instanceof IDFBuildConfiguration)
			{
				return (IDFBuildConfiguration) appBuildCfg;
			}

			throw new CoreException(IDFCorePlugin.errorStatus("Project was not created correctly.",
					new RuntimeException("No IDF build configuration in project.")));
		}

	}

	public static class Launch
	{

		public static IDFBuildConfiguration getIDFBuildConfiguration(IProject project) throws CoreException
		{
			return Build.getIDFBuildConfiguration(project);
		}

		public static IDFBuildConfiguration getIDFBuildConfiguration(IProject project, String mode,
				ILaunchTarget target, IProgressMonitor monitor) throws CoreException
		{
			return getIDFBuildConfiguration(project);
		}

	}

}
