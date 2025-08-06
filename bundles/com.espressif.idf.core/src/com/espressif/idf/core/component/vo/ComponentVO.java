package com.espressif.idf.core.component.vo;

public class ComponentVO
{
	private String name;
	private String namespace;
	private String version;
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getNamespace()
	{
		return namespace;
	}
	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}
	public String getVersion()
	{
		return version;
	}
	public void setVersion(String version)
	{
		this.version = version;
	}
}
