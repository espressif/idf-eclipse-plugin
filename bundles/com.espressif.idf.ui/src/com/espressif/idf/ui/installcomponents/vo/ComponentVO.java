/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents.vo;

/**
 * Bean for the API response JSON for components
 * 
 * @author Ali Azam Rana
 *
 */
public class ComponentVO
{
	private String createdAt;

	private boolean featured;

	private String name;

	private String namespace;

	private ComponentDetailsVO componentDetails;
	
	private boolean isComponentAdded;

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

	public boolean isComponentAdded()
	{
		return isComponentAdded;
	}

	public void setComponentAdded(boolean isComponentAdded)
	{
		this.isComponentAdded = isComponentAdded;
	}
}
