/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.handlers.EclipseHandler;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeMemoryHandler extends AbstractHandler
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
			Logger.log("There is no project selected in the Project Explorer");
			return null;
		}

		IPath mapFilePath = IDFUtil.getMapFilePath((IProject) project);
		Logger.log("Mapping file path " + mapFilePath); //$NON-NLS-1$
		if (mapFilePath == null || !mapFilePath.toFile().exists())
		{
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", //$NON-NLS-1$
					"Could not find .map file for project"); //$NON-NLS-1$
			return null;

		}
		try
		{
			launchEditor(mapFilePath, (IProject) project);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		return null;
	}

	private void launchEditor(IPath mapFilePath, IProject project) throws CoreException
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile iFile = workspace.getRoot().getFileForLocation(mapFilePath);
		if (mapFilePath.toFile().exists() && iFile == null) // file is located outside of the workspace
		{
			// create a link in the project/build/ folder to open file in eclipse editor
			IFolder buildRootFolder = project.getFolder(IDFConstants.BUILD_FOLDER);
			iFile = buildRootFolder.getFile(mapFilePath.lastSegment());
			if (!iFile.exists())
			{
				IProgressMonitor monitor = new NullProgressMonitor();
				if (!buildRootFolder.exists())
				{
					buildRootFolder.create(IResource.FORCE | IResource.DERIVED, true, monitor);
				}
				iFile.createLink(mapFilePath, IResource.NONE, null);
			}
		}
		FileEditorInput editorInput = new FileEditorInput(iFile);
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
				try
				{
					IDE.openEditor(activeww.getActivePage(), editorInput, IDFSizeAnalysisEditor.EDITOR_ID);
				}
				catch (PartInitException e)
				{
					Logger.log(e);
				}
			}
		});
	}

}
