/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;

public class LaunchTargetHelper
{
	private static String lastSavedTargetName;

	public static void saveTargetName(String targetName)
	{
		lastSavedTargetName = targetName;
	}

	public static Optional<String> getLastTargetName()
	{
		return Optional.ofNullable(lastSavedTargetName);
	}

	public static ILaunchTarget findLaunchTargetByName(ILaunchTargetManager launchTargetManager, String targetName)
	{
		ILaunchTarget[] targets = launchTargetManager
				.getLaunchTargetsOfType("com.espressif.idf.launch.serial.core.serialFlashTarget"); //$NON-NLS-1$

		return streamTargetsByName(targetName, targets).findFirst().orElse(null);
	}

	public static Optional<ILaunchTarget> findSuitableTargetForSelectedItem(ILaunchTargetManager launchTargetManager,
			ILaunchBarManager launchBarManager, String selectedItem)
	{
		ILaunchTarget[] targets = launchTargetManager
				.getLaunchTargetsOfType("com.espressif.idf.launch.serial.core.serialFlashTarget"); //$NON-NLS-1$

		String suitableSerialPort = getSerialPort(launchBarManager);

		Stream<ILaunchTarget> launchTargetStream = streamTargetsByName(selectedItem, targets);
		return launchTargetStream
				.filter(target -> suitableSerialPort
						.equals(target.getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY)))
				.findFirst().or(() -> streamTargetsByName(selectedItem, targets).findFirst());
	}

	private static Stream<ILaunchTarget> streamTargetsByName(String selectedItem, ILaunchTarget[] targets)
	{
		return Stream.of(targets).filter(target -> selectedItem
				.equals(target.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY)));
	}

	private static String getSerialPort(ILaunchBarManager launchBarManager)
	{

		String serialPort = StringUtil.EMPTY;
		try
		{
			serialPort = launchBarManager.getActiveLaunchTarget().getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT,
					StringUtil.EMPTY);

		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return serialPort;
	}

}
