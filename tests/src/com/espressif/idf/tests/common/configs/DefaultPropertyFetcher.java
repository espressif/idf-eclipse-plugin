/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.tests.common.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
	 * ({@link IDefaultConfigConstants#DEFAULT_CONFIG_PROPERTY_FILE}) and if not found returns the default value
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
				.getResourceAsStream(IDefaultConfigConstants.DEFAULT_CONFIG_PROPERTY_FILE);

		if (inputStream != null)
		{
			properties.load(inputStream);
		}

		if (properties.containsKey(propertyName))
		{
			return Long.valueOf(properties.get(propertyName).toString());
		}

		return defaultValue;
	}
	
	/**
	 * Gets the String property value from the default config file
	 * ({@link IDefaultConfigConstants#DEFAULT_CONFIG_PROPERTY_FILE}) and if not found returns the default value
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
				.getResourceAsStream(IDefaultConfigConstants.DEFAULT_CONFIG_PROPERTY_FILE);

		if (inputStream != null)
		{
			properties.load(inputStream);
		}

		if (properties.containsKey(propertyName))
		{
			return properties.get(propertyName).toString();
		}

		return defaultValue;
	}
}
