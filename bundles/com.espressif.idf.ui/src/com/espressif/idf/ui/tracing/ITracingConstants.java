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
	String EVENTS_KEY = "events"; //$NON-NLS-1$

	String HEAP_KEY = "heap"; //$NON-NLS-1$

	String HEAP_ALLOC_EVENT_KEY = "alloc"; //$NON-NLS-1$

	String HEAP_FREE_EVENT_KEY = "free"; //$NON-NLS-1$

	String STREAMS_KEY = "streams"; //$NON-NLS-1$
}
