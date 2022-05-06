package com.espressif.idf.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class GenericJsonReader
{
	private String filePath;

	/**
	 * @param filePath AbsolutePath file path for json file
	 */
	public GenericJsonReader(String filePath)
	{
		this.filePath = filePath;
	}

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

	public JSONObject read() throws Exception
	{
		if (!new File(filePath).exists())
		{
			Logger.log(MessageFormat.format("{0} could not find", filePath.toString())); //$NON-NLS-1$
			return null;
		}

		JSONParser parser = new JSONParser();
		BufferedReader breader = null;
		try
		{
			breader = new BufferedReader(new FileReader(new File(filePath)));
			return (JSONObject) parser.parse(breader);

		}
		catch (
				IOException
				| ParseException e)
		{
			throw new Exception(e);
		} finally
		{
			try
			{
				if (breader != null)
				{
					breader.close();
				}
			}
			catch (IOException ex)
			{
				breader = null;
			}
		}
	}

}
