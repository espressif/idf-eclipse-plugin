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
public class ESP32S3CMakeToolChainProvider extends ESPCMakeToolChainProvider
{

	public static final String TOOLCHAIN_ESP3233_CMAKE = "toolchain-esp32s3.cmake"; //$NON-NLS-1$

	protected IToolChain getToolchain() throws CoreException
	{
		IToolChain toolChain = tcManager.getToolChain(ESPToolChainProvider.ID, ESP32S3ToolChain.ID);
		return toolChain;
	}

}