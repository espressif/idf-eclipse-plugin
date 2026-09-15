/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.util.LaunchUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * Resolves the build directory represented by the active launch configuration.
 */
public final class BuildDirectoryResolver
{
	private BuildDirectoryResolver()
	{
	}

	/**
	 * Resolves the build directory for a project using its active launch configuration.
	 *
	 * @param project project whose build directory should be resolved
	 * @return absolute build directory path
	 * @throws CoreException if the launch configuration or project properties cannot be read
	 */
	public static IPath resolve(IProject project) throws CoreException
	{
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		if (launchBarManager != null)
		{
			ILaunchConfiguration configuration = launchBarManager.getActiveLaunchConfiguration();
			if (configuration != null)
			{
				ILaunchConfiguration buildConfiguration = resolveBuildConfiguration(configuration);
				if (belongsToProject(buildConfiguration, project))
				{
					return resolve(project, buildConfiguration);
				}
			}
		}

		return resolveLegacyOrDefault(project);
	}

	/**
	 * Resolves the build directory from a specific launch configuration.
	 *
	 * @param project       project used to resolve relative paths
	 * @param configuration launch configuration containing the build folder attribute
	 * @return absolute build directory path
	 * @throws CoreException if the configuration or project properties cannot be read
	 */
	public static IPath resolve(IProject project, ILaunchConfiguration configuration) throws CoreException
	{
		ILaunchConfiguration buildConfiguration = resolveBuildConfiguration(configuration);
		if (!belongsToProject(buildConfiguration, project))
		{
			return resolveLegacyOrDefault(project);
		}

		String buildFolder = buildConfiguration.getAttribute(IDFLaunchConstants.BUILD_FOLDER_PATH, StringUtil.EMPTY);
		return resolvePath(project, buildFolder);
	}

	private static ILaunchConfiguration resolveBuildConfiguration(ILaunchConfiguration configuration)
			throws CoreException
	{
		if (configuration.getType().getIdentifier().equals(IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE))
		{
			return new LaunchUtil(DebugPlugin.getDefault().getLaunchManager()).getBoundConfiguration(configuration);
		}
		return configuration;
	}

	private static boolean belongsToProject(ILaunchConfiguration configuration, IProject project) throws CoreException
	{
		if (project == null || configuration == null)
		{
			return false;
		}

		IProject mappedProject = LaunchUtil.getMappedProject(configuration);
		if (mappedProject != null)
		{
			return project.equals(mappedProject);
		}

		// A configuration may carry only the project name, for instance while the Launch Bar is switching targets.
		// Without this the resolver would silently fall back to the default build folder.
		return project.getName().equals(
				configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, StringUtil.EMPTY));
	}

	private static IPath resolvePath(IProject project, String buildFolder)
	{
		String normalizedBuildFolder = StringUtil.isEmpty(buildFolder) || buildFolder.isBlank()
				? IDFConstants.BUILD_FOLDER
				: buildFolder.trim();
		IPath path = Path.fromOSString(normalizedBuildFolder);
		return path.isAbsolute() ? path : project.getLocation().append(path);
	}

	private static IPath resolveLegacyOrDefault(IProject project) throws CoreException
	{
		String legacyBuildDirectory = project
				.getPersistentProperty(new QualifiedName(IDFCorePlugin.PLUGIN_ID, IDFConstants.BUILD_DIR_PROPERTY));
		if (StringUtil.isEmpty(legacyBuildDirectory))
		{
			return resolvePath(project, IDFConstants.BUILD_FOLDER);
		}

		IPath legacyPath = resolvePath(project, legacyBuildDirectory);
		return isStaleAfterRename(project, legacyPath) ? resolvePath(project, IDFConstants.BUILD_FOLDER) : legacyPath;
	}

	/**
	 * The legacy property stores an absolute path, so a value captured before a project rename still points inside
	 * the old project folder. Such a path must not win over the project's current default build folder (IEP-1521).
	 */
	private static boolean isStaleAfterRename(IProject project, IPath legacyPath)
	{
		IPath projectLocation = project.getLocation();
		return projectLocation != null && !projectLocation.isPrefixOf(legacyPath) && !legacyPath.toFile().exists();
	}
}
