/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.common;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

/**
 * Since eclipse is started only once per project its good to have the shared bot between tests
 * @author Ali Azam Rana
 *
 */
public class WorkBenchSWTBot
{
	private static SWTWorkbenchBot swtWorkbenchBot;
	
	
	public static SWTWorkbenchBot getBot()
	{
		if (swtWorkbenchBot == null)
		{
			swtWorkbenchBot = new SWTWorkbenchBot();
		}
		
		return swtWorkbenchBot;
	}
}
