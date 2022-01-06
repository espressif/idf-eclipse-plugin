/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.resources;

import java.beans.PropertyChangeSupport;

public class OpenDialogListenerSupport
{
	private static PropertyChangeSupport support;

	public static PropertyChangeSupport getSupport()
	{
		if (support == null)
		{
			support = new PropertyChangeSupport(new Object());
		}
		return support;
	}

}