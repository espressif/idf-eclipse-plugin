/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.espressif.idf.core.util.LaunchAttributes;
import com.espressif.idf.core.util.StringUtil;

class LaunchAttributesTest
{
	private static final String KEY = "test.key"; //$NON-NLS-1$
	private static final String DEFAULT_VALUE = "default"; //$NON-NLS-1$
	private static final String USER_VALUE = "user"; //$NON-NLS-1$

	private ILaunchConfiguration configuration;
	private ILaunchConfigurationWorkingCopy workingCopy;

	@BeforeEach
	void setUp()
	{
		configuration = Mockito.mock(ILaunchConfiguration.class);
		workingCopy = Mockito.mock(ILaunchConfigurationWorkingCopy.class);
	}

	@Test
	void getString_returnsStoredValueWhenPresent() throws CoreException
	{
		when(configuration.getAttribute(KEY, DEFAULT_VALUE)).thenReturn(USER_VALUE);

		assertEquals(USER_VALUE, LaunchAttributes.getString(configuration, KEY, DEFAULT_VALUE));
	}

	@Test
	void getString_returnsDefaultWhenValueIsBlank() throws CoreException
	{
		when(configuration.getAttribute(KEY, DEFAULT_VALUE)).thenReturn("  "); //$NON-NLS-1$

		assertEquals(DEFAULT_VALUE, LaunchAttributes.getString(configuration, KEY, DEFAULT_VALUE));
	}

	@Test
	void getString_returnsDefaultWhenValueIsEmpty() throws CoreException
	{
		when(configuration.getAttribute(KEY, DEFAULT_VALUE)).thenReturn(StringUtil.EMPTY);

		assertEquals(DEFAULT_VALUE, LaunchAttributes.getString(configuration, KEY, DEFAULT_VALUE));
	}

	@Test
	void setStringIfEmpty_writesWhenAttributeIsMissing() throws CoreException
	{
		when(workingCopy.getAttribute(KEY, StringUtil.EMPTY)).thenReturn(StringUtil.EMPTY);

		LaunchAttributes.setStringIfEmpty(workingCopy, KEY, DEFAULT_VALUE);

		verify(workingCopy).setAttribute(KEY, DEFAULT_VALUE);
	}

	@Test
	void setStringIfEmpty_writesWhenAttributeIsBlank() throws CoreException
	{
		when(workingCopy.getAttribute(KEY, StringUtil.EMPTY)).thenReturn(" "); //$NON-NLS-1$

		LaunchAttributes.setStringIfEmpty(workingCopy, KEY, DEFAULT_VALUE);

		verify(workingCopy).setAttribute(KEY, DEFAULT_VALUE);
	}

	@Test
	void setStringIfEmpty_doesNotOverwriteUserValue() throws CoreException
	{
		when(workingCopy.getAttribute(KEY, StringUtil.EMPTY)).thenReturn(USER_VALUE);

		LaunchAttributes.setStringIfEmpty(workingCopy, KEY, DEFAULT_VALUE);

		verify(workingCopy, never()).setAttribute(eq(KEY), anyString());
	}

	@Test
	void setIfAbsent_writesBooleanWhenMissing() throws CoreException
	{
		when(workingCopy.hasAttribute(KEY)).thenReturn(false);

		LaunchAttributes.setIfAbsent(workingCopy, KEY, true);

		verify(workingCopy).setAttribute(KEY, true);
	}

	@Test
	void setIfAbsent_keepsStoredFalse() throws CoreException
	{
		when(workingCopy.hasAttribute(KEY)).thenReturn(true);

		LaunchAttributes.setIfAbsent(workingCopy, KEY, true);

		verify(workingCopy, never()).setAttribute(eq(KEY), eq(true));
	}

	@Test
	void setIfAbsent_writesIntWhenMissing() throws CoreException
	{
		when(workingCopy.hasAttribute(KEY)).thenReturn(false);

		LaunchAttributes.setIfAbsent(workingCopy, KEY, 3333);

		verify(workingCopy).setAttribute(KEY, 3333);
	}

	@Test
	void setIfAbsent_keepsStoredZero() throws CoreException
	{
		when(workingCopy.hasAttribute(KEY)).thenReturn(true);

		LaunchAttributes.setIfAbsent(workingCopy, KEY, 3333);

		verify(workingCopy, never()).setAttribute(eq(KEY), eq(3333));
	}

	@Test
	void getStoredString_returnsEmptyWhenAttributeIsMissing() throws CoreException
	{
		when(configuration.hasAttribute(KEY)).thenReturn(false);

		assertEquals(StringUtil.EMPTY, LaunchAttributes.getStoredString(configuration, KEY));
	}

	@Test
	void getStoredString_returnsStoredValue() throws CoreException
	{
		when(configuration.hasAttribute(KEY)).thenReturn(true);
		when(configuration.getAttribute(KEY, StringUtil.EMPTY)).thenReturn(USER_VALUE);

		assertEquals(USER_VALUE, LaunchAttributes.getStoredString(configuration, KEY));
	}

	@Test
	void getStoredIntText_returnsEmptyWhenAttributeIsMissing() throws CoreException
	{
		when(configuration.hasAttribute(KEY)).thenReturn(false);

		assertEquals(StringUtil.EMPTY, LaunchAttributes.getStoredIntText(configuration, KEY));
	}

	@Test
	void getStoredIntText_returnsStoredValue() throws CoreException
	{
		when(configuration.hasAttribute(KEY)).thenReturn(true);
		when(configuration.getAttribute(KEY, 0)).thenReturn(3333);

		assertEquals("3333", LaunchAttributes.getStoredIntText(configuration, KEY)); //$NON-NLS-1$
	}

	@Test
	void setOrClearString_removesBlankValue()
	{
		LaunchAttributes.setOrClearString(workingCopy, KEY, "  "); //$NON-NLS-1$

		verify(workingCopy).setAttribute(KEY, (String) null);
	}

	@Test
	void setOrClearString_writesNonEmptyValue()
	{
		LaunchAttributes.setOrClearString(workingCopy, KEY, USER_VALUE);

		verify(workingCopy).setAttribute(KEY, USER_VALUE);
	}

	@Test
	void setOrClearInt_removesBlankValue()
	{
		LaunchAttributes.setOrClearInt(workingCopy, KEY, StringUtil.EMPTY);

		verify(workingCopy).setAttribute(KEY, (String) null);
	}

	@Test
	void setOrClearInt_writesParsedValue()
	{
		LaunchAttributes.setOrClearInt(workingCopy, KEY, "3333"); //$NON-NLS-1$

		verify(workingCopy).setAttribute(KEY, 3333);
	}
}
