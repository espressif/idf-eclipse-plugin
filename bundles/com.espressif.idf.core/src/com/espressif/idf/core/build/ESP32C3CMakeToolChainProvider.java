/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESP32C3CMakeToolChainProvider extends AbstractESPCMakeToolChainProvider
{

	public static final String TOOLCHAIN_NAME = "toolchain-esp32c3.cmake"; //$NON-NLS-1$

	protected IToolChain getToolchain() throws CoreException
	{
		IToolChain toolChain = tcManager.getToolChain(ESPToolChainProvider.ID, ESP32C3ToolChain.ID);
		return toolChain;
	}

}