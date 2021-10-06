package com.espressif.idf.ui.installcomponents.vo;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ComponentDetailsVO
{
	private String componentHash;
	
	private String createdAt;
	
	private String description;
	
	private String url;
	
	private String version;
	
	private List<ComponentDetailsDependenciesVO> dependencies;
	
	private String readMe;
	
	private List<String> targets;

	public String getComponentHash()
	{
		return componentHash;
	}

	public void setComponentHash(String componentHash)
	{
		this.componentHash = componentHash;
	}

	public String getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(String createdAt)
	{
		this.createdAt = createdAt;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public List<ComponentDetailsDependenciesVO> getDependencies()
	{
		return dependencies;
	}

	public void setDependencies(List<ComponentDetailsDependenciesVO> dependencies)
	{
		this.dependencies = dependencies;
	}

	public String getReadMe()
	{
		return readMe;
	}

	public void setReadMe(String readMe)
	{
		this.readMe = readMe;
	}

	public List<String> getTargets()
	{
		return targets;
	}

	public void setTargets(List<String> targets)
	{
		this.targets = targets;
	}
}
