/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

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

	@Override
	public void resourceChanged(IResourceChangeEvent event)
	{
		String idfPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PATH);
		boolean checkForMarkers = Platform.getPreferencesService().getBoolean(IDFCorePlugin.PLUGIN_ID,
				IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS,
				IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS_DEFAULT_STATUS, null);
		if (!checkForMarkers)
			return;
		Set<IProject> projects = new HashSet<IProject>();

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
				projects.add(file.getProject());
				if (file.exists() && delta.getKind() == IResourceChangeEvent.POST_CHANGE
						&& delta.getMarker().isSubtypeOf(IMarker.PROBLEM) && Integer.parseInt(
								delta.getMarker().getAttribute(IMarker.SEVERITY).toString()) == IMarker.SEVERITY_ERROR)
				{
					if (file.isLinked(IResource.CHECK_ANCESTORS))
					{
						IPath originalPath = file.getRawLocation();
						if (originalPath != null && originalPath.toOSString().startsWith(idfPath))
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
		ProjectRefreshJob projectRefreshJob = new ProjectRefreshJob(projects);
		projectRefreshJob.schedule();
	}

	public void initialMarkerCleanup() throws CoreException
	{
		String idfPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PATH);
		IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		Set<IProject> projects = new HashSet<IProject>();
		for (IMarker marker : markers)
		{
			if (!(marker.getResource() instanceof IFile))
				continue;
			
			IFile file = (IFile) marker.getResource();
			projects.add(file.getProject());
			if (file.isLinked(IResource.CHECK_ANCESTORS))
			{
				IPath originalPath = file.getRawLocation();
				if (originalPath != null && originalPath.toOSString().startsWith(idfPath))
				{
					// Scheduling marker cleanup job for the file
					MarkerCleanupJob markerCleanupJob = new MarkerCleanupJob(file, marker);
					markerCleanupJob.schedule();
				}
			}
		}
		ProjectRefreshJob projectRefreshJob = new ProjectRefreshJob(projects);
		projectRefreshJob.schedule();
	}
	
	private class ProjectRefreshJob extends Job
	{
		private Set<IProject> projects;
		private ProjectRefreshJob(Set<IProject> projects)
		{
			super(Messages.RefreshingProjects_JobName);
			this.projects = projects;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor)
		{
			for(IProject project : projects)
			{
				try
				{
					project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				}
				catch (CoreException e)
				{
					Logger.log(e);
				}
			}
			return Status.OK_STATUS;
		}
		
	}
}
