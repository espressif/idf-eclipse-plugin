/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.FileEditorInput;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.LspService;
import com.espressif.idf.core.util.StringUtil;

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
			IFile file = fileInput.getFile();
			IProject project = file.getProject();
			if (project == null)
				return;
			
			if (!isSourceOrHeaderFile(file))
			{
				return;
			}
			
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
	
	private boolean isSourceOrHeaderFile(IFile file)
	{
		try
		{
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			IContentType contentType = contentTypeManager.findContentTypeFor(file.getFullPath().toOSString());
			if (contentType != null)
			{
				String id = contentType.getId();
				if ((id.startsWith("org.eclipse.cdt.core.c") && (id.endsWith("Source") || id.endsWith("Header")))) {
                    return true;
                }
			}
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return false;
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
