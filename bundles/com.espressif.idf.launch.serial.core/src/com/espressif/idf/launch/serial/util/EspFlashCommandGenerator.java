/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.launch.serial.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunch;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;
import com.espressif.idf.launch.serial.internal.SerialFlashLaunch;

public class EspFlashCommandGenerator {
	/**
	 * @param launch
	 * @return command to flash the application
	 */
	public static String getEspFlashCommand(ILaunch launch) {

		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("-p"); //$NON-NLS-1$

		String serialPort = ((SerialFlashLaunch) launch).getLaunchTarget()
				.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, ""); //$NON-NLS-1$
		commands.add(serialPort);

		commands.add(IDFConstants.FLASH_CMD);

		return String.join(" ", commands); //$NON-NLS-1$
	}
}
