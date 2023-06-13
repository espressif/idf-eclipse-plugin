/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain;

import java.nio.file.Path;
import java.util.List;

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

	// API after changes in CDT 10.5.0
	public List<String> getBinaryParserIds()
	{
		return List.<String>of(CCorePlugin.PLUGIN_ID + ".ELF"); //$NON-NLS-1$
	}

	// API before CDT 10.5.0
	public String getBinaryParserId()
	{
		return CCorePlugin.PLUGIN_ID + ".ELF"; //$NON-NLS-1$
	}
}
