/*******************************************************************************
 * Copyright 2021-2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/


package com.espressif.idf.ui.menuitem;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.espressif.idf.ui.menuitem.listener.LanguageItemSelectionListener;

public class LanguageDynamicMenuItem extends ContributionItem
{
	private LanguageItemSelectionListener languageItemSelectionListener;

	public LanguageDynamicMenuItem()
	{
		languageItemSelectionListener = new LanguageItemSelectionListener();
	}

	public LanguageDynamicMenuItem(String id)
	{
		super(id);
		languageItemSelectionListener = new LanguageItemSelectionListener();
	}

	@Override
	public void fill(Menu menu, int index)
	{
		IConfigurationElement[] configElements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("com.espressif.idf.ui.extensionspoint.languagepoint");
		for (IConfigurationElement iConfigurationElement : configElements)
		{
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			String langCode = iConfigurationElement.getAttribute("id");
			String langName = iConfigurationElement.getAttribute("name");
			menuItem.setText(langName + " - " + langCode);
			if (languageItemSelectionListener == null)
			{
				languageItemSelectionListener = new LanguageItemSelectionListener();
			}
			
			menuItem.addSelectionListener(languageItemSelectionListener);
		}
	}

}
