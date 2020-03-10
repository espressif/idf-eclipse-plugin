package com.espressif.idf.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.espressif.idf.core.logging.Logger;

public class OutputStreamThread extends Thread
{
	private OutputStream out;
	private String charsetName;
	private String content;

	public OutputStreamThread(OutputStream out, String content, String charsetName)
	{
		if (out == null || content == null)
		{
			throw new IllegalArgumentException("The OutputStream and the content cannot be null!"); //$NON-NLS-1$
		}
		this.out = out;
		this.content = content;
		this.charsetName = charsetName;
	}

	public void run()
	{
		OutputStreamWriter osr = null;
		try
		{
			if (charsetName != null)
			{
				osr = new OutputStreamWriter(out, charsetName);
			}
			else
			{
				osr = new OutputStreamWriter(out);
			}

			BufferedWriter br = new BufferedWriter(osr);
			br.write(content);
			br.flush();

		}
		catch (IOException e)
		{
			Logger.log(e);
		} finally
		{
			if (osr != null)
			{
				try
				{
					osr.close();
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}
