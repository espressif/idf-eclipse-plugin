/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd;

import org.eclipse.ui.IStartup;

import com.espressif.idf.debug.gdbjtag.openocd.preferences.DefaultPreferenceInitializer;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class OpenOCDPluginStartup implements IStartup
{

	@Override
	public void earlyStartup()
	{
		new DefaultPreferenceInitializer().initializeDefaultPreferences();
	}

}
