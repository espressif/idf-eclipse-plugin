/*******************************************************************************
 * Copyright 2021-2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.menuitem;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.EclipseIniUtil;
import com.espressif.idf.ui.handlers.Messages;

/**
 * Language dynamic menu item class to fill the menu with 
 * available languages and handle the event when clicked on them
 * 
 * @author Ali Azam Rana
 *
 */
public class LanguageDynamicMenuItem extends ContributionItem
{
	private static final String LANGUAGE_SWITCH = "-nl"; //$NON-NLS-1$
//	private static final String ECLIPSE_RCP_NAME = "espressif-ide"; //$NON-NLS-1$
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
				.getConfigurationElementsFor("com.espressif.idf.ui.locale.support"); //$NON-NLS-1$
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
									restartEclipse();
								}
							}
							else
							{
								eclipseIniUtil.setEclipseSwitchValue(LANGUAGE_SWITCH, langCode);
								restartEclipse();
							}
						}
						catch (Exception e)
						{
							MessageDialog.openError(Display.getDefault().getActiveShell(),
									Messages.LanguageChange_ErrorTitle, Messages.LanguageChange_ErrorMessage);
							Logger.log(e);
						}

					}
				}
			});
		}
	}

	private void restartEclipse() throws Exception
	{
		URL eclipseInstallationUrl = new URL(
				Platform.getInstallLocation().getURL() + System.getProperty("eclipse.launcher.name")); //$NON-NLS-1$
		String pathToEclipse = new File(eclipseInstallationUrl.toURI()).toString();
		ProcessBuilder processBuilder = new ProcessBuilder(pathToEclipse);
		processBuilder.start();
		PlatformUI.getWorkbench().close();
	}
}
