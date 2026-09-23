package com.espressif.idf.core.build.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.build.BuildDirectoryResolver;
import com.espressif.idf.core.build.IDFLaunchConstants;

class BuildDirectoryResolverTest
{
	@TempDir
	File tempDirectory;

	@Test
	void resolveActiveConfigurationRelativePathOverridesLegacyProjectProperty() throws Exception
	{
		IProject project = mockProject();
		ILaunchConfiguration configuration = mockRunConfiguration("build-release", project); //$NON-NLS-1$
		ILaunchBarManager launchBarManager = mock(ILaunchBarManager.class);
		when(launchBarManager.getActiveLaunchConfiguration()).thenReturn(configuration);

		try (MockedStatic<IDFCorePlugin> plugin = mockStatic(IDFCorePlugin.class))
		{
			plugin.when(() -> IDFCorePlugin.getService(ILaunchBarManager.class)).thenReturn(launchBarManager);

			IPath result = BuildDirectoryResolver.resolve(project);

			assertEquals(project.getLocation().append("build-release"), result); //$NON-NLS-1$
		}
	}

	@Test
	void resolveAbsolutePathPreservesExternalLocation() throws Exception
	{
		IProject project = mockProject();
		String externalBuildDirectory = new File(tempDirectory, "external-build").getAbsolutePath(); //$NON-NLS-1$
		ILaunchConfiguration configuration = mockRunConfiguration(externalBuildDirectory, project);

		assertEquals(Path.fromOSString(externalBuildDirectory), BuildDirectoryResolver.resolve(project, configuration));
	}

	@Test
	void resolveBlankPathUsesProjectBuildDirectory() throws Exception
	{
		IProject project = mockProject();
		ILaunchConfiguration configuration = mockRunConfiguration(" ", project); //$NON-NLS-1$

		assertEquals(project.getLocation().append("build"), BuildDirectoryResolver.resolve(project, configuration)); //$NON-NLS-1$
	}

	@Test
	void resolveConfigurationForAnotherProjectUsesLegacyDirectory() throws Exception
	{
		IProject project = mockProject();
		ILaunchConfiguration configuration = mockRunConfiguration("other-build", mock(IProject.class)); //$NON-NLS-1$

		assertEquals(project.getLocation().append("legacy-build"), //$NON-NLS-1$
				BuildDirectoryResolver.resolve(project, configuration));
	}

	@Test
	void resolveConfigurationWithoutMappedResourcesUsesLegacyDirectory() throws Exception
	{
		IProject project = mockProject();
		ILaunchConfiguration configuration = mockRunConfiguration("unmapped-build", null); //$NON-NLS-1$

		assertEquals(project.getLocation().append("legacy-build"), //$NON-NLS-1$
				BuildDirectoryResolver.resolve(project, configuration));
	}

	@Test
	void resolveIgnoresLegacyDirectoryLeftInsideRenamedProject() throws Exception
	{
		IProject project = mockProject();
		String renamedAwayDirectory = new File(tempDirectory.getParentFile(), "OldProjectName/build") //$NON-NLS-1$
				.getAbsolutePath();
		when(project.getPersistentProperty(any())).thenReturn(renamedAwayDirectory);
		ILaunchConfiguration configuration = mockRunConfiguration("build-release", mock(IProject.class)); //$NON-NLS-1$

		assertEquals(project.getLocation().append("build"), //$NON-NLS-1$
				BuildDirectoryResolver.resolve(project, configuration));
	}

	@Test
	void resolveKeepsLegacyDirectoryThatStillExistsOutsideProject() throws Exception
	{
		IProject project = mockProject();
		File externalDirectory = new File(tempDirectory.getParentFile(), "external-legacy-build"); //$NON-NLS-1$
		externalDirectory.mkdirs();
		when(project.getPersistentProperty(any())).thenReturn(externalDirectory.getAbsolutePath());
		ILaunchConfiguration configuration = mockRunConfiguration("build-release", mock(IProject.class)); //$NON-NLS-1$

		try
		{
			assertEquals(Path.fromOSString(externalDirectory.getAbsolutePath()),
					BuildDirectoryResolver.resolve(project, configuration));
		}
		finally
		{
			externalDirectory.delete();
		}
	}

	private IProject mockProject() throws Exception
	{
		IProject project = mock(IProject.class);
		when(project.getLocation()).thenReturn(Path.fromOSString(tempDirectory.getAbsolutePath()));
		when(project.getPersistentProperty(any())).thenReturn("legacy-build"); //$NON-NLS-1$
		return project;
	}

	/**
	 * @param mappedProject project the configuration is mapped to, or <code>null</code> to simulate a configuration
	 *                      carrying no mapped resource
	 */
	private ILaunchConfiguration mockRunConfiguration(String buildDirectory, IProject mappedProject) throws Exception
	{
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		ILaunchConfigurationType type = mock(ILaunchConfigurationType.class);
		when(configuration.getType()).thenReturn(type);
		when(type.getIdentifier()).thenReturn(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE);
		when(configuration.getAttribute(IDFLaunchConstants.BUILD_FOLDER_PATH, "")).thenReturn(buildDirectory); //$NON-NLS-1$

		if (mappedProject != null)
		{
			IResource mappedResource = mock(IResource.class);
			when(mappedResource.getProject()).thenReturn(mappedProject);
			when(configuration.getMappedResources()).thenReturn(new IResource[] { mappedResource });
		}

		return configuration;
	}
}
