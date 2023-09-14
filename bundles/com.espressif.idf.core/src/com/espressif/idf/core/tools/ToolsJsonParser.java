/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.tools.vo.ToolsVO;
import com.espressif.idf.core.tools.vo.VersionDetailsVO;
import com.espressif.idf.core.tools.vo.VersionsVO;
import com.espressif.idf.core.util.IDFUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
	private Gson gson;
	private List<ToolsVO> toolsList;
	private List<ToolsVO> requiredToolsList;
	private static final String[] REQUIRED_TOOLS = new String[] {"cmake", "dfu-util", "ninja"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public ToolsJsonParser()
	{
		gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		toolsList = new ArrayList<>();
		requiredToolsList = new ArrayList<>();
	}

	public void loadJson() throws Exception
	{
		toolsList.clear();
		JsonReader jsonReader = new JsonReader(new FileReader(IDFUtil.getIDFToolsJsonFileForInstallation()));
		JsonObject jsonObject = gson.fromJson(jsonReader, JsonObject.class);
		JsonArray jsonArray = jsonObject.get(IToolsJsonKeys.TOOLS_KEY).getAsJsonArray();
		List<String> reqToolsNamesList = Arrays.asList(REQUIRED_TOOLS);
		for (int i = 0; i < jsonArray.size(); i++)
		{
			JsonObject toolsJsonObject = jsonArray.get(i).getAsJsonObject();
			ToolsVO toolsVO = new ToolsVO();
			toolsVO.setDescription(toolsJsonObject.get(IToolsJsonKeys.DESCRIPTION_KEY).getAsString());
			if (toolsJsonObject.get(IToolsJsonKeys.EXPORT_PATHS_KEY).getAsJsonArray().size() > 0)
			{
				toolsVO.setExportPaths(getStringsListFromJsonArray(
						toolsJsonObject.get(IToolsJsonKeys.EXPORT_PATHS_KEY).getAsJsonArray().get(0).getAsJsonArray()));	
			}
			toolsVO.setExportVars(
					getExportVarsMapFromJsonObject(toolsJsonObject.get(IToolsJsonKeys.EXPORT_VARS_KEY).getAsJsonObject()));
			toolsVO.setInfoUrl(toolsJsonObject.get(IToolsJsonKeys.INFO_URL_KEY).getAsString());
			toolsVO.setInstallType(toolsJsonObject.get(IToolsJsonKeys.INSTALL_KEY).getAsString());
			toolsVO.setLicesnse(toolsJsonObject.get(IToolsJsonKeys.LICENSE_KEY).getAsString());
			toolsVO.setName(toolsJsonObject.get(IToolsJsonKeys.NAME_KEY).getAsString());
			if (toolsJsonObject.get(IToolsJsonKeys.SUPPORTED_TARGETS_KEY) != null)
			{
				toolsVO.setSupportedTargets(
						getStringsListFromJsonArray(toolsJsonObject.get(IToolsJsonKeys.SUPPORTED_TARGETS_KEY).getAsJsonArray()));	
			}
			toolsVO.setVersionCmd(getStringsListFromJsonArray(toolsJsonObject.get(IToolsJsonKeys.VERSION_CMD_KEY).getAsJsonArray()));
			toolsVO.setVersionRegex(toolsJsonObject.get(IToolsJsonKeys.VERSION_REGEX).getAsString());
			toolsVO.setVersionVO(getVersions(toolsJsonObject.get(IToolsJsonKeys.VERSIONS_VO_KEY).getAsJsonArray()));
			toolsVO.setVersion(jsonObject.get(IToolsJsonKeys.VERSION_KEY).getAsString());
			JsonElement jsonElement = toolsJsonObject.get(IToolsJsonKeys.PLATFORM_OVERRIDES_KEY);
			if (jsonElement != null)
			{
				adjustPlatformOverrides(jsonElement.getAsJsonArray(), toolsVO);	
			}
			
			toolsList.add(toolsVO);
			if (reqToolsNamesList.contains(toolsVO.getName()))
			{
				requiredToolsList.add(toolsVO);
			}
		}
	}
	
	private void adjustPlatformOverrides(JsonArray jsonArray, ToolsVO toolsVO) throws Exception
	{
		String currentOS = Platform.getOS();
		if (currentOS.equals(Platform.OS_WIN32))
		{
			currentOS = "win"; //$NON-NLS-1$
		}
		
		if (currentOS.contains(Platform.OS_MACOSX))
		{
			Process p = Runtime.getRuntime().exec("uname -m"); //$NON-NLS-1$
			InputStreamReader inputStreamReader = new InputStreamReader(p.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String output = bufferedReader.readLine();
			if (!output.contains("arm64")) //$NON-NLS-1$
			{
				currentOS = "macos"; //$NON-NLS-1$
			}
			else 
			{
				currentOS = "macos-".concat(output); //$NON-NLS-1$
				inputStreamReader.close();
				bufferedReader.close();				
			}
		}
		
		for (int i = 0; i < jsonArray.size(); i++)
		{
			JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
			JsonArray platformArray = jsonObject.get(IToolsJsonKeys.PLATFORMS_KEY).getAsJsonArray();
			for (int j = 0; j < platformArray.size(); j++)
			{
				String platform = platformArray.get(j).getAsString();
				if (platform.contains(currentOS))
				{
					Set<String> keys = jsonObject.keySet();
					keys.remove(IToolsJsonKeys.PLATFORMS_KEY);
					for(String key : keys)
					{
						JsonElement element = jsonObject.get(key);
						if (element.isJsonArray())
						{
							List<String> list = getStringsListFromJsonArray(element.getAsJsonArray().get(0).getAsJsonArray());
							injectOverride(toolsVO, key, list);
						}
						else
						{
							injectOverride(toolsVO, key, element.getAsString());
						}
					}
				}
			}
		}
	}
	
	private void injectOverride(ToolsVO toolsVO, String key, Object val) throws Exception
	{
		Field[] allFields = ToolsVO.class.getDeclaredFields();
		for (Field field : allFields)
		{
			if (field.isAnnotationPresent(JsonKey.class))
			{
				JsonKey ann = field.getAnnotation(JsonKey.class);
				if (ann.key_name().equals(key))
				{
					field.trySetAccessible();
					field.set(toolsVO, val);
					return;
				}
			}
		}
	}

	private List<VersionsVO> getVersions(JsonArray jsonArray)
	{
		List<VersionsVO> versionsVOs = new ArrayList<VersionsVO>();
		for (int i = 0; i < jsonArray.size(); i++)
		{
			JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
			VersionsVO versionsVO = new VersionsVO();
			Map<String, VersionDetailsVO> versionDetailMap = new HashMap<>();
			for (String key : jsonObject.keySet())
			{
				if (key.equalsIgnoreCase(IToolsJsonKeys.NAME_KEY) || key.equalsIgnoreCase(IToolsJsonKeys.STATUS_KEY))
				{
					continue;
				}

				VersionDetailsVO versionDetailsVO = new VersionDetailsVO();
				JsonObject osVersionDetailsObject = jsonObject.get(key).getAsJsonObject();
				versionDetailsVO.setSha256(osVersionDetailsObject.get(IToolsJsonKeys.SHA256_KEY).getAsString());
				versionDetailsVO.setSize(osVersionDetailsObject.get(IToolsJsonKeys.SIZE_KEY).getAsDouble());
				versionDetailsVO.setUrl(osVersionDetailsObject.get(IToolsJsonKeys.URL_KEY).getAsString());
				versionDetailMap.put(key, versionDetailsVO);
			}

			versionsVO.setName(jsonObject.get(IToolsJsonKeys.NAME_KEY).getAsString());
			versionsVO.setStatus(jsonObject.get(IToolsJsonKeys.STATUS_KEY).getAsString());
			versionsVO.setVersionOsMap(versionDetailMap);
			versionsVOs.add(versionsVO);

		}

		return versionsVOs;
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

	public List<ToolsVO> getRequiredToolsList()
	{
		return requiredToolsList;
		
	}
}
