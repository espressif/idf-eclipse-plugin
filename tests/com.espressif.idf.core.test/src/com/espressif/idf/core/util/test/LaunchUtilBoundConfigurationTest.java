package com.espressif.idf.core.util.test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.util.LaunchUtil;

class LaunchUtilBoundConfigurationTest
{
	@Test
	void getBoundConfigurationReturnsNamedRunConfiguration() throws Exception
	{
		ILaunchManager launchManager = mock(ILaunchManager.class);
		ILaunchConfigurationType runType = mock(ILaunchConfigurationType.class);
		ILaunchConfiguration debugConfiguration = mock(ILaunchConfiguration.class);
		ILaunchConfiguration expected = mock(ILaunchConfiguration.class);
		ILaunchConfiguration other = mock(ILaunchConfiguration.class);
		when(debugConfiguration.getAttribute(IDFLaunchConstants.ATTR_LAUNCH_CONFIGURATION_NAME, "")) //$NON-NLS-1$
				.thenReturn("release"); //$NON-NLS-1$
		when(expected.getName()).thenReturn("release"); //$NON-NLS-1$
		when(other.getName()).thenReturn("debug"); //$NON-NLS-1$
		when(launchManager.getLaunchConfigurationType(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE)).thenReturn(runType);
		when(launchManager.getLaunchConfigurations(runType))
				.thenReturn(new ILaunchConfiguration[] { other, expected });

		assertSame(expected, new LaunchUtil(launchManager).getBoundConfiguration(debugConfiguration));
	}

	@Test
	void getBoundConfigurationFallsBackOnlyToConfigurationFromSameProject() throws Exception
	{
		ILaunchManager launchManager = mock(ILaunchManager.class);
		ILaunchConfigurationType runType = mock(ILaunchConfigurationType.class);
		ILaunchConfiguration debugConfiguration = mock(ILaunchConfiguration.class);
		ILaunchConfiguration expected = mock(ILaunchConfiguration.class);
		ILaunchConfiguration other = mock(ILaunchConfiguration.class);
		IProject project = mock(IProject.class);
		IProject otherProject = mock(IProject.class);
		when(debugConfiguration.getAttribute(IDFLaunchConstants.ATTR_LAUNCH_CONFIGURATION_NAME, "")) //$NON-NLS-1$
				.thenReturn("missing"); //$NON-NLS-1$
		when(expected.getName()).thenReturn("release"); //$NON-NLS-1$
		when(other.getName()).thenReturn("debug"); //$NON-NLS-1$
		when(launchManager.getLaunchConfigurationType(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE)).thenReturn(runType);
		when(launchManager.getLaunchConfigurations(runType))
				.thenReturn(new ILaunchConfiguration[] { other, expected });

		mapToProject(debugConfiguration, project);
		mapToProject(other, otherProject);
		mapToProject(expected, project);

		assertSame(expected, new LaunchUtil(launchManager).getBoundConfiguration(debugConfiguration));
	}

	@Test
	void getBoundConfigurationReturnsDebugConfigurationWhenNoProjectRunConfigurationExists() throws Exception
	{
		ILaunchManager launchManager = mock(ILaunchManager.class);
		ILaunchConfigurationType runType = mock(ILaunchConfigurationType.class);
		ILaunchConfiguration debugConfiguration = mock(ILaunchConfiguration.class);
		when(debugConfiguration.getAttribute(IDFLaunchConstants.ATTR_LAUNCH_CONFIGURATION_NAME, "")) //$NON-NLS-1$
				.thenReturn("missing"); //$NON-NLS-1$
		when(launchManager.getLaunchConfigurationType(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE)).thenReturn(runType);
		when(launchManager.getLaunchConfigurations(runType)).thenReturn(new ILaunchConfiguration[0]);

		assertSame(debugConfiguration, new LaunchUtil(launchManager).getBoundConfiguration(debugConfiguration));
	}

	@Test
	void getBoundConfigurationIgnoresRunConfigurationWithoutMappedResources() throws Exception
	{
		ILaunchManager launchManager = mock(ILaunchManager.class);
		ILaunchConfigurationType runType = mock(ILaunchConfigurationType.class);
		ILaunchConfiguration debugConfiguration = mock(ILaunchConfiguration.class);
		ILaunchConfiguration unmapped = mock(ILaunchConfiguration.class);
		ILaunchConfiguration expected = mock(ILaunchConfiguration.class);
		IProject project = mock(IProject.class);
		when(debugConfiguration.getAttribute(IDFLaunchConstants.ATTR_LAUNCH_CONFIGURATION_NAME, "")) //$NON-NLS-1$
				.thenReturn("missing"); //$NON-NLS-1$
		when(unmapped.getName()).thenReturn("unmapped"); //$NON-NLS-1$
		when(expected.getName()).thenReturn("release"); //$NON-NLS-1$
		when(launchManager.getLaunchConfigurationType(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE)).thenReturn(runType);
		when(launchManager.getLaunchConfigurations(runType))
				.thenReturn(new ILaunchConfiguration[] { unmapped, expected });
		mapToProject(debugConfiguration, project);
		mapToProject(expected, project);

		assertSame(expected, new LaunchUtil(launchManager).getBoundConfiguration(debugConfiguration));
	}

	private static void mapToProject(ILaunchConfiguration configuration, IProject project) throws Exception
	{
		IResource resource = mock(IResource.class);
		when(resource.getProject()).thenReturn(project);
		when(configuration.getMappedResources()).thenReturn(new IResource[] { resource });
	}
}
