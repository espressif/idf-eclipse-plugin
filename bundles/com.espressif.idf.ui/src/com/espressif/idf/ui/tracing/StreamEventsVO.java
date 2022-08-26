/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tracing;

import java.util.Map;

/**
 * Streams events vo/bean class
 * 
 * @author Ali Azam Rana
 *
 */
public class StreamEventsVO
{
	private String streamName;

	private Map<String, Integer> streamEventIdMap;

	public String getStreamName()
	{
		return streamName;
	}

	public void setStreamName(String streamName)
	{
		this.streamName = streamName;
	}

	public Map<String, Integer> getStreamEventIdMap()
	{
		return streamEventIdMap;
	}

	public void setStreamEventIdMap(Map<String, Integer> streamEventIdMap)
	{
		this.streamEventIdMap = streamEventIdMap;
	}
}
