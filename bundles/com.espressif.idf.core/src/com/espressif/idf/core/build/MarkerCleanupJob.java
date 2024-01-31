/*******************************************************************************
 * Copyright 2023-2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;

/**
 * Error marker clean up job that runs to clean the error markers for the esp_idf_components folder files that are
 * actually derived ones from the esp-idf and are not user generated. This is actually done to improve the UX as CDT
 * indexer shows unwanted errros
 * 
 * @author Ali Azam Rana
 *
 */
public class MarkerCleanupJob extends Job
{
	private IFile file;
	private IMarker marker;

	public MarkerCleanupJob(IFile file, IMarker marker) throws CoreException
	{
		super("Marker: " + marker .getAttribute(IMarker.MESSAGE, StringUtil.EMPTY) + " Cleanup for: " + file.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		this.file = file;
		this.marker = marker ;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		try
		{
			Logger.log("Cleaning marker: " + marker.getAttribute(IMarker.MESSAGE, StringUtil.EMPTY) + " for: " + file.getName()); //$NON-NLS-1$ //$NON-NLS-2$
			file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return Status.OK_STATUS;
	}
}
