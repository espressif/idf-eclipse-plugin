package com.espressif.idf.ui.tools;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerView;

public class ManageEspIdfVersionsHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		// Get the window directly from the event (safest way in handlers)
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

		if (window != null)
		{
			openManagerView(window);
		}
		return null;
	}

	private void openManagerView(IWorkbenchWindow window)
	{
		Display.getDefault().asyncExec(() -> {
			try
			{
				window.getActivePage().showView(ESPIDFManagerView.VIEW_ID);
			}
			catch (PartInitException e)
			{
				Logger.log("Failed to open ESP-IDF Manager View", e);
			}
		});
	}
}
