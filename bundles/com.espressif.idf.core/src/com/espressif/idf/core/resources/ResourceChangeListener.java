package com.espressif.idf.core.resources;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.RecheckConfigsHelper;

public class ResourceChangeListener implements IResourceChangeListener 
{
	
	@Override
	public void resourceChanged(IResourceChangeEvent event)
	{
		if (event == null || event.getDelta() == null) {
			return;
		}
		
		try
		{
			event.getDelta().accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(final IResourceDelta delta) throws CoreException
				{
					IResource resource = delta.getResource();
					int kind = delta.getKind();
					int flags = delta.getFlags();
					updateLaunchBar(resource, kind, flags);
					boolean isProjectAdded = (((resource.getType() & IResource.PROJECT) != 0)
							&& resource.getProject().isOpen() && kind == IResourceDelta.ADDED);
					if (isProjectAdded)
					{
						cleanupBuildFolder(resource);
					}
					boolean isProjectRenamed = resource.getType() == IResource.PROJECT
							&& kind == IResourceDelta.ADDED && ((flags & IResourceDelta.MOVED_FROM) != 0);

					boolean isProjectOpenedOrCopied = resource.getType() == IResource.PROJECT
							&& ((flags & IResourceDelta.OPEN) != 0);

					if (isProjectOpenedOrCopied || isProjectRenamed)
					{
						IProject project = (IProject) resource;
						if (project.isOpen()) 
						{
							RecheckConfigsHelper.revalidateToolchain(project);
						}
					}
					return true;
				}

			});
				
		} catch (CoreException e)
		{
			Logger.log(e);
		}
		
	}
	
	private void updateLaunchBar(IResource resource, int kind, int flags) throws CoreException
	{
		if (resource instanceof IProject)
		{
			ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
			IProject project = (IProject) resource;

			if ((kind & IResourceDelta.CHANGED) != 0)
			{
				ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
				ILaunchConfiguration[] configs = launchManager.getLaunchConfigurations();
				for (ILaunchConfiguration config : configs)
				{
					IResource[] mappedResource = config.getMappedResources();
					if (mappedResource != null && mappedResource[0].getProject() == project)
					{
						if (project.isOpen())
						{
							launchBarManager.launchConfigurationAdded(config);
						}
						else
						{
							launchBarManager.launchObjectRemoved(config);
						}
					}
				}
				if (project.isOpen())
				{
					launchBarManager.launchObjectAdded(project);
				}
				else
				{
					launchBarManager.launchObjectRemoved(project);
				}
			}
		}
	}

	private void cleanupBuildFolder(IResource resource)
	{

		IProject project = (IProject) resource;
		File buildLocation = new File(project.getLocation() + "/"+ IDFConstants.BUILD_FOLDER); //$NON-NLS-1$
		deleteDirectory(buildLocation);
	}
	
	private boolean deleteDirectory(File directoryToBeDeleted)
	{
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null)
		{
			for (File file : allContents)
			{
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
}


