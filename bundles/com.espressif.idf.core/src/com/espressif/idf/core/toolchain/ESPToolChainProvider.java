/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.toolchain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESPToolChainProvider implements IToolChainProvider
{

	/*
	 * ESP Toolchain provider registered in the extension point
	 */
	public static final String ID = "com.espressif.idf.core.esp.toolchainprovider"; //$NON-NLS-1$

	public static final Pattern CLANG_PATTERN = Pattern.compile("xtensa-esp32(.*)-elf-clang(\\.exe)?"); //$NON-NLS-1$ xtensa-esp32s2-elf-readelf
	// esp32, esp32s2, esp32s3
	public static final Pattern GCC_PATTERN = Pattern.compile("xtensa-esp32(.*)-elf-gcc(\\.exe)?"); //$NON-NLS-1$ xtensa-esp32s2-elf-readelf
	public static final Pattern GDB_PATTERN = Pattern.compile("xtensa-esp32(.*)-elf-gdb(\\.exe)?"); //$NON-NLS-1$

	// esp32c3, esp32c2, esp32h2, esp32c6
	public static final Pattern GCC_PATTERN_ESP32C3 = Pattern.compile("riscv32-esp-elf-gcc(\\.exe)?"); //$NON-NLS-1$ //riscv32-esp-elf-gcc
	public static final Pattern GDB_PATTERN_ESP32C3 = Pattern.compile("riscv32-esp-elf-gdb(\\.exe)?"); //$NON-NLS-1$

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) throws CoreException
	{
		ESPToolChainManager espToolChainManager = new ESPToolChainManager();
		espToolChainManager.initToolChain(manager, this);

	}

	public static Collection<Pattern> getToolchainPatterns()
	{
		List<Pattern> list = new ArrayList<>();
		list.add(CLANG_PATTERN);
		list.add(GCC_PATTERN);
		list.add(GCC_PATTERN_ESP32C3);

		return list;
	}

}