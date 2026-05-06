/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.espressif.idf.core.logging.Logger;

public class OpenOcdVersionManager
{
	private OpenOcdVersionManager()
	{
		/* This utility class should not be instantiated */
	}

	private static final Map<String, OpenOcdVersion> versionCache = new ConcurrentHashMap<>();

	private static final Pattern VERSION_PATTERN = Pattern.compile(
			"(?i)(?:Open On-Chip Debugger\\s+|v)(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?(?:-[a-z0-9]+-(?<build>\\d+))?"); //$NON-NLS-1$

	public static class OpenOcdVersion
	{
		public final int major;
		public final int minor;
		public final int patch;
		public final int buildDate;

		public OpenOcdVersion(int major, int minor, int patch, int buildDate)
		{
			this.major = major;
			this.minor = minor;
			this.patch = patch;
			this.buildDate = buildDate;
		}

		public boolean isAtLeast(int targetMajor, int targetMinor)
		{
			return isAtLeast(targetMajor, targetMinor, 0);
		}

		public boolean isAtLeast(int targetMajor, int targetMinor, int targetPatch)
		{

			if (this.major > targetMajor)
				return true;
			if (this.major == targetMajor && this.minor > targetMinor)
				return true;
			return (this.major == targetMajor && this.minor == targetMinor && this.patch >= targetPatch);
		}

		public boolean isBuildDateAtLeast(int targetMajor, int targetMinor, int targetBuildDate)
		{
			// If strictly newer major/minor, we assume the feature exists
			if (this.major > targetMajor)
				return true;
			if (this.major == targetMajor && this.minor > targetMinor)
				return true;

			// If exactly the same major/minor, check the build date
			if (this.major == targetMajor && this.minor == targetMinor)
			{
				return this.buildDate >= targetBuildDate;
			}
			return false;
		}

		@Override
		public String toString()
		{
			return major + "." + minor + "." + patch + " (Build: " + buildDate + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	public static OpenOcdVersion getVersion(String executablePath)
	{
		if (executablePath == null || executablePath.isEmpty())
		{
			return new OpenOcdVersion(0, 0, 0, 0);
		}
		return versionCache.computeIfAbsent(executablePath, OpenOcdVersionManager::fetchVersionFromProcess);
	}

	private static OpenOcdVersion fetchVersionFromProcess(String executablePath)
	{
		try
		{
			ProcessBuilder pb = new ProcessBuilder(executablePath, "--version"); //$NON-NLS-1$
			pb.redirectErrorStream(true);
			Process process = pb.start();

			try
			{
				if (!process.waitFor(2, TimeUnit.SECONDS))
				{
					return new OpenOcdVersion(0, 0, 0, 0);
				}

				try (BufferedReader reader = process.inputReader())
				{
					String output = reader.lines().collect(Collectors.joining("\n")); //$NON-NLS-1$
					return parseVersionString(output);
				}
			} finally
			{
				if (process.isAlive())
				{
					process.destroyForcibly();
				}
			}
		}
		catch (IOException e)
		{
			Logger.log("Failed to execute or parse OpenOCD version fallback for path: " + executablePath, e); //$NON-NLS-1$
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			Logger.log("Thread was interrupted while waiting for OpenOCD process to exit.", e); //$NON-NLS-1$
		}

		return new OpenOcdVersion(0, 0, 0, 0);
	}

	public static OpenOcdVersion parseVersionString(String output)
	{
		if (output == null || output.trim().isEmpty())
		{
			return new OpenOcdVersion(0, 0, 0, 0);
		}

		Matcher matcher = VERSION_PATTERN.matcher(output);
		if (matcher.find())
		{
			int major = Integer.parseInt(matcher.group("major")); //$NON-NLS-1$
			int minor = Integer.parseInt(matcher.group("minor")); //$NON-NLS-1$
			int patch = matcher.group("patch") != null ? Integer.parseInt(matcher.group("patch")) : 0; //$NON-NLS-1$ //$NON-NLS-2$
			int buildDate = matcher.group("build") != null ? Integer.parseInt(matcher.group("build")) : 0; //$NON-NLS-1$//$NON-NLS-2$

			return new OpenOcdVersion(major, minor, patch, buildDate);
		}

		return new OpenOcdVersion(0, 0, 0, 0);
	}
}
