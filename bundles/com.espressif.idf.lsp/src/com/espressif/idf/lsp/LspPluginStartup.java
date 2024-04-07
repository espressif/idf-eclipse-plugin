/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.lsp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.ui.IStartup;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
public class LspPluginStartup implements IStartup
{

	@Override
	public void earlyStartup()
	{
		CCorePlugin.getIndexManager().setDefaultIndexerId(IPDOMManager.ID_NO_INDEXER);
	}

}
