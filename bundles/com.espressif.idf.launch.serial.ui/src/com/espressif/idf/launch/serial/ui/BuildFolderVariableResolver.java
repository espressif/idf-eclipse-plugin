/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.launch.serial.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFDynamicVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.launch.serial.ui.internal.Activator;

/**
 * Class to resolve the build folder introduced for the flashing arguments as the cmake -B build directory when added to
 * cmake build arguments needs to be added to the flash arguments as well
 *
 * @author Ali Azam Rana
 *
 */
public class BuildFolderVariableResolver implements IDynamicVariableResolver
{

	private static final ILaunchBarManager LAUNCH_BAR_MANAGER = Activator.getService(ILaunchBarManager.class);

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException
	{
		IDFDynamicVariables idfVariable = IDFDynamicVariables.valueOf(variable.getName());
		if (idfVariable != IDFDynamicVariables.BUILD_DIR)
		{
			throw new RuntimeException("Invalid Variable for Resolver"); //$NON-NLS-1$
		}

		Logger.log("Resolving for variable: " + idfVariable.name()); //$NON-NLS-1$
		String buildFolder = "build"; //$NON-NLS-1$
		ILaunchConfiguration launchConfiguration = LAUNCH_BAR_MANAGER.getActiveLaunchConfiguration();
		IResource[] mappedResources = launchConfiguration.getMappedResources();
		if (mappedResources != null && mappedResources[0].getProject() != null)
		{
			IProject project = mappedResources[0].getProject();
			String projectBuildFolder = IDFUtil.getBuildDir(project);
			if (!StringUtil.isEmpty(projectBuildFolder))
				buildFolder = projectBuildFolder;
		}

		return buildFolder;
	}

}
