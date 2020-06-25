/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ZipUtility
{

	public static final int _bufferSize = 1024000;

	/**
	 * @param zipPath
	 * @param parentDirectory
	 * @return
	 */
	public boolean decompress(File zipPath, File parentDirectory)
	{
		return decompress(zipPath, parentDirectory, _bufferSize);
	}

	/**
	 * @param path
	 * @param parentDirectory
	 * @param bufferSize
	 * @return
	 */
	public boolean decompress(File path, File parentDirectory, int bufferSize)
	{
		try
		{
			ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(path));
			return decompress(zipInputStream, parentDirectory, bufferSize);
		}
		catch (FileNotFoundException e)
		{
			Logger.log(e);
			return false;
		}
	}

	/**
	 * @param zipInputStream
	 * @param parentDirectory
	 * @param bufferSize
	 * @return
	 */
	private boolean decompress(ZipInputStream zipInputStream, File parentDirectory, int bufferSize)
	{
		try
		{
			try
			{
				ZipEntry zipEntry = null;
				while (null != (zipEntry = zipInputStream.getNextEntry()))
				{
					if (zipEntry.isDirectory())
					{
						File directory = new File(parentDirectory, zipEntry.getName());
						if (!directory.mkdirs())
						{
							zipInputStream.closeEntry();
							return false;
						}
					}
					else
					{
						File file = new File(parentDirectory, zipEntry.getName());
						File directory = new File(file.getParent());
						if (!directory.exists() && !directory.mkdirs())
						{
							zipInputStream.closeEntry();
							return false;
						}

						FileOutputStream fileOutputStream = new FileOutputStream(file);

						byte[] buf = new byte[bufferSize];
						BufferedInputStream bufferedInputStream = new BufferedInputStream(zipInputStream);

						int count;
						while (-1 != (count = bufferedInputStream.read(buf, 0, bufferSize)))
							fileOutputStream.write(buf, 0, count);

						fileOutputStream.close();

					}

					zipInputStream.closeEntry();
				}
			} finally
			{
				zipInputStream.close();
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
			return false;
		}

		return true;
	}

}
