/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.configparser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.configparser.vo.Board;
import com.espressif.idf.core.configparser.vo.EspConfig;
import com.espressif.idf.core.configparser.vo.Option;
import com.espressif.idf.core.configparser.vo.Target;
import com.espressif.idf.core.logging.Logger;
import com.google.gson.Gson;

public class EspConfigParser
{

	private final String espConfigPath;
	private final Gson gson = new Gson();
	private final EspConfig config;

	public EspConfigParser()
	{
		this.espConfigPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS)
				+ "/esp-config.json"; //$NON-NLS-1$
		this.config = loadConfig();
	}

	public EspConfigParser(String espConfigPath)
	{
		this.espConfigPath = espConfigPath;
		this.config = loadConfig();
	}

	private EspConfig loadConfig()
	{
		File file = new File(espConfigPath);
		if (!file.exists())
		{
			Logger.log("esp-config.json not found at: " + espConfigPath); //$NON-NLS-1$
			return null;
		}

		try (FileReader reader = new FileReader(file))
		{
			return gson.fromJson(reader, EspConfig.class);
		}
		catch (IOException e)
		{
			Logger.log(e);
			return null;
		}
	}

	public Set<String> getTargets()
	{
		Set<String> targets = new LinkedHashSet<>();
		if (config == null || config.targets() == null)
			return targets;

		for (Target target : config.targets())
		{
			if (target.id() != null)
			{
				targets.add(target.id());
			}
		}
		return targets;
	}

	public List<String> getEspFlashVoltages()
	{
		List<String> voltages = new ArrayList<>();
		if (config == null || config.options() == null)
			return voltages;

		for (Option option : config.options())
		{
			if ("ESP_FLASH_VOLTAGE".equals(option.name()) && option.values() != null) //$NON-NLS-1$
			{
				voltages.addAll(option.values());
				break;
			}
		}
		return voltages;
	}

	public List<Board> getBoardsForTarget(String target)
	{
		List<Board> boardsForTarget = new ArrayList<>();
		if (config == null || config.boards() == null)
			return boardsForTarget;

		for (Board board : config.boards())
		{
			if (target.equals(board.target()) && board.name() != null && board.config_files() != null)
			{
				boardsForTarget.add(board);
			}
		}
		return boardsForTarget;
	}

	public boolean hasBoardConfigJson()
	{
		return new File(espConfigPath).exists();
	}
}
