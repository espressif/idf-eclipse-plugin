package com.espressif.idf.core.util;

import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
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
	private IProject project;
	private String relativeFilePath;

	public GenericJsonReader(IProject project, String relativeFilePath)
	{
		this.project = project;
		this.relativeFilePath = relativeFilePath;
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

	protected JSONObject read() throws Exception
	{
		IFile filePath = project.getFile(new Path(relativeFilePath));
		if (!filePath.exists())
		{
			Logger.log(MessageFormat.format("{0} couldn't find", filePath.toString()));
			return null;
		}

		JSONParser parser = new JSONParser();
		try
		{
			return (JSONObject) parser.parse(new FileReader(filePath.getLocation().toFile()));

		}
		catch (
				IOException
				| ParseException e)
		{
			throw new Exception(e);
		}
	}

}
