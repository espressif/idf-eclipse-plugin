package com.espressif.idf.launch.serial.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.util.LaunchUtil;
import com.espressif.idf.core.util.StringUtil;

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

		String usbLoc = target.getAttribute(IDFLaunchConstants.OPENOCD_USB_LOCATION, (String) null);
		if (!StringUtil.isEmpty(usbLoc)) {
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute(IDFLaunchConstants.OPENOCD_USB_LOCATION, usbLoc);
			configuration = wc.doSave();
			projectConfigs.put(configuration.getName(), configuration);
		}
		return configuration;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException
	{
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		// Set the project
		IProject project = descriptor.getAdapter(IProject.class);
		workingCopy.setMappedResources(new IResource[] { project });
		workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
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
		if (configuration.exists()) {
			configs.computeIfAbsent(project, key -> new HashMap<>()).put(configuration.getName(), configuration);
		}

		return ownsLaunchConfiguration(configuration);
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException
	{
		// nothing to do
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException
	{
		IProject project = descriptor.getAdapter(IProject.class);
		if (project == null) {
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
