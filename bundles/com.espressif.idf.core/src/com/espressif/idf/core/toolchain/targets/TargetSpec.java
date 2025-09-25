/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain.targets;

/**
 * Common API implemented by known enum {@link Target} and dynamic preview targets.
 * 
 * @author Ali Azam Rana
 *
 */
public interface TargetSpec
{

	/** The ESP-IDF target identifier, e.g., "esp32c6". */
	String idfName();

	/** CPU architecture of this target. */
	Architecture arch();

	// -------- Derived defaults shared by both enum and dynamic implementations --------

	/** "xtensa" or "riscv32". */
	default String architectureId()
	{
		return arch().id();
	}

	/** e.g., "xtensa-esp32-elf" or "riscv32-esp-elf". */
	default String toolchainId()
	{
		return arch().toolchainPrefixFor(this);
	}

	/** e.g., "xtensa-esp32-elf" or "riscv32-esp-elf". */
	default String executablePrefix()
	{
		return arch().executablePrefixFor(this);
	}

	/** Legacy per-target directory name (same as executable/toolchain prefix historically). */
	default String targetSpecificDirName()
	{
		return arch().targetSpecificDirFor(this);
	}

	/** Unified toolchain directory name (IDF v5.5+), e.g., "xtensa-esp-elf". */
	default String unifiedDirName()
	{
		return arch().unifiedDirName();
	}

	/** e.g., "toolchain-esp32c6.cmake". */
	default String toolchainFileName()
	{
		return String.format("toolchain-%s.cmake", idfName()); //$NON-NLS-1$
	}

	/** Matches legacy/unified dirs and ends with -gcc(.exe). */
	default String compilerPattern()
	{
		String exe = executablePrefix();
		String legacy = targetSpecificDirName();
		String unified = unifiedDirName();
		return "(?:" + legacy + "|" + unified + ")[\\\\/]+bin[\\\\/]+" + exe + "-gcc(?:\\.exe)?$"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/** Matches <prefix>-gdb(.exe). */
	default String debuggerPattern()
	{
		return executablePrefix() + "-gdb(?:\\.exe)?$"; //$NON-NLS-1$
	}
}
