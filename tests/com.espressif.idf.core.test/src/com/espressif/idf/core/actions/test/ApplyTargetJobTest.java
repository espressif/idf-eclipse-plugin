/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.actions.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.espressif.idf.core.actions.test.TestableApplyTargetJob.TestableApplyTargetJobException;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.util.StringUtil;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ApplyTargetJobTest
{
	private static final String TARGET_NAME_ATTR = "targetNameAttr"; //$NON-NLS-1$
	private static final String EXPECTED_TARGET_NAME = "TestTarget"; //$NON-NLS-1$
	private static final String NOT_EXISTING_TARGET_NAME = "NotExisting"; //$NON-NLS-1$
	private static final String SERIAL_PORT = "COM1"; //$NON-NLS-1$
	@Mock
	private ILaunchBarManager launchBarManager;

	@Mock
	private ILaunchTargetManager targetManager;

	@Mock
	private IWizard wizard;

	@Mock
	private IProgressMonitor monitor;

	private TestableApplyTargetJob applyTargetJob;

	@BeforeEach
	void setUp()
	{
		MockitoAnnotations.openMocks(this);
		applyTargetJob = new TestableApplyTargetJob(launchBarManager, targetManager, TARGET_NAME_ATTR, wizard);
		monitor = mock(IProgressMonitor.class);
	}

	@Test
	void run_whenActiveLaunchConfigurationIsNull_shouldReturnCancelStatus() throws CoreException
	{
		when(launchBarManager.getActiveLaunchConfiguration()).thenReturn(null);

		IStatus result = applyTargetJob.run(monitor);

		assertEquals(Status.CANCEL_STATUS, result);
	}

	@Test
	void run_whenTargetNameIsEmpty_shouldReturnOkStatus() throws CoreException
	{
		when(launchBarManager.getActiveLaunchConfiguration()).thenReturn(mock(ILaunchConfiguration.class));
		when(launchBarManager.getActiveLaunchConfiguration().getAttribute(eq(TARGET_NAME_ATTR), anyString()))
				.thenReturn(""); //$NON-NLS-1$

		IStatus result = applyTargetJob.run(monitor);

		assertEquals(Status.OK_STATUS, result);
	}

	@Test
	void run_whenSuitableTargetFound_shouldSetActiveLaunchTarget() throws CoreException
	{
		// Available ILaunchTargets in provided by mocked target manager
		ILaunchTarget target = Mockito.mock(ILaunchTarget.class);
		when(target.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY))
				.thenReturn(EXPECTED_TARGET_NAME);
		when(target.getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY)).thenReturn(SERIAL_PORT);
		when(targetManager.getLaunchTargetsOfType(Mockito.anyString())).thenReturn(new ILaunchTarget[] { target });

		// Mocked LaunchBarManager has the active launch target with expected target name
		when(launchBarManager.getActiveLaunchTarget()).thenReturn(target);
		when(launchBarManager.getActiveLaunchConfiguration()).thenReturn(mock(ILaunchConfiguration.class));
		when(launchBarManager.getActiveLaunchConfiguration().getAttribute(eq(TARGET_NAME_ATTR), anyString()))
				.thenReturn(EXPECTED_TARGET_NAME);

		IStatus result = applyTargetJob.run(monitor);

		assertEquals(Status.OK_STATUS, result);
		verify(launchBarManager).setActiveLaunchTarget(target);
	}

	@Test
	void run_whenNoSuitableTargetFound_shouldShowNoTargetMessage() throws CoreException
	{
		ILaunchTarget target = mock(ILaunchTarget.class);
		ILaunchConfiguration launchConfiguration = mock(ILaunchConfiguration.class);

		when(target.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY))
				.thenReturn(EXPECTED_TARGET_NAME);
		when(target.getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY)).thenReturn(SERIAL_PORT);
		when(launchConfiguration.getAttribute(eq(TARGET_NAME_ATTR), anyString())).thenReturn(NOT_EXISTING_TARGET_NAME);
		when(launchBarManager.getActiveLaunchConfiguration()).thenReturn(launchConfiguration);
		when(launchBarManager.getActiveLaunchTarget()).thenReturn(target);
		when(targetManager.getLaunchTargetsOfType(Mockito.anyString())).thenReturn(new ILaunchTarget[] { target });

		TestableApplyTargetJobException exception = assertThrows(
				TestableApplyTargetJob.TestableApplyTargetJobException.class, () -> applyTargetJob.run(monitor));
		assertEquals(exception.getMessage(), NOT_EXISTING_TARGET_NAME);
		verify(launchBarManager, never()).setActiveLaunchTarget(any());
	}

	@Test
	void run_whenCoreExceptionThrown_shouldReturnCancelStatus() throws CoreException
	{
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		when(launchBarManager.getActiveLaunchConfiguration()).thenThrow(new CoreException(Status.CANCEL_STATUS));

		IStatus result = applyTargetJob.run(monitor);

		assertEquals(Status.CANCEL_STATUS, result);
	}

}