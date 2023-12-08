/*******************************************************************************
 * Copyright 2023-2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;

/**
 * Error marker clean up job that runs to clean the error markers for the esp_idf_components
 * folder files that are actually derived ones from the esp-idf and are not user generated.
 * This is actually done to improve the UX as CDT indexer shows unwanted errros
 * @author Ali Azam Rana
 *
 */
public class MarkerCleanupJob extends Job
{
	private IFile file;
	private final String IDF_PATH;

	public MarkerCleanupJob(IFile file)
	{
		super("Marker Cleanup for " + file.getName()); //$NON-NLS-1$
		this.file = file;
		IDF_PATH = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PATH);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		try
		{
			if (file.exists())
			{
				if (file.isLinked(IResource.CHECK_ANCESTORS))
				{
					IPath originalPath = file.getRawLocation();
					if (originalPath != null && originalPath.toOSString().startsWith(IDF_PATH))
					{
						// Clear markers for this specific file
						Logger.log("Cleaning markers for " + file.getName()); //$NON-NLS-1$
						file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
					}
				}
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return Status.OK_STATUS;
	}
}
