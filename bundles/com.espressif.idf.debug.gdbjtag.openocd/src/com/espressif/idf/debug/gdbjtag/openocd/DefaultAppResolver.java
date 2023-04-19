package com.espressif.idf.debug.gdbjtag.openocd;

import java.io.File;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.util.GenericJsonReader;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

public class DefaultAppResolver implements IDynamicVariableResolver
{

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException
	{
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		ILaunchConfiguration activeConfig = launchBarManager.getActiveLaunchConfiguration();
		String projectName = activeConfig.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				StringUtil.EMPTY);
		IProject activeProject = CoreModel.getDefault().getCModel().getCProject(projectName).getProject();
		GenericJsonReader jsonReader = new GenericJsonReader(
				IDFUtil.getBuildDir(activeProject) + File.separator + IDFConstants.PROECT_DESCRIPTION_JSON);
		String value = jsonReader.getValue("app_elf"); //$NON-NLS-1$

		return IDFUtil.getBuildDir(activeProject) + File.separator + value;
	}

}
