/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

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
		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
		String url = "https://www.esp32.com/viewforum.php?f=40"; //$NON-NLS-1$
		try
		{
			IWebBrowser browser = support.getExternalBrowser();
			if (browser != null)
			{
				URL docsUrl = new URL(url);
				Logger.log(docsUrl.getPath());
				browser.openURL(docsUrl);
			}
		}
		catch (
				PartInitException
				| IOException e)
		{
			Logger.log(e);
		}
		return null;
	}
}
