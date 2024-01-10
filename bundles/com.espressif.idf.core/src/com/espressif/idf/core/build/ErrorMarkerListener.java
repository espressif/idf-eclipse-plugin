/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;

/**
 * Marker listener that can listen to added markers in the problems view and start a job to remove them from the derived
 * files in ide folder for esp_idf components
 * 
 * @author Ali Azam Rana
 *
 */
public class ErrorMarkerListener implements IResourceChangeListener
{
	private static String IDF_PATH;

	public ErrorMarkerListener()
	{
		IDF_PATH = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PATH);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event)
	{
		boolean checkForMarkers = Platform.getPreferencesService().getBoolean(IDFCorePlugin.PLUGIN_ID,
				IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS,
				IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS_DEFAULT_STATUS, null);
		if (!checkForMarkers)
			return;

		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(IMarker.MARKER, true);
		for (IMarkerDelta delta : markerDeltas)
		{
			try
			{
				if (!(delta.getMarker().getResource() instanceof IFile))
				{
					return;
				}

				IFile file = (IFile) delta.getMarker().getResource();
				if (file.exists() && delta.getKind() == IResourceChangeEvent.POST_CHANGE
						&& delta.getMarker().isSubtypeOf(IMarker.PROBLEM) && Integer.valueOf(
								delta.getMarker().getAttribute(IMarker.SEVERITY).toString()) == IMarker.SEVERITY_ERROR)
				{
					if (file.isLinked(IResource.CHECK_ANCESTORS))
					{
						IPath originalPath = file.getRawLocation();
						if (originalPath != null && originalPath.toOSString().startsWith(IDF_PATH))
						{
							// Scheduling marker cleanup job for the file
							MarkerCleanupJob markerCleanupJob = new MarkerCleanupJob(file, delta.getMarker());
							markerCleanupJob.schedule();
						}
					}
				}
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
		}
	}

	public void initialMarkerCleanup() throws CoreException
	{
		IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		for (IMarker marker : markers)
		{
			if (!(marker.getResource() instanceof IFile))
				continue;
			
			IFile file = (IFile) marker.getResource();
			if (file.isLinked(IResource.CHECK_ANCESTORS))
			{
				IPath originalPath = file.getRawLocation();
				if (originalPath != null && originalPath.toOSString().startsWith(IDF_PATH))
				{
					// Scheduling marker cleanup job for the file
					MarkerCleanupJob markerCleanupJob = new MarkerCleanupJob(file, marker);
					markerCleanupJob.schedule();
				}
			}
		}
		
	}
}
