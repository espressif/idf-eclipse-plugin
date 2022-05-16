/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.vo;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;

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

	private List<VersionsVO> versionVOs;

	private String version;
	
	private static final String MAC_OS = "mac"; //$NON-NLS-1$
	private static final String LINUX_OS = "linux"; //$NON-NLS-1$
	private static final String WIN_OS = "win"; //$NON-NLS-1$

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

	public List<VersionsVO> getVersionVO()
	{
		return versionVOs;
	}

	public void setVersionVO(List<VersionsVO> versionVO)
	{
		this.versionVOs = versionVO;
	}

	public double getSize()
	{
		double totalSize = 0;
		String key = null;
		if (Platform.getOS().equals(Platform.OS_WIN32))
		{
			key = WIN_OS;
		}
		else if (Platform.getOS().equals(Platform.OS_LINUX))
		{
			key = LINUX_OS;
		}
		else if (Platform.getOS().equals(Platform.OS_MACOSX)) 
		{
			key = MAC_OS;
		}
		
		for (VersionsVO versionVO : versionVOs)
		{
			totalSize += versionVO.getVersionOsMap().get(key).getSize();
		}
		
		return totalSize;
	}
	
	public String getReadableSize()
	{
		double totalSize = getSize();
		totalSize /= 1024; // KB
		totalSize /= 1024; // MB
		DecimalFormat df = new DecimalFormat("0"); //$NON-NLS-1$
		return String.valueOf(df.format(totalSize)).concat(" MB"); //$NON-NLS-1$
	}
}
