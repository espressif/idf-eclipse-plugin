package com.espressif.idf.ui.installcomponents.vo;

public class ComponentVO
{
	private String createdAt;
	
	private boolean featured;
	
	private String name;
	
	private String namespace;
	
	private ComponentDetailsVO componentDetails;

	public String getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(String createdAt)
	{
		this.createdAt = createdAt;
	}

	public boolean isFeatured()
	{
		return featured;
	}

	public void setFeatured(boolean featured)
	{
		this.featured = featured;
	}

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

	public ComponentDetailsVO getComponentDetails()
	{
		return componentDetails;
	}

	public void setComponentDetails(ComponentDetailsVO componentDetails)
	{
		this.componentDetails = componentDetails;
	}
}
