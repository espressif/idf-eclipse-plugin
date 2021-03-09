/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.IToolChainProvider;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESP32C3ToolChain extends AbstractESPToolchain
{
	/**
	 * Toolchain target ID
	 */
	public static final String ID = "riscv32-esp-elf"; //$NON-NLS-1$

	/**
	 * Property: The OS the toolchain builds for.
	 */
	public static final String OS = "esp32c3"; //$NON-NLS-1$

	/**
	 * Property: The CPU architecture the toolchain supports.
	 */
	public static final String ARCH = "riscv32"; //$NON-NLS-1$

	/**
	 * @param provider
	 * @param pathToToolChain
	 */
	public ESP32C3ToolChain(IToolChainProvider provider, Path pathToToolChain)
	{
		super(provider, pathToToolChain, OS, ARCH);
	}

}
