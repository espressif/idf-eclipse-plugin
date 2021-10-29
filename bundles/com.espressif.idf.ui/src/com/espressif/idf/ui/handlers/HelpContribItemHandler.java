/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.awt.Desktop;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.espressif.idf.core.logging.Logger;

/**
 * Help button handler class for status bar
 * 
 * @author Ali Azam Rana
 *
 */
public class HelpContribItemHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		String url = "https://www.esp32.com/viewforum.php?f=40"; //$NON-NLS-1$
		try
		{
			Desktop.getDesktop().browse(new URL(url).toURI());
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}
}
