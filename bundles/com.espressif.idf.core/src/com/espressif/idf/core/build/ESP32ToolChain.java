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
public class ESP32ToolChain extends AbstractESPToolchain
{

	public static final String ID = "xtensa-esp32-elf"; //$NON-NLS-1$
	public static final String OS = "esp32"; //$NON-NLS-1$
	public static final String ARCH = "xtensa"; //$NON-NLS-1$

	public ESP32ToolChain(IToolChainProvider provider, Path pathToToolChain)
	{
		super(provider, pathToToolChain, OS, ARCH);
		setProperty("ATTR_ID", ESP32CMakeToolChainProvider.TOOLCHAIN_NAME); //$NON-NLS-1$
	}

}
