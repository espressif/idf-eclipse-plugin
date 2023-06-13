/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESP32S2CMakeToolChainProvider extends AbstractESPCMakeToolChainProvider
{

	public static final String TOOLCHAIN_NAME = "toolchain-esp32s2.cmake"; //$NON-NLS-1$

	@Override
	protected IToolChain getToolchain() throws CoreException
	{
		return tcManager.getToolChain(ESPToolChainProvider.ID, ESP32S2ToolChain.ID);
	}

}