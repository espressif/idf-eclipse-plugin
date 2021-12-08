/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.tools.vo.ToolsVO;
import com.espressif.idf.ui.tools.vo.VersionDetailsVO;
import com.espressif.idf.ui.tools.vo.VersionsVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 * Tools json parser.
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsJsonParser
{
	private static final String URL_KEY = "url"; //$NON-NLS-1$
	private static final String SIZE_KEY = "size"; //$NON-NLS-1$
	private static final String SHA256_KEY = "sha256"; //$NON-NLS-1$
	private static final String STATUS_KEY = "status"; //$NON-NLS-1$
	private static final String VERSION_KEY = "version"; //$NON-NLS-1$
	private static final String VERSIONS_VO_KEY = "versions"; //$NON-NLS-1$
	private static final String VERSION_CMD_KEY = "version_cmd"; //$NON-NLS-1$
	private static final String SUPPORTED_TARGETS_KEY = "supported_targets"; //$NON-NLS-1$
	private static final String NAME_KEY = "name"; //$NON-NLS-1$
	private static final String LICENSE_KEY = "license"; //$NON-NLS-1$
	private static final String INSTALL_KEY = "install"; //$NON-NLS-1$
	private static final String INFO_URL_KEY = "info_url"; //$NON-NLS-1$
	private static final String EXPORT_VARS_KEY = "export_vars"; //$NON-NLS-1$
	private static final String EXPORT_PATHS_KEY = "export_paths"; //$NON-NLS-1$
	private static final String DESCRIPTION_KEY = "description"; //$NON-NLS-1$
	private static final String TOOLS_KEY = "tools"; //$NON-NLS-1$
	private Gson gson;
	private List<ToolsVO> toolsList;

	public ToolsJsonParser()
	{
		gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		toolsList = new ArrayList<>();
	}

	public void loadJson() throws Exception
	{
		toolsList.clear();
		JsonReader jsonReader = new JsonReader(new FileReader(IDFUtil.getIDFToolsJsonFileForInstallation()));
		JsonObject jsonObject = gson.fromJson(jsonReader, JsonObject.class);
		JsonArray jsonArray = jsonObject.get(TOOLS_KEY).getAsJsonArray();
		for (int i = 0; i < jsonArray.size(); i++)
		{
			JsonObject toolsJsonObject = jsonArray.get(i).getAsJsonObject();
			ToolsVO toolsVO = new ToolsVO();
			toolsVO.setDescription(toolsJsonObject.get(DESCRIPTION_KEY).getAsString());
			toolsVO.setExportPaths(getStringsListFromJsonArray(
					toolsJsonObject.get(EXPORT_PATHS_KEY).getAsJsonArray().get(0).getAsJsonArray()));
			toolsVO.setExportVars(
					getExportVarsMapFromJsonObject(toolsJsonObject.get(EXPORT_VARS_KEY).getAsJsonObject()));
			toolsVO.setInfoUrl(toolsJsonObject.get(INFO_URL_KEY).getAsString());
			toolsVO.setInstallType(toolsJsonObject.get(INSTALL_KEY).getAsString());
			toolsVO.setLicesnse(toolsJsonObject.get(LICENSE_KEY).getAsString());
			toolsVO.setName(toolsJsonObject.get(NAME_KEY).getAsString());
			toolsVO.setSupportedTargets(
					getStringsListFromJsonArray(toolsJsonObject.get(SUPPORTED_TARGETS_KEY).getAsJsonArray()));
			toolsVO.setVersionCmd(getStringsListFromJsonArray(toolsJsonObject.get(VERSION_CMD_KEY).getAsJsonArray()));
			toolsVO.setVersionVO(getVersions(toolsJsonObject.get(VERSIONS_VO_KEY).getAsJsonArray()));
			toolsVO.setVersion(jsonObject.get(VERSION_KEY).getAsString());
			toolsList.add(toolsVO);
		}
	}

	private VersionsVO getVersions(JsonArray jsonArray)
	{
		JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
		VersionsVO versionsVO = new VersionsVO();
		Map<String, VersionDetailsVO> versionDetailMap = new HashMap<>();
		for (String key : jsonObject.keySet())
		{
			if (key.equalsIgnoreCase(NAME_KEY) || key.equalsIgnoreCase(STATUS_KEY))
			{
				continue;
			}

			VersionDetailsVO versionDetailsVO = new VersionDetailsVO();
			JsonObject osVersionDetailsObject = jsonObject.get(key).getAsJsonObject();
			versionDetailsVO.setSha256(osVersionDetailsObject.get(SHA256_KEY).getAsString());
			versionDetailsVO.setSize(osVersionDetailsObject.get(SIZE_KEY).getAsDouble());
			versionDetailsVO.setUrl(osVersionDetailsObject.get(URL_KEY).getAsString());
			versionDetailMap.put(key, versionDetailsVO);
		}

		versionsVO.setName(jsonObject.get(NAME_KEY).getAsString());
		versionsVO.setStatus(jsonObject.get(STATUS_KEY).getAsString());
		versionsVO.setVersionOsMap(versionDetailMap);

		return versionsVO;
	}

	private List<String> getStringsListFromJsonArray(JsonArray jsonArray)
	{
		List<String> stringList = new ArrayList<String>();
		if (jsonArray == null)
		{
			return stringList;
		}

		for (int i = 0; i < jsonArray.size(); i++)
		{
			stringList.add(jsonArray.get(i).getAsString());
		}
		return stringList;
	}

	private Map<String, String> getExportVarsMapFromJsonObject(JsonObject exportVars)
	{
		Map<String, String> exportVarMap = new HashMap<>();
		for (String key : exportVars.keySet())
		{
			exportVarMap.put(key, exportVars.get(key).getAsString());
		}
		return exportVarMap;
	}

	public List<ToolsVO> getToolsList()
	{
		return toolsList;
	}
}
