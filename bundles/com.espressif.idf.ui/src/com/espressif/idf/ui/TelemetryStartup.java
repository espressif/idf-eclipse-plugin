/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import org.eclipse.ui.IStartup;

import com.espressif.idf.core.telemetry.TelemetryService;

/**
 * Reports the anonymous installation, update and session events once the workbench is up.
 *
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TelemetryStartup implements IStartup
{
	@Override
	public void earlyStartup()
	{
		TelemetryNotice.showIfNeeded();
		TelemetryService.getInstance().reportSessionStart();
	}
}
