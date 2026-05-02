package com.espressif.idf.launch.serial.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.ILaunchDefaultsContributor;
import com.espressif.idf.core.util.LaunchUtil;

public class IDFCoreLaunchConfigProvider extends CoreBuildGenericLaunchConfigProvider
{

	private Map<IProject, Map<String, ILaunchConfiguration>> configs = new HashMap<>();

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException
	{
		IProject project = descriptor.getAdapter(IProject.class);
		if (project == null)
			return null;

		String targetConfig = descriptor.getName();
		Map<String, ILaunchConfiguration> projectConfigs = configs.computeIfAbsent(project, key -> new HashMap<>());
		ILaunchConfiguration configuration = projectConfigs.get(targetConfig);
		configuration = configuration == null ? new LaunchUtil(DebugPlugin.getDefault().getLaunchManager())
				.findAppropriateLaunchConfig(descriptor, IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE) : configuration;
		configuration = configuration == null ? createLaunchConfiguration(descriptor, target) : configuration;
		projectConfigs.put(configuration.getName(), configuration);

		return configuration;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException
	{
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		// Set the project
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null && project.exists())
		{
			workingCopy.setMappedResources(new IResource[] { project });
			workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());

			org.eclipse.cdt.core.model.ICProject cProject = org.eclipse.cdt.core.CCorePlugin.getDefault().getCoreModel()
					.create(project);
			if (cProject != null && cProject.exists())
			{
				org.eclipse.cdt.core.settings.model.ICProjectDescription projDes = org.eclipse.cdt.core.CCorePlugin
						.getDefault().getProjectDescription(cProject.getProject());

				if (projDes != null && projDes.getActiveConfiguration() != null)
				{
					String buildConfigID = projDes.getActiveConfiguration().getId();
					workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID,
							buildConfigID);
				}
			}

			// 3. Ensure Build Before Launch is enabled
			workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH,
					ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING);
		}

		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("com.espressif.idf.core.launchDefaultsContributor"); //$NON-NLS-1$

		for (IConfigurationElement element : elements)
		{
			try
			{
				Object obj = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (obj instanceof ILaunchDefaultsContributor launchDefaultsContributor)
				{
					launchDefaultsContributor.applyDefaults(workingCopy);
				}
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
		}

		workingCopy.doSave();
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException
	{
		if (configuration.getMappedResources() == null)
		{
			return false;
		}
		IProject project = configuration.getMappedResources()[0].getProject();
		if (project != null && !project.isOpen())
		{
			return true;
		}
		if (configuration.exists())
		{
			configs.computeIfAbsent(project, key -> new HashMap<>()).put(configuration.getName(), configuration);
		}

		return ownsLaunchConfiguration(configuration);
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException
	{
		IDFUtil.updateProjectBuildFolder(configuration.getWorkingCopy());

		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException
	{
		IProject project = descriptor.getAdapter(IProject.class);
		if (project == null)
		{
			return;
		}
		Map<String, ILaunchConfiguration> projectConfigs = configs.get(project);
		if (projectConfigs != null)
		{
			projectConfigs.remove(descriptor.getName());
		}

	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException
	{
		// Nothing to do
	}
}
