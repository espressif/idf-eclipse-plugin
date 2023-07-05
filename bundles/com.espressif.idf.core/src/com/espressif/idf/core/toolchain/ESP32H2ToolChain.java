/*******************************************************************************

 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.IToolChainProvider;

public class ESP32H2ToolChain extends AbstractESPToolchain
{
	/**
	 * Toolchain target ID
	 */
	public static final String ID = "riscv32-esp-elf"; //$NON-NLS-1$

	/**
	 * Property: The OS the toolchain builds for.
	 */
	public static final String OS = "esp32h2"; //$NON-NLS-1$

	/**
	 * Property: The CPU architecture the toolchain supports.
	 */
	public static final String ARCH = "riscv32"; //$NON-NLS-1$

	/**
	 * @param provider
	 * @param pathToToolChain
	 */
	public ESP32H2ToolChain(IToolChainProvider provider, Path pathToToolChain)
	{
		super(provider, pathToToolChain, OS, ARCH);
	}

	@Override
	public String getId()
	{
		// TODO Auto-generated method stub
		return super.getId() + "-" + OS;
	}

}
