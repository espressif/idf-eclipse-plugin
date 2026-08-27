/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * Helpers for launch configuration attributes. Missing keys and blank strings are treated as "use the default".
 */
public final class LaunchAttributes
{
	private LaunchAttributes()
	{
	}

	public static String getString(ILaunchConfiguration configuration, String key, String defaultValue)
			throws CoreException
	{
		String value = configuration.getAttribute(key, defaultValue);
		return StringUtil.isEmpty(value) ? defaultValue : value;
	}

	/**
	 * Returns the stored string as shown in the editor. Missing keys are empty, not substituted with a default.
	 */
	public static String getStoredString(ILaunchConfiguration configuration, String key) throws CoreException
	{
		if (!configuration.hasAttribute(key))
		{
			return StringUtil.EMPTY;
		}
		return configuration.getAttribute(key, StringUtil.EMPTY);
	}

	/**
	 * Returns the stored integer as text for the editor. Missing keys are empty, not substituted with a default.
	 */
	public static String getStoredIntText(ILaunchConfiguration configuration, String key) throws CoreException
	{
		if (!configuration.hasAttribute(key))
		{
			return StringUtil.EMPTY;
		}
		return Integer.toString(configuration.getAttribute(key, 0));
	}

	/**
	 * Persists {@code value}, or removes the attribute when it is blank so launch-time defaults apply.
	 */
	public static void setOrClearString(ILaunchConfigurationWorkingCopy configuration, String key, String value)
	{
		if (StringUtil.isEmpty(value))
		{
			configuration.setAttribute(key, (String) null);
		}
		else
		{
			configuration.setAttribute(key, value);
		}
	}

	/**
	 * Persists a parsed integer, or removes the attribute when {@code text} is blank so launch-time defaults apply.
	 */
	public static void setOrClearInt(ILaunchConfigurationWorkingCopy configuration, String key, String text)
	{
		if (StringUtil.isEmpty(text))
		{
			configuration.setAttribute(key, (String) null);
			return;
		}
		configuration.setAttribute(key, Integer.parseInt(text.trim()));
	}

	/**
	 * Writes {@code value} only when the attribute is absent or blank. Does not overwrite a user-set string.
	 */
	public static void setStringIfEmpty(ILaunchConfigurationWorkingCopy configuration, String key, String value)
			throws CoreException
	{
		if (StringUtil.isEmpty(configuration.getAttribute(key, StringUtil.EMPTY)))
		{
			configuration.setAttribute(key, value);
		}
	}

	/**
	 * Writes {@code value} only when the attribute has never been stored. {@code false} is a valid user choice.
	 */
	public static void setIfAbsent(ILaunchConfigurationWorkingCopy configuration, String key, boolean value)
			throws CoreException
	{
		if (!configuration.hasAttribute(key))
		{
			configuration.setAttribute(key, value);
		}
	}

	/**
	 * Writes {@code value} only when the attribute has never been stored. {@code 0} is a valid user choice.
	 */
	public static void setIfAbsent(ILaunchConfigurationWorkingCopy configuration, String key, int value)
			throws CoreException
	{
		if (!configuration.hasAttribute(key))
		{
			configuration.setAttribute(key, value);
		}
	}
}
