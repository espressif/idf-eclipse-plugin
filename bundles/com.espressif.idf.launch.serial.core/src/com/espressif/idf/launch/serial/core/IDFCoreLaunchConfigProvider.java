package com.espressif.idf.launch.serial.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.IDFCorePlugin;

public class IDFCoreLaunchConfigProvider extends CoreBuildGenericLaunchConfigProvider {

	private Map<IProject, Map<String, ILaunchConfiguration>> configs = new HashMap<>();

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {

		ILaunchConfiguration configuration = null;
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			Map<String, ILaunchConfiguration> projectConfigs = configs.get(project);
			if (projectConfigs == null) {
				projectConfigs = new HashMap<>();
				configs.put(project, projectConfigs);
			}

			String targetConfig = descriptor.getName();
			configuration = projectConfigs.get(targetConfig);
			if (configuration == null) {
				//do we already have one with the descriptor?
				configuration = descriptor.getAdapter(ILaunchConfiguration.class);
				if (configuration == null) {
					configuration = createLaunchConfiguration(descriptor, target);
				}
				projectConfigs.put(configuration.getName(), configuration);
			}
		}
		return configuration;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		// Set the project
		IProject project = descriptor.getAdapter(IProject.class);
		workingCopy.setMappedResources(new IResource[] { project });

	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		IProject project = configuration.getMappedResources()[0].getProject();
		if (project != null && !project.isOpen()) {
			return true;
		}
		if (ownsLaunchConfiguration(configuration)) {

			Map<String, ILaunchConfiguration> projectConfigs = configs.get(project);
			if (projectConfigs == null) {
				projectConfigs = new HashMap<>();
				configs.put(project, projectConfigs);
			}

			projectConfigs.put(configuration.getName(), configuration);
			return true;
		}
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		for (Entry<IProject, Map<String, ILaunchConfiguration>> projectEntry : configs.entrySet()) {
			Map<String, ILaunchConfiguration> projectConfigs = projectEntry.getValue();
			for (Entry<String, ILaunchConfiguration> entry : projectConfigs.entrySet()) {
				if (configuration.equals(entry.getValue())) {
					projectConfigs.remove(entry.getKey());
					if (projectConfigs.isEmpty()) {
						configs.remove(projectEntry.getKey());
						launchBarManager.launchObjectRemoved(projectEntry.getKey());
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		// nothing to do
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			configs.remove(project);
		}
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException {
		//Nothing to do
	}

}
