/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.toolchain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espressif.idf.core.logging.Logger;

/**
 * Class to read ESP-IDF targets from constants.py file
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFTargetsReader
{
	private static final String CONSTANTS_FILE_PATH = "tools/idf_py_actions/constants.py";
	private static final Pattern SUPPORTED_TARGETS_PATTERN = Pattern.compile("SUPPORTED_TARGETS\\s*=\\s*\\[([^\\]]*)\\]", Pattern.MULTILINE);
	private static final Pattern PREVIEW_TARGETS_PATTERN = Pattern.compile("PREVIEW_TARGETS\\s*=\\s*\\[([^\\]]*)\\]", Pattern.MULTILINE);
	
	/**
	 * Read ESP-IDF targets from the constants.py file
	 * @param idfPath ESP-IDF installation path
	 * @return IDFTargets object containing all targets
	 */
	public static IDFTargets readTargetsFromEspIdf(String idfPath)
	{
		IDFTargets targets = new IDFTargets();
		
		if (idfPath == null || idfPath.trim().isEmpty())
		{
			Logger.log("ESP-IDF path is null or empty, cannot read targets");
			return targets;
		}
		
		Path constantsFilePath = Paths.get(idfPath, CONSTANTS_FILE_PATH);
		
		if (!Files.exists(constantsFilePath))
		{
			Logger.log("Constants file not found at: " + constantsFilePath);
			return targets;
		}
		
		try
		{
			String content = new String(Files.readAllBytes(constantsFilePath));
			
			// Extract supported targets
			List<String> supportedTargets = extractTargets(content, SUPPORTED_TARGETS_PATTERN);
			for (String target : supportedTargets)
			{
				targets.addSupportedTarget(target.trim());
			}
			
			// Extract preview targets
			List<String> previewTargets = extractTargets(content, PREVIEW_TARGETS_PATTERN);
			for (String target : previewTargets)
			{
				targets.addPreviewTarget(target.trim());
			}
			
			Logger.log("Successfully read " + supportedTargets.size() + " supported targets and " + previewTargets.size() + " preview targets");
			
		}
		catch (IOException e)
		{
			Logger.log("Error reading constants file: " + e.getMessage());
		}
		catch (Exception e)
		{
			Logger.log("Unexpected error reading targets: " + e.getMessage());
		}
		
		return targets;
	}
	
	/**
	 * Extract target names from the constants.py content using regex pattern
	 * @param content File content as string
	 * @param pattern Regex pattern to match
	 * @return List of target names
	 */
	private static List<String> extractTargets(String content, Pattern pattern)
	{
		List<String> targets = new ArrayList<>();
		
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
		{
			String targetsString = matcher.group(1);
			// Split by comma and clean up
			String[] targetArray = targetsString.split(",");
			for (String target : targetArray)
			{
				// Remove quotes, whitespace and filter empty strings
				String cleanTarget = target.replaceAll("['\"\\s]", "").trim();
				if (!cleanTarget.isEmpty())
				{
					targets.add(cleanTarget);
				}
			}
		}
		
		return targets;
	}
}
