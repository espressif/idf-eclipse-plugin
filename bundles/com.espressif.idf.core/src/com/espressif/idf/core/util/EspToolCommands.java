/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.util;

import java.io.IOException;
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
	private Process writeFlashProcess;

	public Process chipInformation(String port) throws Exception
	{
		destroyAnyChipInfoProcess();
		ProcessBuilder processBuilder = new ProcessBuilder(getChipInfoCommand(port));
		processBuilder.environment().putAll(IDFUtil.getSystemEnv());
		chipInfoProcess = processBuilder.start();
		return chipInfoProcess;
	}

	public Process eraseFlash(String port) throws Exception
	{
		destroyAnyChipInfoProcess();
		ProcessBuilder processBuilder = new ProcessBuilder(getFlashEraseCommand(port));
		processBuilder.environment().putAll(IDFUtil.getSystemEnv());
		flashEraseProcess = processBuilder.start();
		return flashEraseProcess;
	}

	public Process writeFlash(String port, String path, String offset) throws IOException
	{
		destroyAnyChipInfoProcess();
		ProcessBuilder processBuilder = new ProcessBuilder(getWriteFlashCommand(port, path, offset));
		processBuilder.environment().putAll(IDFUtil.getSystemEnv());
		writeFlashProcess = processBuilder.start();
		return writeFlashProcess;
	}

	private List<String> getWriteFlashCommand(String port, String path, String offset)
	{
		List<String> command = new ArrayList<>();
		command.add(IDFUtil.getIDFPythonEnvPath());
		command.add(IDFUtil.getEspToolScriptFile().getAbsolutePath());
		command.add("-p"); //$NON-NLS-1$
		command.add(port);
		command.add(IDFConstants.ESP_WRITE_FLASH_CMD);
		command.add(offset);
		command.add(path);
		return command;
	}

	private List<String> getChipInfoCommand(String port)
	{
		List<String> command = new ArrayList<>();
		command.add(IDFUtil.getIDFPythonEnvPath());
		command.add(IDFUtil.getEspToolScriptFile().getAbsolutePath());
		command.add("-p"); //$NON-NLS-1$
		command.add(port);
		command.add(IDFConstants.ESP_TOOL_CHIP_ID_CMD);
		return command;
	}

	private List<String> getFlashEraseCommand(String port)
	{
		List<String> command = new ArrayList<>();
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

	public boolean checkActiveWriteFlashProcess()
	{
		if (writeFlashProcess != null)
		{
			return writeFlashProcess.isAlive();
		}

		return false;
	}

	public void killWriteFlashProcess()
	{
		if (writeFlashProcess != null)
		{
			writeFlashProcess.destroy();
		}
	}
}
