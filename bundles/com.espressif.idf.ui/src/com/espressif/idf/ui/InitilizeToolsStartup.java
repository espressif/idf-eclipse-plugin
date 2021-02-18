/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IStartup;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.logging.Logger;

public class InitilizeToolsStartup implements IStartup
{

	/**
	 * esp-idf.json is file created by the installer
	 */
	private static final String ESP_IDF_JSON_FILE = "esp_idf.json"; //$NON-NLS-1$

	// Variables defined in the esp-idf.json file
	private static final String GIT_PATH = "gitPath"; //$NON-NLS-1$
	private static final String TOOLS_PATH = "idfToolsPath"; //$NON-NLS-1$
	private static final String IDF_VERSIONS_ID = "idfSelectedId"; //$NON-NLS-1$
	private static final String IDF_INSTALLED_LIST_KEY = "idfInstalled"; //$NON-NLS-1$
	private static final String PYTHON_PATH = "python"; //$NON-NLS-1$
	private static final String IDF_PATH = "path"; //$NON-NLS-1$

	@Override
	public void earlyStartup()
	{
		// Get the location of the eclipse root directory
		Location installLocation = Platform.getInstallLocation();
		URL url = installLocation.getURL();
		Logger.log("Install location::" + url);

		// Check the esp-idf.json
		File idf_json_file = new File(url.getPath() + File.separator + ESP_IDF_JSON_FILE);
		if (!idf_json_file.exists())
		{
			Logger.log(MessageFormat.format("esp-idf.json file doesn't exist at this location: {0}", url.getPath()));
		}

		// read esp-idf.json file
		JSONParser parser = new JSONParser();
		try
		{
			JSONObject jsonObj = (JSONObject) parser.parse(new FileReader(idf_json_file));
			String gitPath = (String) jsonObj.get(GIT_PATH);
			String toolsPath = (String) jsonObj.get(TOOLS_PATH);
			String idfVersionId = (String) jsonObj.get(IDF_VERSIONS_ID);
			JSONObject list = (JSONObject) jsonObj.get(IDF_INSTALLED_LIST_KEY);
			if (list != null)
			{
				// selected esp-idf version information
				JSONObject selectedIDFInfo = (JSONObject) list.get(idfVersionId);
				String idfPath = (String) selectedIDFInfo.get(IDF_PATH);
				String pythongPath = (String) selectedIDFInfo.get(PYTHON_PATH);
				
			}

		}
		catch (
				IOException
				| ParseException e)
		{
			Logger.log(e);
		}
	}

}
