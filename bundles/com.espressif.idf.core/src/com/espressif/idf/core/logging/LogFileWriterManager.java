/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogFileWriterManager
{
	private static final Map<String, PrintWriter> writers = new ConcurrentHashMap<>();

	private LogFileWriterManager()
	{
	}

	public static PrintWriter getWriter(String path, boolean append)
	{
		if (path == null || path.isEmpty())
		{
			return new PrintWriter(Writer.nullWriter());
		}

		return writers.computeIfAbsent(path, p -> {
			try
			{
				File file = new File(p);
				File parent = file.getParentFile();
				if (parent != null && !parent.exists())
				{
					parent.mkdirs();
				}
				if (!file.exists())
				{
					file.createNewFile();
				}
				return new PrintWriter(new BufferedWriter(new FileWriter(file, append)), true);
			}
			catch (IOException e)
			{
				Logger.log(e);
				return new PrintWriter(Writer.nullWriter());
			}
		});
	}

	public static void closeWriter(String path)
	{
		if (path == null || path.isEmpty())
			return;
		PrintWriter writer = writers.remove(path);
		if (writer != null)
		{
			writer.println("=== Session ended at " + LocalDateTime.now() + " ==="); //$NON-NLS-1$ //$NON-NLS-2$
			writer.close();
		}
	}

	public static void closeAll()
	{
		for (PrintWriter writer : writers.values())
		{
			writer.close();
		}
		writers.clear();
	}
}
