package com.espressif.idf.ui.installcomponents.vo;

import com.google.gson.annotations.SerializedName;

public class ComponentDetailsDependenciesVO
{
	private boolean isPublic;

	private String name;
	
	private String namespace;
	
	private String source;
	
	private String spec;

	public boolean isPublic()
	{
		return isPublic;
	}

	public void setPublic(boolean isPublic)
	{
		this.isPublic = isPublic;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getSpec()
	{
		return spec;
	}

	public void setSpec(String spec)
	{
		this.spec = spec;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}
}
