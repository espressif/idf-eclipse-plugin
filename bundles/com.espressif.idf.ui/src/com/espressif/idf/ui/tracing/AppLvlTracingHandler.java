package com.espressif.idf.ui.tracing;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.espressif.idf.ui.dialogs.EraseFlashDialog;
import com.espressif.idf.ui.handlers.EclipseHandler;

public class AppLvlTracingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IResource project = EclipseHandler.getSelectedProject(IPageLayout.ID_PROJECT_EXPLORER);
		if (project == null)
		{
			project = EclipseHandler.getSelectedResource((IEvaluationContext) event.getApplicationContext());
		}
		
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		AppLvlTracingDialog dialog = new AppLvlTracingDialog(activeShell);
		dialog.setProjectPath(project);
		dialog.create();
		dialog.open();
		return null;
	}

}
