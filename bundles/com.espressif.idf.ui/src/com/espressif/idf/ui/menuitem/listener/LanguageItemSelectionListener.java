/*******************************************************************************
 * Copyright 2021-2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.menuitem.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.util.EclipseIniUtil;

public class LanguageItemSelectionListener extends SelectionAdapter
{
	private static final String LANGUAGE_SWITCH = "-nl";
	
	EclipseIniUtil eclipseIniUtil;

	public LanguageItemSelectionListener()
	{
		try
		{
			eclipseIniUtil = new EclipseIniUtil();
		}
		catch (Exception e)
		{
			IDFCorePlugin.getPlugin().getLog().error(e.getMessage());
		}
	}

	@Override
	public void widgetSelected(SelectionEvent selectionEvent)
	{
		MenuItem currentItem = ((MenuItem) selectionEvent.widget);
		String code = currentItem.getText().split("-")[1].replaceAll("^[ \t]+|[ \t]+$", "");
		if (eclipseIniUtil != null)
		{
			try
			{
				if (currentItem.getText().toLowerCase().contains("default"))
				{
					if (eclipseIniUtil.containsEclipseSwitchInEclipseIni(LANGUAGE_SWITCH))
					{
						eclipseIniUtil.removeEclipseSwitch(LANGUAGE_SWITCH);
					}
				}
				else
				{
					eclipseIniUtil.setEclipseSwitchValue(LANGUAGE_SWITCH, code);
				}
				PlatformUI.getWorkbench().restart(false);
			}
			catch (Exception e)
			{
				IDFCorePlugin.getPlugin().getLog().error(e.getMessage());
			}

		}
	}
}
