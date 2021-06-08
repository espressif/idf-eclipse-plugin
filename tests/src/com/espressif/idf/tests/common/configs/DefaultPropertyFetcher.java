package com.espressif.idf.tests.common.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DefaultPropertyFetcher
{
	public static long getLongPropertyValue(String propertyName, long defaultValue) throws IOException
	{
		Properties properties = new Properties();
		InputStream inputStream = DefaultPropertyFetcher.class.getClassLoader().getResourceAsStream(IDefaultConfigConstants.DEFAULT_CONFIG_PROPERTY_FILE);
		
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
}
