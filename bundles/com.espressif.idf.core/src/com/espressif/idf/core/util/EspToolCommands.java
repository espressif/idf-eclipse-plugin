/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.util;

import java.util.ArrayList;
import java.util.List;

import com.espressif.idf.core.IDFConstants;

/**
 * Class to get the commands and processes for the esptool.py
 * @author Ali Azam Rana
 *
 */
public class EspToolCommands
{
	private Process chipInfoProcess;
	private Process flashEraseProcess;

	public Process chipInformation(String port) throws Exception
	{
		destroyAnyChipInfoProcess();
		chipInfoProcess = new ProcessBuilder(getChipInfoCommand(port)).start();
		return chipInfoProcess;
	}

	public Process eraseFlash(String port) throws Exception
	{
		destroyAnyChipInfoProcess();
		flashEraseProcess = new ProcessBuilder(getFlashEraseCommand(port)).start();
		return flashEraseProcess;
	}

	private List<String> getChipInfoCommand(String port)
	{
		List<String> command = new ArrayList<String>();
		command.add(IDFUtil.getIDFPythonEnvPath());
		command.add(IDFUtil.getEspToolScriptFile().getAbsolutePath());
		command.add("-p"); //$NON-NLS-1$
		command.add(port);
		command.add(IDFConstants.ESP_TOOL_CHIP_ID_CMD);
		return command;
	}

	private List<String> getFlashEraseCommand(String port)
	{
		List<String> command = new ArrayList<String>();
		command.add(IDFUtil.getIDFPythonEnvPath());
		command.add(IDFUtil.getEspToolScriptFile().getAbsolutePath());
		command.add("-p"); //$NON-NLS-1$
		command.add(port);
		command.add(IDFConstants.ESP_TOOL_ERASE_FLASH_CMD);
		return command;
	}

	private void destroyAnyChipInfoProcess()
	{
		if (chipInfoProcess != null && chipInfoProcess.isAlive())
		{
			chipInfoProcess.destroy();
		}
	}

	public boolean checkActiveFlashEraseProcess()
	{
		if (flashEraseProcess != null)
		{
			return flashEraseProcess.isAlive();
		}

		return false;
	}

	public void killEraseFlashProcess()
	{
		if (flashEraseProcess != null)
		{
			flashEraseProcess.destroy();
		}
	}
}
