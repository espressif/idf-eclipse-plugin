/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.ui.test.operations.EnvSetupOperations;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class EspressifMenuTest
{

	private static SWTWorkbenchBot bot;

	@Before
	public void beforeClass() throws Exception
	{
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.SCREENSHOTS_DIR = "screenshots/EspressifMenu/";
		UIThreadRunnable.syncExec(new VoidResult()
		{
			public void run()
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
			}
		});
		bot = new SWTWorkbenchBot();
		bot.shell().activate();
	}

	@Test
	public void testProductInformation() throws Exception
	{
		bot.shell().activate();
		EnvSetupOperations.setupEspressifEnv(bot);
		bot.viewByTitle("Project Explorer").show();
		bot.menu("Espressif").menu("Product Information").click();	
	}

}
