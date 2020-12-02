/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class SDKConfigJsonReader
{
	private IProject project;

	public SDKConfigJsonReader(IProject project)
	{
		this.project = project;
	}

	/**
	 * Load build/config/sdkconfig.json file and look for a specified key
	 * 
	 * @param key
	 * @return value for a specified key. Null if key is found or file not found
	 */
	public String getValue(String key)
	{
		try
		{
			JSONObject jsonObj = read();
			if (jsonObj != null)
			{
				return (String) jsonObj.get(key);
			}

		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}

	protected JSONObject read() throws Exception
	{
		String sdkconfigJsonPath = new SDKConfigUtil().getSDKConfigJsonFilePath(project);
		if (!new File(sdkconfigJsonPath).exists())
		{
			Logger.log(MessageFormat.format("sdkconfig.json file couldn't find {0}", sdkconfigJsonPath));
			return null;
		}

		JSONParser parser = new JSONParser();
		try
		{
			return (JSONObject) parser.parse(new FileReader(sdkconfigJsonPath));

		}
		catch (
				IOException
				| ParseException e)
		{
			throw new Exception(e);
		}
	}

}
