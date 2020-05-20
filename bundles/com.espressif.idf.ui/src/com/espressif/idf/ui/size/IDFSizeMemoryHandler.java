/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.espressif.idf.core.logging.Logger;
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
			return null;
		}

		IPath mapFilePath = getMapFilePath((IProject) project);
		Logger.log("Mapping file path " + mapFilePath); //$NON-NLS-1$
		if (mapFilePath == null)
		{
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", //$NON-NLS-1$
					"Project mapping file doesn't exist"); //$NON-NLS-1$
			return null;

		}
		launchEditor(mapFilePath);

		return null;
	}

	private void launchEditor(IPath mapFilePath)
	{

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile iFile = workspace.getRoot().getFileForLocation(mapFilePath);
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

	protected IPath getMapFilePath(IProject project)
	{
		IPath mapPath = project.getLocation().append("build").append(project.getName() + ".map"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!mapPath.toFile().exists())
		{
			File buildDir = project.getLocation().append("build").toFile(); //$NON-NLS-1$
			if (buildDir.exists())
			{
				// search for .map file
				File[] fileList = buildDir.listFiles();
				for (File file : fileList)
				{
					if (file.getName().endsWith(".map")) // $NON-NLS-1$
					{
						return new Path(file.getAbsolutePath());
					}
				}
			}
		}
		return null;
	}

}
