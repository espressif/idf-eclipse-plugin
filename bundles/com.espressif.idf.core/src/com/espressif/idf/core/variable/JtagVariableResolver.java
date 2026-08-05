/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.variable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.DefaultBoardProvider;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.configparser.EspConfigParser;
import com.espressif.idf.core.configparser.vo.Board;
import com.espressif.idf.core.logging.Logger;
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
		var selectedVoltage = activeILaunchTarget.getAttribute(LaunchBarTargetConstants.FLASH_VOLTAGE, "default"); //$NON-NLS-1$
		return selectedVoltage.equals("default") ? StringUtil.EMPTY //$NON-NLS-1$
				: String.format("-c 'set ESP32_FLASH_VOLTAGE' %s' ", selectedVoltage); //$NON-NLS-1$

	}

	private String generatePartOfConfigOptionsForBoard()
	{
		var result = new StringBuilder();
		for (Object config : resolveBoardConfigFiles())
		{
			result.append(String.format("-f %s ", config)); //$NON-NLS-1$
		}
		return result.toString();
	}

	/**
	 * Resolves the OpenOCD board configuration files for the active launch target. These config files register the
	 * board-specific OpenOCD commands (e.g. {@code program_esp_bins}). An empty result means no usable board is
	 * selected for the active target.
	 *
	 * @return the list of board config files, never {@code null}
	 */
	private List<String> resolveBoardConfigFiles()
	{
		var parser = new EspConfigParser();
		ILaunchTarget activeILaunchTarget = getActiveLaunchTarget().orElseGet(() -> ILaunchTarget.NULL_TARGET);
		var targetName = activeILaunchTarget.getAttribute(LaunchBarTargetConstants.TARGET, StringUtil.EMPTY);
		var board = activeILaunchTarget.getAttribute(LaunchBarTargetConstants.BOARD,
				new DefaultBoardProvider().getDefaultBoard(targetName));
		int idx = board.lastIndexOf(" [usb://"); //$NON-NLS-1$
		String boardKey = (idx != -1) ? board.substring(0, idx) : board;
		List<Board> boards = parser.getBoardsForTarget(targetName);
		return boards.stream().filter(b -> b.name().equals(boardKey)).findFirst().map(Board::config_files)
				.orElse(List.of());
	}

	/**
	 * Checks whether a board configuration is resolvable for the active launch target. When this returns
	 * {@code false}, OpenOCD would start without a board configuration and board-specific commands such as
	 * {@code program_esp_bins} would not be available, so the debug session cannot succeed.
	 *
	 * @return {@code true} if a board is selected and its OpenOCD config files were found, {@code false} otherwise
	 */
	public static boolean isBoardConfigResolvable()
	{
		return !new JtagVariableResolver().resolveBoardConfigFiles().isEmpty();
	}

}
