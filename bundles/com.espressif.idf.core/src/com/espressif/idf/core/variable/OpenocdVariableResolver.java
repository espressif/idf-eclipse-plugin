package com.espressif.idf.core.variable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.embedcdt.core.EclipseUtils;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;

public class OpenocdVariableResolver implements IDynamicVariableResolver
{
	private static final String OPENOCD_PREFIX = "com.espressif.idf.debug.gdbjtag.openocd"; //$NON-NLS-1$
	private static final String INSTALL_FOLDER = "install.folder"; //$NON-NLS-1$

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException
	{
		return getAppropriateEnumVariable(variable).map(this::resolveForOpenocdDynamicEnum).orElse(variable.getName());
	}

	private Optional<OpenocdDynamicVariable> getAppropriateEnumVariable(IDynamicVariable variable)
	{
		return Arrays.stream(OpenocdDynamicVariable.values()).filter(v -> v.getValue().equals(variable.getName()))
				.findAny();
	}

	private String resolveForOpenocdDynamicEnum(OpenocdDynamicVariable enumVariable)
	{
		switch (enumVariable)
		{
		case OPENOCD_PATH:
			ILaunchConfiguration configuration = getActiveLaunchConfiguration();
			Path openocdBinPath = getOpenocdBinPath(configuration);
			return openocdBinPath.getParent().toString();

		case OPENOCD_EXE:
			return File.separator + "bin" + File.separator + "openocd"; //$NON-NLS-1$ //$NON-NLS-2$

		case OPENOCD_SCRIPTS:
			return new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS);

		default:
			return StringUtil.EMPTY;
		}
	}

	private ILaunchConfiguration getActiveLaunchConfiguration()
	{
		try
		{
			return IDFCorePlugin.getService(ILaunchBarManager.class).getActiveLaunchConfiguration();
		}
		catch (CoreException e)
		{
			Logger.log(e);
			return null;
		}
	}

	private Path getOpenocdBinPath(ILaunchConfiguration configuration)
	{
		String installFolder = EclipseUtils.getPreferenceValueForId(OPENOCD_PREFIX, INSTALL_FOLDER, "", //$NON-NLS-1$
				EclipseUtils.getProjectByLaunchConfiguration(configuration));
		return Paths.get(installFolder);
	}
}
