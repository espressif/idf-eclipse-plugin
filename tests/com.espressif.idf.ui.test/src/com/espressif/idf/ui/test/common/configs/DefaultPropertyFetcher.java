/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.common.configs;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;

/**
 * The class to fetch the properties from the default config file specified in {@link IDefaultConfigConstants}
 * 
 * @author Ali Azam Rana
 *
 */
public class DefaultPropertyFetcher
{
	/**
	 * Gets the long property value from the default config file
	 * ({@link IDefaultConfigConstants#DEFAULT_CONFIG_PROPERTY_FILE_LINUX}) and if not found returns the default value
	 * 
	 * @param propertyName property name to look for
	 * @param defaultValue the default value to return if property not present in file
	 * @return the long value of the property
	 * @throws IOException if the property file is not found
	 */
	public static long getLongPropertyValue(String propertyName, long defaultValue) throws IOException
	{
		Properties properties = new Properties();
		InputStream inputStream = DefaultPropertyFetcher.class.getClassLoader()
				.getResourceAsStream(getPropertyFile());

		if (inputStream == null)
		{
			return defaultValue;
		}

		properties.load(inputStream);
		if (properties.containsKey(propertyName))
		{
			return Long.valueOf(properties.getProperty(propertyName));
		}

		return defaultValue;
	}

	/**
	 * Gets the String property value from the default config file
	 * ({@link IDefaultConfigConstants#DEFAULT_CONFIG_PROPERTY_FILE_LINUX}) and if not found returns the default value
	 * 
	 * @param propertyName property name to look for
	 * @param defaultValue the default value to return if property not present in file
	 * @return the long value of the property
	 * @throws IOException if the property file is not found
	 */
	public static String getStringPropertyValue(String propertyName, String defaultValue) throws IOException
	{
		Properties properties = new Properties();
		InputStream inputStream = DefaultPropertyFetcher.class.getClassLoader()
				.getResourceAsStream(getPropertyFile());

		if (inputStream == null)
		{
			return defaultValue;
		}

		properties.load(inputStream);
		if (properties.containsKey(propertyName))
		{
			String property = properties.getProperty(propertyName);
			return MessageFormat.format(property, System.getenv("GITHUB_WORKSPACE"));
		}

		return defaultValue;
	}
	
	private static String getPropertyFile()
	{
		if (Platform.getOS().equals(Platform.OS_LINUX))
		{
			return IDefaultConfigConstants.DEFAULT_CONFIG_PROPERTY_FILE_LINUX;			
		}
		
		return IDefaultConfigConstants.DEFAULT_CONFIG_PROPERTY_FILE_WINDOWS;
	}
}
