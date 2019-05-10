/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.logging.IdfLog;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.sdk.config.core.IJsonServerConfig;
import com.espressif.idf.sdk.config.core.SDKConfigCorePlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class JsonConfigOutput implements IJsonConfigOutput
{
	private JSONObject valuesJsonMap;
	private JSONObject visibleJsonMap;
	private JSONObject rangesJsonMap;

	@Override
	public JSONObject getValuesJsonMap()
	{
		return valuesJsonMap;
	}

	@Override
	public JSONObject getVisibleJsonMap()
	{
		return visibleJsonMap;
	}

	@Override
	public JSONObject getRangesJsonMap()
	{
		return rangesJsonMap;
	}

	@SuppressWarnings("unchecked")
	public void parse(String response, boolean isUpdate) throws ParseException
	{
		if (StringUtil.isEmpty(response))
		{
			IdfLog.logError(SDKConfigCorePlugin.getPlugin(), "Can't parse. Config server response can't be null or empty!"); //$NON-NLS-1$
			return;
		}
		
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(response);
		if (jsonObj != null)
		{
			if (isUpdate)
			{
				// newly updated values and visible items
				JSONObject visibleJson = (JSONObject) jsonObj.get(IJsonServerConfig.VISIBLE);
				JSONObject valuesJson = (JSONObject) jsonObj.get(IJsonServerConfig.VALUES);
				JSONObject rangesJson = (JSONObject) jsonObj.get(IJsonServerConfig.RANGES);

				// Updated visible items
				Set<String> newVisibleKeyset = visibleJson.keySet();
				for (String key : newVisibleKeyset)
				{
					visibleJsonMap.put(key, visibleJson.get(key));
				}

				// Updated values
				Set<String> newValuesKeyset = valuesJson.keySet();
				for (String key : newValuesKeyset)
				{
					valuesJsonMap.put(key, valuesJson.get(key));
				}

				// Updated ranges
				Set<String> newRangesKeyset = rangesJson.keySet();
				for (String key : newRangesKeyset)
				{
					rangesJsonMap.put(key, rangesJson.get(key));
				}
			}
			else
			{
				valuesJsonMap = (JSONObject) jsonObj.get(IJsonServerConfig.VALUES);
				visibleJsonMap = (JSONObject) jsonObj.get(IJsonServerConfig.VISIBLE);
				rangesJsonMap = (JSONObject) jsonObj.get(IJsonServerConfig.RANGES);
			}
		}
		
	}

}
