/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

/**
 * Details composite table data holding vo
 * 
 * @author Ali Azam Rana
 *
 */
public class DetailsVO
{
	private int index;

	private EventsVO eventsVO;

	private String eventName;

	private String streamName;

	private boolean memoryLeak;

	public String getEventName()
	{
		return eventName;
	}

	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}

	public String getStreamName()
	{
		return streamName;
	}

	public void setStreamName(String streamName)
	{
		this.streamName = streamName;
	}

	public EventsVO getEventsVO()
	{
		return eventsVO;
	}

	public void setEventsVO(EventsVO eventsVO)
	{
		this.eventsVO = eventsVO;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public boolean isMemoryLeak()
	{
		return memoryLeak;
	}

	public void setMemoryLeak(boolean memoryLeak)
	{
		this.memoryLeak = memoryLeak;
	}
}
