/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;

/**
 * View main class used for tools management
 * 
 * @author Ali Azam Rana
 *
 */
public class ESPIDFManagerView extends ViewPart
{

	public static final String VIEW_ID = "com.espressif.idf.ui.manageespidf"; //$NON-NLS-1$

	private ESPIDFMainTablePage espidfMainTablePage;

	@Override
	public void createPartControl(Composite parent)
	{
		EimIdfConfiguratinParser parser = new EimIdfConfiguratinParser();
		EimJson initialData = null;
		try
		{
			initialData = parser.getEimJson(false);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}

		espidfMainTablePage = new ESPIDFMainTablePage(initialData);
		espidfMainTablePage.createPage(parent);
		espidfMainTablePage.setupInitialEspIdf();
		setPartName(Messages.EspIdfEditorTitle);
	}

	@Override
	public void setFocus()
	{
		if (espidfMainTablePage != null)
		{
			espidfMainTablePage.setFocus();
		}
	}
}
