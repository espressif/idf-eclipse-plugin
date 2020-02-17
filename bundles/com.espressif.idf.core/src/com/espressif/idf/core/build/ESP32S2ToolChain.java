/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.nio.file.Path;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainProvider;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESP32S2ToolChain extends GCCToolChain
{

	public static final String ID = "xtensa-esp32s2-elf"; //$NON-NLS-1$
	public static final String OS = "esp32s2"; //$NON-NLS-1$
	public static final String ARCH = "xtensa"; //$NON-NLS-1$

	public ESP32S2ToolChain(IToolChainProvider provider, Path pathToToolChain)
	{
		super(provider, pathToToolChain, ARCH, null);
		setProperty(ATTR_OS, OS);
		setProperty(ATTR_ARCH, ARCH);
	}

	@Override
	public String getBinaryParserId()
	{
		return CCorePlugin.PLUGIN_ID + ".ELF"; //$NON-NLS-1$
	}

}
