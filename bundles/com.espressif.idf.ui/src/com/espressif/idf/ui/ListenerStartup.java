/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.handlers.FileOpenListener;

public class ListenerStartup implements IStartup
{
	private FileOpenListener partListener;

	@Override
	public void earlyStartup()
	{
		IWorkbench workbench = PlatformUI.getWorkbench();

		// Execute on the UI thread
		workbench.getDisplay().asyncExec(() -> {
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window != null)
			{
				IWorkbenchPage page = window.getActivePage();
				if (page != null)
				{
					partListener = new FileOpenListener();
					page.addPartListener(partListener);
				}
			}
		});
	}
}
