/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.test.common.resources;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.espressif.idf.test.common.configs.IDefaultConfigConstants;

/**
 * Default file contents reader reads the files stored in the resources/default-files
 * This will help to maintain the default files and can help assert them in tests
 * @author Ali Azam Rana
 *
 */
public class DefaultFileContentsReader
{
	/**
	 * Gets the contents of a file in the default file directory 
	 * @param fileName file name or full file path can be given
	 * @return string containing the full file contents
	 * @throws IOException
	 */
	public static String getFileContents(String fileName)
	{
		InputStream fileStream = null;
		try
		{
			String filePath = IDefaultConfigConstants.DEFAULT_FILE_DIRECTORY + "/" + fileName;
			fileStream = DefaultFileContentsReader.class.getClassLoader().getResourceAsStream(filePath);
			String fileContents = IOUtils.toString(IOUtils.toByteArray(fileStream), null);
			fileStream.close();
			return fileContents;
		}
		catch (IOException e)
		{
			return StringUtils.EMPTY;
		}
	}
	
}
