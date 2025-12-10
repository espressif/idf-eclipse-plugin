package com.espressif.idf.core.tools.vo;

import com.google.gson.annotations.Expose;

public class IdfInstalled
{
	@Expose
	private String activationScript;
	@Expose
	private String id;
	@Expose
	private String idfToolsPath;
	@Expose
	private String name;
	@Expose
	private String path;
	@Expose
	private String python;
	
	public String getActivationScript()
	{
		return activationScript;
	}

	public void setActivationScript(String activationScript)
	{
		this.activationScript = activationScript;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getIdfToolsPath()
	{
		return idfToolsPath;
	}

	public void setIdfToolsPath(String idfToolsPath)
	{
		this.idfToolsPath = idfToolsPath;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getPython()
	{
		return python;
	}

	public void setPython(String python)
	{
		this.python = python;
	}

}
