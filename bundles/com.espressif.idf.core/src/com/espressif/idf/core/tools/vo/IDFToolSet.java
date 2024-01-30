package com.espressif.idf.core.tools.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;

import com.espressif.idf.core.toolchain.ESPToolchain;
import com.google.gson.annotations.Expose;

public class IDFToolSet implements Serializable 
{
	private static final long serialVersionUID = -4899224940094139736L;
	
	@Expose
	private String id;
	
	@Expose
	private String idfLocation;
	
	@Expose
	private String idfVersion;
	
	@Expose
	private boolean active;
	
	@Expose
	private Map<String, String> envVars;
	
	
	private List<ESPToolchain> espStdToolChains;
	private List<ICMakeToolChainFile> espCmakeToolChainFiles;
	private List<String> launchTargets;
	
	public String getIdfLocation()
	{
		return idfLocation;
	}
	public void setIdfLocation(String idfLocation)
	{
		this.idfLocation = idfLocation;
	}
	public String getIdfVersion()
	{
		return idfVersion;
	}
	public void setIdfVersion(String idfVersion)
	{
		this.idfVersion = idfVersion;
	}
	public boolean isActive()
	{
		return active;
	}
	public void setActive(boolean active)
	{
		this.active = active;
	}
	public Map<String, String> getEnvVars()
	{
		return envVars;
	}
	public void setEnvVars(Map<String, String> envVars)
	{
		this.envVars = envVars;
	}
	public List<ESPToolchain> getEspStdToolChains()
	{
		return espStdToolChains;
	}
	public void setEspStdToolChains(List<ESPToolchain> espStdToolChains)
	{
		this.espStdToolChains = espStdToolChains;
	}
	public List<ICMakeToolChainFile> getEspCmakeToolChainFiles()
	{
		return espCmakeToolChainFiles;
	}
	public void setEspCmakeToolChainFiles(List<ICMakeToolChainFile> espCmakeToolChainFiles)
	{
		this.espCmakeToolChainFiles = espCmakeToolChainFiles;
	}
	public List<String> getLaunchTargets()
	{
		return launchTargets;
	}
	public void setLaunchTargets(List<String> launchTargets)
	{
		this.launchTargets = launchTargets;
	}
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
}
