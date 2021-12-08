/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.vo;

import java.util.List;
import java.util.Map;

/**
 * Bean class for tools information from json
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsVO
{
	private String description;

	private List<String> exportPaths;

	private Map<String, String> exportVars;

	private String infoUrl;

	private String installType;

	private String licesnse;

	private String name;

	private List<String> supportedTargets;

	private List<String> versionCmd;

	private VersionsVO versionVO;

	private String version;

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<String> getExportPaths()
	{
		return exportPaths;
	}

	public void setExportPaths(List<String> exportPaths)
	{
		this.exportPaths = exportPaths;
	}

	public String getInfoUrl()
	{
		return infoUrl;
	}

	public void setInfoUrl(String infoUrl)
	{
		this.infoUrl = infoUrl;
	}

	public Map<String, String> getExportVars()
	{
		return exportVars;
	}

	public void setExportVars(Map<String, String> exportVars)
	{
		this.exportVars = exportVars;
	}

	public String getInstallType()
	{
		return installType;
	}

	public void setInstallType(String installType)
	{
		this.installType = installType;
	}

	public String getLicesnse()
	{
		return licesnse;
	}

	public void setLicesnse(String licesnse)
	{
		this.licesnse = licesnse;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<String> getSupportedTargets()
	{
		return supportedTargets;
	}

	public void setSupportedTargets(List<String> supportedTargets)
	{
		this.supportedTargets = supportedTargets;
	}

	public List<String> getVersionCmd()
	{
		return versionCmd;
	}

	public void setVersionCmd(List<String> versionCmd)
	{
		this.versionCmd = versionCmd;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public VersionsVO getVersionVO()
	{
		return versionVO;
	}

	public void setVersionVO(VersionsVO versionVO)
	{
		this.versionVO = versionVO;
	}
}
