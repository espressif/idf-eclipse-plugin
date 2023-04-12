/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.espressif.idf.core.logging.Logger;

/**
 * esp_idf.json parser utility class to parse the 
 * stored configs for startup in esp_idf.json
 * @author Ali Azam Rana
 *
 */
public class EspIdfJsonParser
{
	/**
	 * esp-idf.json is file created by the installer
	 */
	private static final String ESP_IDF_JSON_FILE = "esp_idf.json"; //$NON-NLS-1$
	
	// Variables defined in the esp-idf.json file
	private static final String GIT_PATH = "gitPath"; //$NON-NLS-1$
	private static final String IDF_VERSIONS_ID = "idfSelectedId"; //$NON-NLS-1$
	private static final String IDF_INSTALLED_LIST_KEY = "idfInstalled"; //$NON-NLS-1$
	private static final String PYTHON_PATH = "python"; //$NON-NLS-1$
	private static final String IDF_PATH = "path"; //$NON-NLS-1$
	private static final String DEFAULT_WORSPACE_PATH = "defaultWorkspace"; //$NON-NLS-1$

	private String gitExecutablePath;
	private String idfVersionId;
	private String idfPath;
	private String pythonExecutablePath;
	private String defaultWorkspaceLocation;
	private boolean isIdfJsonPresent;

	public void parseJsonAndLoadValues()
	{
		// Get the location of the eclipse root directory
		Location installLocation = Platform.getInstallLocation();
		URL url = installLocation.getURL();
		Logger.log("Eclipse Install location::" + url); //$NON-NLS-1$
		File idf_json_file = new File(url.getPath() + File.separator + ESP_IDF_JSON_FILE);
		if (!idf_json_file.exists())
		{
			Logger.log(MessageFormat.format("esp-idf.json file doesn't exist at this location: '{0}'", url.getPath())); //$NON-NLS-1$
			isIdfJsonPresent = false;
			return;
		}
		
		isIdfJsonPresent = true;
		
		
		JSONParser parser = new JSONParser();
		try (FileReader fr = new FileReader(idf_json_file))
		{
			JSONObject jsonObj = (JSONObject) parser.parse(fr);
			gitExecutablePath = (String) jsonObj.get(GIT_PATH);
			idfVersionId = (String) jsonObj.get(IDF_VERSIONS_ID);
			defaultWorkspaceLocation = (String) jsonObj.get(DEFAULT_WORSPACE_PATH);
			JSONObject list = (JSONObject) jsonObj.get(IDF_INSTALLED_LIST_KEY);
			if (list != null)
			{
				// selected esp-idf version information
				JSONObject selectedIDFInfo = (JSONObject) list.get(idfVersionId);
				idfPath = (String) selectedIDFInfo.get(IDF_PATH);
				pythonExecutablePath = (String) selectedIDFInfo.get(PYTHON_PATH);
			}
		}
		catch (Exception e) 
		{
			Logger.log(e);
		}
	}

	public String getGitExecutablePath()
	{
		return gitExecutablePath;
	}

	public String getIdfVersionId()
	{
		return idfVersionId;
	}

	public String getIdfPath()
	{
		return idfPath;
	}

	public String getPythonExecutablePath()
	{
		return pythonExecutablePath;
	}

	public boolean isIdfJsonPresent()
	{
		return isIdfJsonPresent;
	}

	public String getDefaultWorkspaceLocation()
	{
		return defaultWorkspaceLocation;
	}
}
