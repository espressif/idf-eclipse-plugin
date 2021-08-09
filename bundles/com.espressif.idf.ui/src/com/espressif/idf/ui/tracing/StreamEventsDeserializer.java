/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Stream events desrializing class for json 
 * @author Ali Azam Rana
 *
 */
public class StreamEventsDeserializer implements JsonDeserializer<List<StreamEventsVO>>
{

	@Override
	public List<StreamEventsVO> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException
	{
		List<StreamEventsVO> streamEventsVOs = new ArrayList<StreamEventsVO>();

		JsonElement streamsElement = json.getAsJsonObject().get(ITracingConstants.STREAMS_KEY);
		for (String key : streamsElement.getAsJsonObject().keySet())
		{
			StreamEventsVO streamEventsVO = new StreamEventsVO();
			streamEventsVO.setStreamName(key);
			JsonObject internalEventStream = streamsElement.getAsJsonObject().get(key).getAsJsonObject();
			Map<String, Integer> streamEventIdMap = new HashMap<String, Integer>();
			for (String event : internalEventStream.keySet())
			{
				streamEventIdMap.put(event, internalEventStream.get(event).getAsInt());
			}
			streamEventsVO.setStreamEventIdMap(streamEventIdMap);
			streamEventsVOs.add(streamEventsVO);
		}

		return streamEventsVOs;
	}

}
