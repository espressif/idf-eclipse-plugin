/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.espressif.idf.sdk.config.core.KConfigMenuItem;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ConfigLabelProvider extends LabelProvider
{

	public static final String SETTINGS_ICON = "icons/settings.png"; //$NON-NLS-1$

	public Image getImage(Object element)
	{
		if (element instanceof KConfigMenuItem)
		{
			return SDKConfigUIPlugin.getImage(SETTINGS_ICON);
		}

		return null;
	}

	/*
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element)
	{
		if (element instanceof KConfigMenuItem)
		{
			return ((KConfigMenuItem) element).getTitle();
		}

		return null;
	}

}
