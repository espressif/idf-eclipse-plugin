/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.launch.serial.util;

import java.util.ArrayList;
import java.util.List;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.util.IDFUtil;

public class EspFlashCommandGenerator {
	/**
	 * @param launch
	 * @return command to flash the application
	 */
	public static String getEspFlashCommand(String serialPort) {

		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("-p"); //$NON-NLS-1$
		commands.add(serialPort);
		commands.add(IDFConstants.FLASH_CMD);

		return String.join(" ", commands); //$NON-NLS-1$
	}
}
