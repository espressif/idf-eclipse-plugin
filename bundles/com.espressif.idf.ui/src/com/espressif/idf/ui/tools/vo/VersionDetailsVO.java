/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.vo;

/**
 * Version details vo for the versions class
 * 
 * @author Ali Azam Rana
 *
 */
public class VersionDetailsVO
{
	private String sha256;

	private double size;

	private String url;

	public String getSha256()
	{
		return sha256;
	}

	public void setSha256(String sha256)
	{
		this.sha256 = sha256;
	}

	public double getSize()
	{
		return size;
	}

	public void setSize(double size)
	{
		this.size = size;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
}
