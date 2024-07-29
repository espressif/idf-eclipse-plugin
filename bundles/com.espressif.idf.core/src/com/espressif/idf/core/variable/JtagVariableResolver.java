/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
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
import com.espressif.idf.core.util.EspConfigParser;
import com.espressif.idf.core.util.StringUtil;

public class JtagVariableResolver implements IDynamicVariableResolver
{

	public String resolveValue(IDynamicVariable variable, String argument)
	{
		return getAppropriateEnumVariable(variable).map(this::resolveForDynamicEnum).orElse(variable.getName());
	}

	private Optional<JtagDynamicVariable> getAppropriateEnumVariable(IDynamicVariable variable)
	{
		return Arrays.stream(JtagDynamicVariable.values()).filter(v -> v.name().equals(variable.getName())).findFirst();
	}

	private String resolveForDynamicEnum(JtagDynamicVariable enumVariable)
	{

		return switch (enumVariable)
		{
		case JTAG_FLASH_ARGS -> generatePartOfConfigOptionsForVoltage() + generatePartOfConfigOptionsForBoard(); // $NON-NLS-1$
		};
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

	private String generatePartOfConfigOptionsForVoltage()
	{
		ILaunchTarget activeILaunchTarget = getActiveLaunchTarget().orElseGet(() -> ILaunchTarget.NULL_TARGET);
		var selectedVoltage = activeILaunchTarget.getAttribute(LaunchBarTargetConstants.FLASH_VOLTAGE,
				StringUtil.EMPTY);
		return selectedVoltage.equals("default") ? StringUtil.EMPTY //$NON-NLS-1$
				: String.format("-c 'set ESP32_FLASH_VOLTAGE' %s' ", selectedVoltage); //$NON-NLS-1$

	}

	private String generatePartOfConfigOptionsForBoard()
	{
		ILaunchTarget activeILaunchTarget = getActiveLaunchTarget().orElseGet(() -> ILaunchTarget.NULL_TARGET);
		var parser = new EspConfigParser();
		var boardConfigMap = parser
				.getBoardsConfigs(activeILaunchTarget.getAttribute(LaunchBarTargetConstants.TARGET, StringUtil.EMPTY));
		var board = activeILaunchTarget.getAttribute(LaunchBarTargetConstants.BOARD, StringUtil.EMPTY);
		var boardConfigs = boardConfigMap.get(board);
		var result = new StringBuilder();
		var iterator = boardConfigs.iterator();
		while (iterator.hasNext())
		{
			result.append(String.format("-f %s ", iterator.next()));//$NON-NLS-1$
		}
		return result.toString();
	}

}
