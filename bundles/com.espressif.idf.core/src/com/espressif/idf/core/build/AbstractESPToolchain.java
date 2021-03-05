/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
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
public abstract class AbstractESPToolchain extends GCCToolChain
{

	public AbstractESPToolchain(IToolChainProvider provider, Path pathToToolChain, String OS, String ARCH)
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
