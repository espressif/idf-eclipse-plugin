/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tracing;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import com.google.gson.JsonObject;

/**
 * Tracing JSON Parser
 * @author Ali Azam Rana
 *
 */
public class TracingJsonParser
{
	private String jsonFilePath;
	private Gson gson;
	private int allocEventId;
	private int freeEventId;
	private List<EventsVO> eventsVOs;
	private List<StreamEventsVO> streamEventsVOs;

	public TracingJsonParser(String jsonFilePath) throws FileNotFoundException
	{
		this.jsonFilePath = jsonFilePath;
		gson = new GsonBuilder().registerTypeAdapter(ArrayList.class, new StreamEventsDeserializer()).create();
		loadJson();
	}

	private void loadJson() throws FileNotFoundException
	{
		JsonReader jsonReader = new JsonReader(new FileReader(jsonFilePath));
		JsonObject json = gson.fromJson(jsonReader, JsonObject.class);
		streamEventsVOs = gson.fromJson(json.toString(), ArrayList.class);
		Optional<StreamEventsVO> oStreamEventsVO = getStreamEventsVOs().stream()
				.filter(stream -> stream.getStreamName().equals(ITracingConstants.HEAP_KEY)).findAny();

		if (oStreamEventsVO.isPresent())
		{
			allocEventId = oStreamEventsVO.get().getStreamEventIdMap().get(ITracingConstants.HEAP_ALLOC_EVENT_KEY);
			freeEventId = oStreamEventsVO.get().getStreamEventIdMap().get(ITracingConstants.HEAP_FREE_EVENT_KEY);
		}

		JsonArray eventsArray = json.getAsJsonObject().getAsJsonArray(ITracingConstants.EVENTS_KEY);
		eventsVOs = new ArrayList<EventsVO>(100);
		for (int i = 0; i < eventsArray.size(); i++)
		{
			EventsVO eventsVO = new Gson().fromJson(eventsArray.get(i), EventsVO.class);
			eventsVOs.add(eventsVO);
		}
	}

	public List<EventsVO> getAllocEvents()
	{
		return getEventsVOs().stream().filter(event -> event.getEventId() == getAllocEventId())
				.collect(Collectors.toList());
	}

	public List<EventsVO> getFreeEvents()
	{
		return getEventsVOs().stream().filter(event -> event.getEventId() == getFreeEventId())
				.collect(Collectors.toList());
	}

	public int getAllocEventId()
	{
		return allocEventId;
	}

	public int getFreeEventId()
	{
		return freeEventId;
	}

	public List<EventsVO> getEventsVOs()
	{
		return eventsVOs;
	}

	public List<StreamEventsVO> getStreamEventsVOs()
	{
		return streamEventsVOs;
	}

	public void setStreamEventsVOs(List<StreamEventsVO> streamEventsVOs)
	{
		this.streamEventsVOs = streamEventsVOs;
	}

	public Map<String, Integer> getAllEventsNameIdMap()
	{
		Map<String, Integer> eventsNameIdMap = new HashMap<>();
		for (StreamEventsVO streamEventsVO : streamEventsVOs)
		{
			eventsNameIdMap.putAll(streamEventsVO.getStreamEventIdMap());
		}
		return eventsNameIdMap;
	}
}
