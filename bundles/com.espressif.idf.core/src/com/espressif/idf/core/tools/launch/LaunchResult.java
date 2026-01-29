/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.launch;

import java.util.OptionalLong;

/**
 * Launch result abstraction so the rest of the code doesn't care how the process
 * is identified (PID on Win/Linux, PID or execPath polling on macOS, etc).
 * 
 * @author Ali Azam Rana
 */
public final class LaunchResult
{
	private final OptionalLong pid;
	private final String execPath;
	private final String details;

	private LaunchResult(OptionalLong pid, String execPath, String details)
	{
		this.pid = pid;
		this.execPath = execPath;
		this.details = details;
	}

	public static LaunchResult ofPid(long pid, String execPath, String details)
	{
		return new LaunchResult(OptionalLong.of(pid), execPath, details);
	}

	public static LaunchResult ofNoPid(String execPath, String details)
	{
		return new LaunchResult(OptionalLong.empty(), execPath, details);
	}

	public OptionalLong pid()
	{
		return pid;
	}

	/**
	 * Platform-specific executable path (e.g. .../Contents/MacOS/eim on macOS) or
	 * the launched binary path on Linux/Windows.
	 */
	public String execPath()
	{
		return execPath;
	}

	/**
	 * Debug info: launcher output, command used, etc.
	 */
	public String details()
	{
		return details;
	}
}
