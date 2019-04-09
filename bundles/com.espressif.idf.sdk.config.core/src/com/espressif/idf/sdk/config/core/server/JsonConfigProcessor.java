package com.espressif.idf.sdk.config.core.server;

public class JsonConfigProcessor
{

	/**
	 * @param jsonConfigOp output from idf.py menuconfig
	 * @return extracted json content
	 */
	public String getInitialOutput(String jsonConfigOp)
	{
		int startIndex = jsonConfigOp.indexOf("{\"ranges\":"); //$NON-NLS-1$
		if (startIndex != -1)
		{
			return jsonConfigOp.substring(startIndex);
		}

		return null;

	}

}
