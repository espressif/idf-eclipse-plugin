package com.espressif.idf.debug.gdbjtag.openocd.ui.gcov;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IPageLayout;

import com.espressif.idf.ui.handlers.EclipseHandler;

public class GcovUtility
{
	public static IResource getProject(ExecutionEvent event)
	{
		IResource project = EclipseHandler.getSelectedProject(IPageLayout.ID_PROJECT_EXPLORER);
		if (project == null)
		{
			project = EclipseHandler.getSelectedResource((IEvaluationContext) event.getApplicationContext());
		}
		
		return project;
	}
}
