/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IResource;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public interface ITemplateNode
{

	/**
	 * @return Template Name
	 */
	String getName();

	/**
	 * @return location of the Template File
	 */
	File getFilePath();

	/**
	 * @return template parent node.
	 */
	ITemplateNode getParent();

	/**
	 * @return Template children if it's a folder.
	 */
	List<ITemplateNode> getChildren();

	/**
	 * @return Template Type it can be {@link IResource.PROJECT} OR {@link IResource.FOLDER} OR {@link IResource.ROOT}
	 */
	int getType();
}
