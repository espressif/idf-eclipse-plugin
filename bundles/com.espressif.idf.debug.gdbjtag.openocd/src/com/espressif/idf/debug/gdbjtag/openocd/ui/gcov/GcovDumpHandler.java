package com.espressif.idf.debug.gdbjtag.openocd.ui.gcov;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IPageLayout;

import com.espressif.idf.debug.gdbjtag.openocd.ui.gcov.dialog.GcovDialog;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.handlers.EclipseHandler;

public class GcovDumpHandler extends AbstractHandler
{
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		GcovDialog gcovDialog = new GcovDialog(EclipseUtil.getShell(), EclipseHandler.getSelectedProject(IPageLayout.ID_PROJECT_EXPLORER));
		gcovDialog.open();
		return null;
	}
}
