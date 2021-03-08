/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESP32CMakeToolChainProvider extends AbstractESPCMakeToolChainProvider
{

	public static final String TOOLCHAIN_NAME = "toolchain-esp32.cmake"; //$NON-NLS-1$

	@Override
	protected IToolChain getToolchain() throws CoreException
	{
		return tcManager.getToolChain(ESPToolChainProvider.ID, ESP32ToolChain.ID);
	}

}