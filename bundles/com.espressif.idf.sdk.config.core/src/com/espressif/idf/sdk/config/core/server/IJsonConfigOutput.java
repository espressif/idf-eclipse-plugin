/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

import org.json.simple.JSONObject;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public interface IJsonConfigOutput
{
	/**
	 * @return Values json map from the configuration server output
	 */
	public JSONObject getValuesJsonMap();

	/**
	 * @return Visible json map from the configuration server output
	 */
	public JSONObject getVisibleJsonMap();

	/**
	 * @return Ranges map from the configuration server output
	 */
	public JSONObject getRangesJsonMap();

}
