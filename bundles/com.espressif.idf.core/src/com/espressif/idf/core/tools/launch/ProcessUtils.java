/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.launch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for process-related operations
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public final class ProcessUtils
{
	private ProcessUtils()
	{
		// utility
	}

	public static String readAll(InputStream in) throws IOException
	{
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
		{
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
			{
				sb.append(line).append('\n');
			}
			return sb.toString().trim();
		}
	}

	public static Long parseFirstLongLine(String output)
	{
		if (output == null || output.isEmpty())
			return null;

		for (String line : output.split("\n")) //$NON-NLS-1$
		{
			String t = line.trim();
			if (t.matches("\\d+")) //$NON-NLS-1$
			{
				return Long.valueOf(t);
			}
		}
		return null;
	}

	public static String bashSingleQuote(String input)
	{
		return "'" + input.replace("'", "'\"'\"'") + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
