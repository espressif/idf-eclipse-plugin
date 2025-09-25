/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain.enums;

import java.util.Locale;

/**
 * Target enum for ESP-IDF toolchains
 * 
 * @author Ali Azam Rana
 *
 */
public enum Target
{
	ESP32("esp32", Architecture.XTENSA), //$NON-NLS-1$
	ESP32S2("esp32s2", Architecture.XTENSA), //$NON-NLS-1$
	ESP32S3("esp32s3", Architecture.XTENSA), //$NON-NLS-1$
	ESP32C2("esp32c2", Architecture.RISCV32), //$NON-NLS-1$
	ESP32C3("esp32c3", Architecture.RISCV32), //$NON-NLS-1$
	ESP32C6("esp32c6", Architecture.RISCV32), //$NON-NLS-1$
	ESP32H2("esp32h2", Architecture.RISCV32), //$NON-NLS-1$
	ESP32P4("esp32p4", Architecture.RISCV32); //$NON-NLS-1$

	private static final String CMAKE_TOOLCHAIN_FILE_TEMPLATE = "toolchain-%s.cmake"; //$NON-NLS-1$

	private final String idfName;
	private final Architecture arch;

	Target(String idfName, Architecture arch)
	{
		this.idfName = idfName;
		this.arch = arch;
	}

	public String idfName()
	{
		return idfName;
	}

	public Architecture arch()
	{
		return arch;
	}

	public static Target fromString(String s)
	{
		if (s == null)
			return null;
		String key = s.toLowerCase(Locale.ROOT);
		for (Target t : values())
		{
			if (t.idfName.equals(key))
				return t;
		}
		return null;
	}

	// ----- Derived values (replace former constants/methods) -----

	public String architectureId()
	{
		return arch.id();
	}

	/** e.g., "xtensa-esp32-elf" or "riscv32-esp-elf" */
	public String toolchainId()
	{
		return arch.toolchainPrefixFor(this);
	}

	/** e.g., "xtensa-esp32-elf" or "riscv32-esp-elf" */
	public String executablePrefix()
	{
		return arch.executablePrefixFor(this);
	}

	/** e.g., "xtensa-esp32-elf" (legacy per-target) or "riscv32-esp-elf" */
	public String targetSpecificDirName()
	{
		return arch.targetSpecificDirFor(this);
	}

	/** e.g., "xtensa-esp-elf" or "riscv32-esp-elf" (unified in v5.5+) */
	public String unifiedDirName()
	{
		return arch.unifiedDirName();
	}

	/** e.g., "toolchain-esp32c6.cmake" */
	public String toolchainFileName()
	{
		return String.format(CMAKE_TOOLCHAIN_FILE_TEMPLATE, idfName);
	}

	/** Matches both legacy and unified layouts, ending with -gcc(.exe) */
	public String compilerPattern()
	{
		String exe = executablePrefix();
		String legacy = targetSpecificDirName();
		String unified = unifiedDirName();
		return "(?:" + legacy + "|" + unified + ")[\\\\/]+bin[\\\\/]+" + exe + "-gcc(?:\\.exe)?$"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public String debuggerPattern()
	{
		return executablePrefix() + "-gdb(?:\\.exe)?$"; //$NON-NLS-1$
	}
}
