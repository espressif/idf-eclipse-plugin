/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain.targets;

/**
 * CPU architecture metadata with enough info to derive toolchain paths/prefixes.
 * 
 * @author Ali Azam Rana
 *
 */
public enum Architecture
{
	XTENSA("xtensa", "xtensa-esp-elf", "xtensa-%s-elf"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	RISCV32("riscv32", "riscv32-esp-elf", null); //$NON-NLS-1$ //$NON-NLS-2$

	private final String id;
	private final String unifiedDirName;
	private final String prefixTemplate; // null => fixed prefix equals unifiedDirName

	Architecture(String id, String unifiedDirName, String prefixTemplate)
	{
		this.id = id;
		this.unifiedDirName = unifiedDirName;
		this.prefixTemplate = prefixTemplate;
	}

	/** e.g., "xtensa" or "riscv32" */
	public String id()
	{
		return id;
	}

	/** e.g., "xtensa-esp-elf" or "riscv32-esp-elf" */
	public String unifiedDirName()
	{
		return unifiedDirName;
	}

	/** For XTENSA -> "xtensa-<idf>-elf"; for RISCV32 -> "riscv32-esp-elf". */
	public String toolchainPrefixFor(TargetSpec t)
	{
		return prefixTemplate == null ? unifiedDirName : String.format(prefixTemplate, t.idfName());
	}

	/** Historically same as toolchain prefix (binary prefix). */
	public String executablePrefixFor(TargetSpec t)
	{
		return toolchainPrefixFor(t);
	}

	/** Historically same as toolchain prefix (legacy per-target dir). */
	public String targetSpecificDirFor(TargetSpec t)
	{
		return toolchainPrefixFor(t);
	}
}
