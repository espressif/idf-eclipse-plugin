/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain.enums;

/**
 * Architecture enum for ESP-IDF toolchains
 * 
 * @author Ali Azam Rana
 *
 */
public enum Architecture
{
	XTENSA("xtensa", "xtensa-esp-elf", "xtensa-%s-elf"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	RISCV32("riscv32", "riscv32-esp-elf", null); //$NON-NLS-1$//$NON-NLS-2$

	private final String id; // e.g., "xtensa" / "riscv32"
	private final String unifiedDirName; // e.g., "xtensa-esp-elf" / "riscv32-esp-elf"
	private final String prefixTemplate; // e.g., "xtensa-%s-elf" or null when fixed

	Architecture(String id, String unifiedDirName, String prefixTemplate)
	{
		this.id = id;
		this.unifiedDirName = unifiedDirName;
		this.prefixTemplate = prefixTemplate;
	}

	public String id()
	{
		return id;
	}

	public String unifiedDirName()
	{
		return unifiedDirName;
	}

	/** For XTENSA, returns "xtensa-<idfName>-elf"; for RISCV, fixed "riscv32-esp-elf". */
	public String toolchainPrefixFor(Target t)
	{
		return prefixTemplate == null ? unifiedDirName : String.format(prefixTemplate, t.idfName());
	}

	public String executablePrefixFor(Target t)
	{
		return toolchainPrefixFor(t);
	}

	public String targetSpecificDirFor(Target t)
	{
		return toolchainPrefixFor(t);
	}
}
