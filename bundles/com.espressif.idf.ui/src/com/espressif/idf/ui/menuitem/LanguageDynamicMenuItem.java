/*******************************************************************************
 * Copyright 2021-2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.menuitem;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.EclipseIniUtil;

public class LanguageDynamicMenuItem extends ContributionItem
{
	private static final String LANGUAGE_SWITCH = "-nl"; //$NON-NLS-1$
	private EclipseIniUtil eclipseIniUtil;

	public LanguageDynamicMenuItem()
	{
		try
		{
			eclipseIniUtil = new EclipseIniUtil();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	@Override
	public void fill(Menu menu, int index)
	{
		IConfigurationElement[] configElements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("com.espressif.idf.ui.locale.support");
		for (IConfigurationElement iConfigurationElement : configElements)
		{
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			String langCode = iConfigurationElement.getAttribute("id"); //$NON-NLS-1$
			String langName = iConfigurationElement.getAttribute("name"); //$NON-NLS-1$
			menuItem.setText(langName);

			menuItem.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent selectionEvent)
				{
					if (eclipseIniUtil != null)
					{
						try
						{
							if (eclipseIniUtil.containsEclipseSwitchInEclipseIni(LANGUAGE_SWITCH))
							{
								if (!eclipseIniUtil.getEclipseIniSwitchValue(LANGUAGE_SWITCH).equals(langCode))
								{
									eclipseIniUtil.setEclipseSwitchValue(LANGUAGE_SWITCH, langCode);
									PlatformUI.getWorkbench().restart(false);
								}
							}
						}
						catch (Exception e)
						{
							Logger.log(e);
						}

					}
				}
			});
		}
	}

}
