/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.install;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFVersionsReader
{

	private final String VERSIONS_URL = "https://dl.espressif.com/dl/esp-idf/idf_versions.txt"; //$NON-NLS-1$
	private static final String GITHUB_VERSION_URL = "https://github.com/espressif/esp-idf/releases/download/IDFZIPFileVersion/esp-idf-IDFZIPFileVersion.zip"; //$NON-NLS-1$
	private static final String ESPRESSIF_VERSION_URL = "https://dl.espressif.com/dl/esp-idf/releases/esp-idf-IDFZIPFileVersion.zip"; //$NON-NLS-1$
	private static final String MASTER_URL = " https://github.com/espressif/esp-idf/archive/master.zip"; //$NON-NLS-1$

	public List<String> getVersions()
	{
		List<String> versionList = new ArrayList<>();
		try
		{
			URL oracle = new URL(VERSIONS_URL);
			URLConnection yc = oracle.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				versionList.add(inputLine);
			}
			in.close();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

		return versionList;
	}

	public Map<String, IDFVersion> getVersionsMap()
	{
		Map<String, IDFVersion> versionsMap = new LinkedHashMap<String, IDFVersion>();
		String versionRegEx = "IDFZIPFileVersion";
		List<String> versions = getVersions();
		for (String version : versions)
		{
			if (version.startsWith("master"))
			{
				String gitHubVersionUrl = MASTER_URL;
				String espressifVersionUrl = MASTER_URL;
				versionsMap.put(version, new IDFVersion(version, gitHubVersionUrl, espressifVersionUrl));
			}
			else if (version.startsWith("v"))
			{
				String gitHubVersionUrl = GITHUB_VERSION_URL.replace(versionRegEx, version);
				String espressifVersionUrl = ESPRESSIF_VERSION_URL.replace(versionRegEx, version);
				versionsMap.put(version, new IDFVersion(version, gitHubVersionUrl, espressifVersionUrl));
			}
			else if (version.startsWith("release/"))
			{
				String newVersion = version.replace("release/", "");
				String gitHubVersionUrl = GITHUB_VERSION_URL.replace(versionRegEx, newVersion);
				String espressifVersionUrl = ESPRESSIF_VERSION_URL.replace(versionRegEx, newVersion);
				versionsMap.put(version, new IDFVersion(version, gitHubVersionUrl, espressifVersionUrl));
			}
		}

		return versionsMap;
	}

	// test
	public static void main(String[] args)
	{
		IDFVersionsReader reader = new IDFVersionsReader();
		Map<String, IDFVersion> versionsMap = reader.getVersionsMap();
		for (String version : versionsMap.keySet())
		{
			IDFVersion idfVersion = versionsMap.get(version);
			System.out.println("Version:" + version);
			System.out.println("URL:" + idfVersion.getUrl());
			System.out.println("Mirror URL:" + idfVersion.getMirrorUrl());
			System.out.println("");
		}

	}
}
