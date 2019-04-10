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
	String VALUES = Messages.IJsonServerConfig_values;
	String VISIBLE = Messages.IJsonServerConfig_visible;
	String RANGES = Messages.IJsonServerConfig_ranges;
	String VERSION = Messages.IJsonServerConfig_version;

	// data types
	String HEX_TYPE = Messages.IJsonServerConfig_hex;
	String STRING_TYPE = Messages.IJsonServerConfig_string;
	String BOOL_TYPE = Messages.IJsonServerConfig_bool;
	String INT_TYPE = Messages.IJsonServerConfig_int;
	String CHOICE_TYPE = Messages.IJsonServerConfig_choice;
	String MENU_TYPE = Messages.IJsonServerConfig_menu;

	// Operations
	String SET = Messages.IJsonServerConfig_set;
	String SAVE = Messages.IJsonServerConfig_save;
	String LOAD = Messages.IJsonServerConfig_load;
}
