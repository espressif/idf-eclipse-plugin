/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.build;

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

	public static final String ID = "com.espressif.idf.core.esp.toolchainprovider"; //$NON-NLS-1$
	public static final Pattern GCC_PATTERN = Pattern.compile("xtensa-esp32(.*)-elf-gcc(\\.exe)?"); //$NON-NLS-1$ xtensa-esp32s2-elf-readelf

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

}