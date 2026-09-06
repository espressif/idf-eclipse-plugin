package com.espressif.idf.launch.serial.core;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.LaunchDefaults;

public class IDFCoreLaunchConfigProvider extends CoreBuildGenericLaunchConfigProvider
{

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

		LaunchDefaults.apply(workingCopy);
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException
	{
		IDFUtil.updateProjectBuildFolder(configuration.getWorkingCopy());

		return false;
	}
}
