package com.espressif.idf.tests.common.resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.espressif.idf.tests.common.configs.IDefaultConfigConstants;

/**
 * Default file contents reader reads the files stored in the resources/default-files
 * This will help to maintain the default files and can help assert them in tests
 * @author Ali Azam Rana
 *
 */
public class DefaultFileContentsReader
{
	public static String getFileContents(String fileName) throws IOException
	{
		String filePath = IDefaultConfigConstants.DEFAULT_FILE_DIRECTORY + "/" + fileName;
		InputStream fileStream = DefaultFileContentsReader.class.getClassLoader().getResourceAsStream(filePath);
		String fileContents = IOUtils.toString(fileStream.readAllBytes(), null);
		fileStream.close();
		return fileContents; 
	}
	
}
