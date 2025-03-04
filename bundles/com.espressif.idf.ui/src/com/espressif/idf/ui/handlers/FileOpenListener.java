/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.FileEditorInput;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.LspService;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerEditor;

public class FileOpenListener implements IPartListener2
{

	@Override
	public void partOpened(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef)
	{
		IWorkbenchPart part = partRef.getPart(false);

		if (part instanceof IEditorPart ieditorpart
				&& ieditorpart.getEditorInput() instanceof FileEditorInput fileInput)
		{
			
			if (ieditorpart instanceof ESPIDFManagerEditor)
				return;
			
			IFile file = fileInput.getFile();
			IProject project = file.getProject();
			if (project == null)
				return;
			String buildDir = StringUtil.EMPTY;
			try
			{
				buildDir = IDFUtil.getBuildDir(project);
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
			if (StringUtil.isEmpty(buildDir))
				return;
			LspService lspService = new LspService();
			lspService.updateCompileCommandsDir(buildDir);
			lspService.restartLspServers();

		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef)
	{
	}
}
