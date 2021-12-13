/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.vo;

import java.text.DecimalFormat;
import java.util.List;

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
	
	private boolean selected;
	
	private String parentName;
	
	private List<String> exportPaths;

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

	public String getReadableSize()
	{
		double totalSize = getSize();
		totalSize /= 1024; // KB
		totalSize /= 1024; // MB
		DecimalFormat df = new DecimalFormat("0.00");
		return String.valueOf(df.format(totalSize)).concat(" MB");
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public String getParentName()
	{
		return parentName;
	}

	public void setParentName(String parentName)
	{
		this.parentName = parentName;
	}

	public List<String> getExportPaths()
	{
		return exportPaths;
	}

	public void setExportPaths(List<String> exportPaths)
	{
		this.exportPaths = exportPaths;
	}
}
