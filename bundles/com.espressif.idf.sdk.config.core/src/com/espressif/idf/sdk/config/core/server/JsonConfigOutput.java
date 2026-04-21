/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.logging.Logger;
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
	private JSONObject defaultsJsonMap;
	private long version = 1;

	@Override
	public long getVersion()
	{
		return version;
	}

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
			Logger.log(SDKConfigCorePlugin.getPlugin(), "Can't parse. Config server response can't be null or empty!"); //$NON-NLS-1$
			return;
		}

		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(response);
		if (jsonObj != null)
		{
			if (jsonObj.containsKey(IJsonServerConfig.VERSION))
			{
				version = (long) jsonObj.get(IJsonServerConfig.VERSION);
			}

			if (isUpdate)
			{
				// newly updated values and visible items
				JSONObject visibleJson = (JSONObject) jsonObj.get(IJsonServerConfig.VISIBLE);
				JSONObject valuesJson = (JSONObject) jsonObj.get(IJsonServerConfig.VALUES);
				JSONObject rangesJson = (JSONObject) jsonObj.get(IJsonServerConfig.RANGES);
				JSONObject defaultsJson = (JSONObject) jsonObj.get(IJsonServerConfig.DEFAULTS);

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
				defaultsJsonMap = (JSONObject) jsonObj.get(IJsonServerConfig.DEFAULTS);
			}
		}

	}

	public JSONObject getDefaultsJsonMap()
	{
		return defaultsJsonMap;
	}

}
