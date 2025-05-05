package com.espressif.idf.core.tools.vo;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.Expose;

public class EimJson
{
	@Expose
	private String eimPath;
	@Expose
	private String gitPath;
	@Expose
	private String idfSelectedId;
	@Expose
	private List<IdfInstalled> idfInstalled = Collections.emptyList();

	public String getGitPath()
	{
		return gitPath;
	}

	public void setGitPath(String gitPath)
	{
		this.gitPath = gitPath;
	}

	public String getIdfSelectedId()
	{
		return idfSelectedId;
	}

	public void setIdfSelectedId(String idfSelectedId)
	{
		this.idfSelectedId = idfSelectedId;
	}

	public List<IdfInstalled> getIdfInstalled()
	{
		return idfInstalled;
	}

	public void setIdfInstalled(List<IdfInstalled> idfInstalled)
	{
		this.idfInstalled = idfInstalled;
	}

	public String getEimPath()
	{
		return eimPath;
	}

	public void setEimPath(String eimPath)
	{
		this.eimPath = eimPath;
	}

}
