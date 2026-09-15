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
	private String buildDirectory;

	public SDKConfigJsonReader(IProject project)
	{
		this(project, null);
	}

	/**
	 * @param project        project owning the configuration
	 * @param buildDirectory build directory to read from, or <code>null</code> to resolve the project's active one
	 */
	public SDKConfigJsonReader(IProject project, String buildDirectory)
	{
		this.project = project;
		this.buildDirectory = buildDirectory;
	}

	/**
	 * Load build/config/sdkconfig.json file and look for a specified key
	 * 
	 * @param key
	 * @return value for a specified key. Null if key is not found or file not found
	 */
	public String getValue(String key)
	{
		try
		{
			JSONObject jsonObj = read();
			if (jsonObj != null)
			{
				return String.valueOf(jsonObj.get(key));
			}
		}
		catch (Exception e)
		{
			Logger.log(e, true);
		}
		return null;
	}

	protected JSONObject read() throws Exception
	{
		String sdkconfigJsonPath = StringUtil.isEmpty(buildDirectory)
				? new SDKConfigUtil().getSDKConfigJsonFilePath(project)
				: new SDKConfigUtil().getSDKConfigJsonFilePath(buildDirectory);
		if (!new File(sdkconfigJsonPath).exists())
		{
			Logger.log(MessageFormat.format("sdkconfig.json file could not find {0}", sdkconfigJsonPath)); //$NON-NLS-1$
			return null;
		}

		JSONParser parser = new JSONParser();
		FileReader reader = new FileReader(sdkconfigJsonPath);
		try
		{
			return (JSONObject) parser.parse(reader);
		}
		catch (
				IOException
				| ParseException e)
		{
			throw new Exception(e);
		}
		finally 
		{
			reader.close();
		}
	}

}
