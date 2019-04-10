/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public interface IJsonServerConfig
{

	// json output constants
	String VALUES = "values"; //$NON-NLS-1$
	String VISIBLE = "visible"; //$NON-NLS-1$
	String RANGES = "ranges"; //$NON-NLS-1$
	String VERSION = "version"; //$NON-NLS-1$

	// data types
	String HEX_TYPE = "hex"; //$NON-NLS-1$
	String STRING_TYPE = "string"; //$NON-NLS-1$
	String BOOL_TYPE = "bool"; //$NON-NLS-1$
	String INT_TYPE = "int"; //$NON-NLS-1$
	String CHOICE_TYPE = "choice"; //$NON-NLS-1$
	String MENU_TYPE = "menu"; //$NON-NLS-1$

	// Operations
	String SET = "set"; //$NON-NLS-1$
	String SAVE = "save"; //$NON-NLS-1$
	String LOAD = "load"; //$NON-NLS-1$
}
