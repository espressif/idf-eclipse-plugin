/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.util.ArrayList;
import java.util.List;

import com.espressif.idf.core.IDFConstants;

/**
 * List installed tools and versions command handler
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ListInstalledToolsHandler extends AbstractToolsHandler
{

	@Override
	protected void execute()
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_LIST_CMD);

		runCommand(arguments);
	}

}
