/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFVersion
{
	private String name;
	private String url;
	private String mirrorUrl;

	public IDFVersion(String name, String url, String mirrorUrl)
	{
		this.name = name;
		this.url = url;
		this.mirrorUrl = mirrorUrl;
	}

	public String getName()
	{
		return name;
	}

	public String getUrl()
	{
		return url;
	}

	public String getMirrorUrl()
	{
		return mirrorUrl;
	}
}
