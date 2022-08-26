/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tracing;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Events vo/bean class to store information for events in the json parsed
 * 
 * @author Ali Azam Rana
 *
 */
public class EventsVO
{
	@SerializedName("core_id")
	private int coreId;

	@SerializedName("ctx_name")
	private String contextName;

	@SerializedName("id")
	private int eventId;

	@SerializedName("in_irq")
	private boolean isIRQ;

	@SerializedName("params")
	private Map<String, Object> parameters;

	@SerializedName("ts")
	private float timestampOfEvent;

	@SerializedName("addr")
	private String addressOfAllocatedMemoryBlock;

	@SerializedName("size")
	private int sizeOfAllocatedMemoryBlock;

	@SerializedName("callers")
	private List<String> callersAddressList;

	public int getCoreId()
	{
		return coreId;
	}

	public void setCoreId(int coreId)
	{
		this.coreId = coreId;
	}

	public String getContextName()
	{
		return contextName;
	}

	public void setContextName(String contextName)
	{
		this.contextName = contextName;
	}

	public int getEventId()
	{
		return eventId;
	}

	public void setEventId(int eventId)
	{
		this.eventId = eventId;
	}

	public boolean isIRQ()
	{
		return isIRQ;
	}

	public void setIRQ(boolean isIRQ)
	{
		this.isIRQ = isIRQ;
	}

	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters)
	{
		this.parameters = parameters;
	}

	public float getTimestampOfEvent()
	{
		return timestampOfEvent;
	}

	public void setTimestampOfEvent(float timestampOfEvent)
	{
		this.timestampOfEvent = timestampOfEvent;
	}

	public String getAddressOfAllocatedMemoryBlock()
	{
		return addressOfAllocatedMemoryBlock;
	}

	public void setAddressOfAllocatedMemoryBlock(String addressOfAllocatedMemoryBlock)
	{
		this.addressOfAllocatedMemoryBlock = addressOfAllocatedMemoryBlock;
	}

	public int getSizeOfAllocatedMemoryBlock()
	{
		return sizeOfAllocatedMemoryBlock;
	}

	public void setSizeOfAllocatedMemoryBlock(int sizeOfAllocatedMemoryBlock)
	{
		this.sizeOfAllocatedMemoryBlock = sizeOfAllocatedMemoryBlock;
	}

	public List<String> getCallersAddressList()
	{
		return callersAddressList;
	}

	public void setCallersAddressList(List<String> callersAddressList)
	{
		this.callersAddressList = callersAddressList;
	}
}
