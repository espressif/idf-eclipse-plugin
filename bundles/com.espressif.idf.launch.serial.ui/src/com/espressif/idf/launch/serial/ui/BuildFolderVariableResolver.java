/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.launch.serial.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.espressif.idf.core.IDFDynamicVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * Class to resolve the build folder introduced for the flashing arguments as the cmake -B build directory when added to
 * cmake build arguments needs to be added to the flash arguments as well
 *
 * @author Ali Azam Rana
 *
 */
public class BuildFolderVariableResolver implements IDynamicVariableResolver
{

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
		IProject project = IDFUtil.getProjectFromActiveLaunchConfig();
		if (project != null)
		{
			String projectBuildFolder = IDFUtil.getBuildDir(project);
			if (!StringUtil.isEmpty(projectBuildFolder))
				buildFolder = projectBuildFolder;
		}

		if (buildFolder != null && buildFolder.contains(" ")) //$NON-NLS-1$
		{
			buildFolder = "\"" + buildFolder + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return buildFolder;
	}

}
