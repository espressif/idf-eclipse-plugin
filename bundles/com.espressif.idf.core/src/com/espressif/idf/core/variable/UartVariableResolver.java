package com.espressif.idf.core.variable;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;

public class UartVariableResolver implements IDynamicVariableResolver
{

	public String resolveValue(IDynamicVariable variable, String argument)
	{
		return getAppropriateEnumVariable(variable).map(this::resolveForDynamicEnum).orElse(variable.getName());
	}

	private Optional<UartDynamicVariable> getAppropriateEnumVariable(IDynamicVariable variable)
	{
		return Arrays.stream(UartDynamicVariable.values()).filter(v -> v.getValue().equals(variable.getName()))
				.findFirst();
	}

	private String resolveForDynamicEnum(UartDynamicVariable enumVariable)
	{

		return switch (enumVariable)
		{
		case SERIAL_PORT -> getSerialPort(); // $NON-NLS-1$
		};
	}

	private String getSerialPort()
	{
		return getActiveLaunchTarget().orElseGet(() -> ILaunchTarget.NULL_TARGET)
				.getAttribute(LaunchBarTargetConstants.SERIAL_PORT, StringUtil.EMPTY);
	}

	private Optional<ILaunchTarget> getActiveLaunchTarget()
	{

		try
		{
			return Optional.of(IDFCorePlugin.getService(ILaunchBarManager.class).getActiveLaunchTarget());
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return Optional.empty();
	}

}
