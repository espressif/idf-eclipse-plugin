/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.lsp.ClangdConfigFileHandler;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ClangdConfigCreateHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		// get the selected project
		IResource project = EclipseHandler.getSelectedProject(IPageLayout.ID_PROJECT_EXPLORER);
		if (project == null)
		{
			project = EclipseHandler.getSelectedResource((IEvaluationContext) event.getApplicationContext());
		}

		if (project == null)
		{
			throw new ExecutionException(Messages.ClangdConfigCreateHandler_NoProjectFound);
		}

		updateClangdFile((IProject) project);
		return null;
	}

	private void updateClangdFile(IProject project)
	{
		try
		{
			new ClangdConfigFileHandler().update(project);

			MessageDialog.openInformation(Display.getDefault().getActiveShell(),
					Messages.ClangdConfigCreateHandler_MsgTitle, Messages.ClangdConfigCreateHandler_FileCreationMsg);

		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

}
