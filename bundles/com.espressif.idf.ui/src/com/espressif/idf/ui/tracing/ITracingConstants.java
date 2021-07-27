/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tracing;

/**
 * Interface to store the constants related to JSON for tracing
 * 
 * @author Ali Azam Rana
 *
 */
public interface ITracingConstants
{
	String EVENTS_KEY = "events";

	String HEAP_KEY = "heap";

	String HEAP_ALLOC_EVENT_KEY = "alloc";

	String HEAP_FREE_EVENT_KEY = "free";

	String STREAMS_KEY = "streams";
}
