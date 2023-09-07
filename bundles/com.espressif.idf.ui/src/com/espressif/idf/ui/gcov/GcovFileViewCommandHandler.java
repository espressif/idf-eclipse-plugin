/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.gcov;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.GcovUtility;
import com.espressif.idf.ui.UIPlugin;

/**
 * Handler for the gcov view menu command
 * @author Ali Azam Rana
 *
 */
public class GcovFileViewCommandHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
		{
			IWorkbenchPage page = window.getActivePage();
			if (page != null)
			{
				try
				{
					GcovUtility.clearSelectedProject();
					page.showView(GcovFileView.ID);
				}
				catch (PartInitException e)
				{
					Logger.log(UIPlugin.getDefault(), "Failed to initialize GcovFileView: " + e.getMessage(), e);
				}
			}
		}
		return null;
	}

}
