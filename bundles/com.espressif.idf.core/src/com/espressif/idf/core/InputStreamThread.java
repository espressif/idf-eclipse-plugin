package com.espressif.idf.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.espressif.idf.core.logging.Logger;

public class InputStreamThread extends Thread
{
	private InputStream input;
	private String charsetName;
	private String newLineSeparator;
	private StringBuilder result;

	public InputStreamThread(InputStream inputStream, String newLineSeparator, String charsetName)
	{
		if (inputStream == null || newLineSeparator == null)
		{
			throw new IllegalArgumentException("The InputStream and the newLineSeparator cannot be null!"); //$NON-NLS-1$
		}
		this.input = inputStream;
		this.charsetName = charsetName;
		this.newLineSeparator = newLineSeparator;
	}

	public String getResult()
	{
		if (result == null)
		{
			return null;
		}
		return result.toString();
	}

	public void run()
	{
		InputStreamReader streamReader = null;
		try
		{
			if (charsetName != null)
			{
				streamReader = new InputStreamReader(input, charsetName);
			}
			else
			{
				streamReader = new InputStreamReader(input);
			}

			BufferedReader br = new BufferedReader(streamReader);
			result = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null)
			{
				result.append(line);
				result.append(newLineSeparator);
			}
			if (result.length() > 0)
			{
				result.deleteCharAt(result.length() - newLineSeparator.length());
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
		} finally
		{
			if (streamReader != null)
			{
				try
				{
					streamReader.close();
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}
