/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager.render;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.tools.eimjson.presentation.EimInstallationPresentation;

/**
 * Maps neutral {@link EimInstallationPresentation} to SWT colours for the manager table.
 */
public final class EimInstallationStatusSwtMapper
{
	private EimInstallationStatusSwtMapper()
	{
	}

	public static Color foreground(EimInstallationPresentation presentation, Display display)
	{
		return switch (presentation.getStatusKind())
		{
			case ACTIVE -> display.getSystemColor(SWT.COLOR_DARK_GREEN);
			case SETTING_UP, IN_PROGRESS, BEING_REPAIRED -> display.getSystemColor(SWT.COLOR_DARK_YELLOW);
			case FAILED, BROKEN -> display.getSystemColor(SWT.COLOR_RED);
			case INACTIVE -> display.getSystemColor(SWT.COLOR_GRAY);
		};
	}
}
