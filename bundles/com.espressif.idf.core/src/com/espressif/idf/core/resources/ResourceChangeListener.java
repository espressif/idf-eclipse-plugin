package com.espressif.idf.core.resources;

import java.io.File;
import java.util.Iterator;
import java.util.Optional;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager2;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.build.ESP32S2ToolChain;
import com.espressif.idf.core.build.ESP32ToolChain;
import com.espressif.idf.core.logging.Logger;

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
							Preferences settings = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node("config")
									.node(project.getName()).node(project.getActiveBuildConfig().getName()); //$NON-NLS-1$
							IToolChainManager toolChainManager = CCorePlugin.getService(IToolChainManager.class);
							IToolChain toolChain = getESPToolChain(toolChainManager);
							settings.put(ICBuildConfiguration.TOOLCHAIN_TYPE,
									Optional.ofNullable(toolChain).map(o -> o.getTypeId()).orElse("")); //$NON-NLS-1$
							settings.put(ICBuildConfiguration.TOOLCHAIN_ID,
									Optional.ofNullable(toolChain).map(o -> o.getId()).orElse("")); //$NON-NLS-1$
							recheckConfigs();
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
		File buildLocation = new File(project.getLocation() + "/build"); //$NON-NLS-1$
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

	private void recheckConfigs()
	{
		ICBuildConfigurationManager mgr = CCorePlugin.getService(ICBuildConfigurationManager.class);
		ICBuildConfigurationManager2 manager = (ICBuildConfigurationManager2) mgr;
		manager.recheckConfigs();
	}
	
	private IToolChain getESPToolChain(IToolChainManager toolChainManager) throws CoreException
	{
		Iterator<IToolChain> iter = toolChainManager.getAllToolChains().iterator();
		IToolChain toolChain = null;
		while (iter.hasNext())
		{
			toolChain = iter.next();
			if (toolChain instanceof ESP32ToolChain ||  toolChain instanceof ESP32S2ToolChain) //TODO: remove specific conditions
			{
				return toolChain;
			}
		}
		return toolChain;
	}
}


