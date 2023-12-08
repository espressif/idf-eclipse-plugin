/*******************************************************************************
 * Copyright 2023-2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;

/**
 * File Open Listener for the idf_components to remove the error markers 
 * @author Ali Azam Rana
 *
 */
public class FileOpenListener implements IResourceChangeListener
{

	@Override
	public void resourceChanged(IResourceChangeEvent event)
	{
		// Get the active workbench window
		Display.getDefault().asyncExec(() -> {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null)
			{
				IWorkbenchPage page = window.getActivePage();
				if (page != null)
				{
					IEditorPart editor = page.getActiveEditor();
					if (editor != null)
					{
						IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
						if (file != null)
						{
							String filePath = file.getFullPath().toString();
							if (filePath.contains("/ide/esp_idf_components/")) //$NON-NLS-1$
							{
								// Start the job
								MarkerCleanupJob job = new MarkerCleanupJob(file);
								job.schedule();
							}
						}
					}
				}
			}
		});
	}

	private class MarkerCleanupJob extends Job
	{
		private IFile file;

		private MarkerCleanupJob(IFile file)
		{
			super("Marker Cleanup for " + file.getName()); //$NON-NLS-1$
			this.file = file;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor)
		{
			try
			{
				if (file.exists())
				{
					// Clear markers for this specific file
					Logger.log("Cleaning markers for " + file.getName()); //$NON-NLS-1$
					file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);					
				}
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
			return Status.OK_STATUS;
		}
	}
}
