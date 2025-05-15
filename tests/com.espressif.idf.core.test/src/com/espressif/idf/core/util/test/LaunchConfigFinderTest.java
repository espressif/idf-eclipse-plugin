/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.util.LaunchUtil;

public class LaunchConfigFinderTest
{
	@Mock
	private ILaunchManager launchManager;
	@Mock
	private ILaunchConfiguration launchConfiguration;
	@Mock
	private IProject project;
	@Mock
	private ILaunchDescriptor launchDescriptor;

	@Mock
	private ILaunchConfigurationType launchConfigType;

	private LaunchUtil launchConfigFinder;

	@BeforeEach
	public void setUp()
	{
		this.launchManager = Mockito.mock(ILaunchManager.class);
		this.launchConfiguration = Mockito.mock(ILaunchConfiguration.class);
		this.launchConfigType = Mockito.mock(ILaunchConfigurationType.class);
		this.launchDescriptor = Mockito.mock(ILaunchDescriptor.class);
		this.project = Mockito.mock(IProject.class);
		launchConfigFinder = new LaunchUtil(launchManager);
	}

	@Test
	public void testFindAppropriateDebugConfig() throws CoreException
	{
		when(project.getProject()).thenReturn(project);
		when(launchManager.getLaunchConfigurations()).thenReturn(new ILaunchConfiguration[] { launchConfiguration });
		when(launchConfiguration.getMappedResources()).thenReturn(new IProject[] { project });
		when(launchConfiguration.getType()).thenReturn(launchConfigType);
		when(launchConfiguration.getType().getIdentifier()).thenReturn(IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE);
		when(launchDescriptor.getAdapter(IProject.class)).thenReturn(project);

		ILaunchConfiguration result = launchConfigFinder.findAppropriateLaunchConfig(launchDescriptor,
				IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE);

		assertEquals(launchConfiguration, result);
	}

	@Test
	public void testFindExistingLaunchConfiguration() throws CoreException
	{
		when(project.getProject()).thenReturn(project);
		when(launchManager.getLaunchConfigurations()).thenReturn(new ILaunchConfiguration[] { launchConfiguration });
		when(launchConfiguration.getMappedResources()).thenReturn(new IProject[] { project });
		when(launchConfiguration.getType()).thenReturn(launchConfigType);
		when(launchConfiguration.getType().getIdentifier()).thenReturn(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE);
		when(launchDescriptor.getAdapter(IProject.class)).thenReturn(project);
		when(launchConfiguration.getName()).thenReturn("name");
		when(launchDescriptor.getName()).thenReturn("name");

		ILaunchConfiguration result = launchConfigFinder.findAppropriateLaunchConfig(launchDescriptor,
				IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE);

		assertEquals(launchConfiguration, result);
	}

	@Test
	public void testFindAppropriateDebugConfig_NoMappedResources() throws CoreException
	{
		when(project.getProject()).thenReturn(project);
		when(launchManager.getLaunchConfigurations()).thenReturn(new ILaunchConfiguration[] { launchConfiguration });
		when(launchConfiguration.getMappedResources()).thenReturn(null);
		when(launchDescriptor.getAdapter(IProject.class)).thenReturn(project);

		ILaunchConfiguration result = launchConfigFinder.findAppropriateLaunchConfig(launchDescriptor,
				IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE);

		assertEquals(null, result);
	}

	@Test
	public void testFindAppropriateDebugConfig_MappedResourcesAreEmpty() throws CoreException
	{
		when(project.getProject()).thenReturn(project);
		when(launchManager.getLaunchConfigurations()).thenReturn(new ILaunchConfiguration[] { launchConfiguration });
		when(launchConfiguration.getMappedResources()).thenReturn(new IProject[] {});
		when(launchDescriptor.getAdapter(IProject.class)).thenReturn(project);

		ILaunchConfiguration result = launchConfigFinder.findAppropriateLaunchConfig(launchDescriptor,
				IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE);

		assertEquals(null, result);
	}

	@Test
	public void testFindAppropriateDebugConfig_NoMatchingType() throws CoreException
	{
		when(project.getProject()).thenReturn(project);
		when(launchManager.getLaunchConfigurations()).thenReturn(new ILaunchConfiguration[] { launchConfiguration });
		when(launchConfiguration.getMappedResources()).thenReturn(new IProject[] { project });
		when(launchConfiguration.getType()).thenReturn(launchConfigType);
		when(launchConfigType.getIdentifier()).thenReturn("notDebugType");
		when(launchDescriptor.getAdapter(IProject.class)).thenReturn(project);

		ILaunchConfiguration result = launchConfigFinder.findAppropriateLaunchConfig(launchDescriptor,
				IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE);

		assertEquals(null, result);
	}

	@Test
	public void testFindExistingLaunchConfiguration_NoMatchingName() throws CoreException
	{
		when(project.getProject()).thenReturn(project);
		when(launchManager.getLaunchConfigurations()).thenReturn(new ILaunchConfiguration[] { launchConfiguration });
		when(launchConfiguration.getMappedResources()).thenReturn(new IProject[] { project });
		when(launchConfiguration.getName()).thenReturn("anotherName");
		when(launchDescriptor.getName()).thenReturn("name");
		when(launchConfiguration.getType()).thenReturn(launchConfigType);
		when(launchConfiguration.getType().getIdentifier()).thenReturn(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE);

		ILaunchConfiguration result = launchConfigFinder.findAppropriateLaunchConfig(launchDescriptor,
				IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE);

		assertEquals(null, result);
	}
}
