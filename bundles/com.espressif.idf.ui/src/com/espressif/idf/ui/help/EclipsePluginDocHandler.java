/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.help;

import java.io.IOException;
import java.net.MalformedURLException;
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
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class EclipsePluginDocHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			IWebBrowser browser = support.getExternalBrowser();
			if (browser != null) {
				URL docsUrl = getDocsUrl();
				Logger.log(docsUrl.getPath());
				browser.openURL(docsUrl);
			}
		} catch (PartInitException | IOException e) {
			Logger.log(e);
		}
		return null;
	}

	protected URL getDocsUrl() throws MalformedURLException {
		return new URL("https://github.com/espressif/idf-eclipse-plugin#esp-idf-eclipse-plugin"); //$NON-NLS-1$
	}

}
