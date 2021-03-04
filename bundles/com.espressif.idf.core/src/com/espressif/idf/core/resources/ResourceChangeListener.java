package com.espressif.idf.core.resources;

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
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.build.ESP32S2ToolChain;
import com.espressif.idf.core.build.ESPToolChain;
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
				public boolean visit(IResourceDelta delta) throws CoreException
				{
					boolean isProjectRenamed = delta.getResource().getType() == IResource.PROJECT
							&& delta.getKind() == IResourceDelta.ADDED
							&& ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0);

					boolean isProjectOpenedOrCopied = delta.getResource().getType() == IResource.PROJECT
							&& delta.getFlags() == IResourceDelta.OPEN;

					if (isProjectOpenedOrCopied || isProjectRenamed)
					{
						IProject project = (IProject) delta.getResource();
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
			if (toolChain instanceof ESPToolChain ||  toolChain instanceof ESP32S2ToolChain) 
			{
				return toolChain;
			}
		}
		return toolChain;
	}
}


