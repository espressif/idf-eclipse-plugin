/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import java.io.IOException;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public interface ITemplateManager
{

	/**
	 * @return ITemplateNode root node which contains IDF examples as children
	 */
	ITemplateNode getTemplates();

	/**
	 * @param template
	 * @return README description for a given project template
	 * @throws IOException
	 */
	String getDescription(ITemplateNode template) throws IOException;
}
