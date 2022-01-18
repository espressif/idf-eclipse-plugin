/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.vo;

import java.util.Map;

/**
 * Versions class for versions information in tools json
 * 
 * @author Ali Azam Rana
 *
 */
public class VersionsVO
{
	private String name;

	private String status;

	private Map<String, VersionDetailsVO> versionOsMap;
	
	private boolean isAvailable;
	
	private String availablePath;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public Map<String, VersionDetailsVO> getVersionOsMap()
	{
		return versionOsMap;
	}

	public void setVersionOsMap(Map<String, VersionDetailsVO> versionOsMap)
	{
		this.versionOsMap = versionOsMap;
	}

	public boolean isAvailable()
	{
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable)
	{
		this.isAvailable = isAvailable;
	}

	public String getAvailablePath()
	{
		return availablePath;
	}

	public void setAvailablePath(String availablePath)
	{
		this.availablePath = availablePath;
	}
}
