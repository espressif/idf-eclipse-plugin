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
public class ESPToolchain extends GCCToolChain
{

	public ESPToolchain(IToolChainProvider provider, Path pathToToolChain, ESPToolChainElement element)
	{
		super(provider, pathToToolChain, element.arch, null);

		setProperty(ATTR_OS, element.name);
		setProperty(ATTR_ARCH, element.arch);
		setProperty("ATTR_ID", element.fileName); //$NON-NLS-1$
		// TODO: Set a property here for the idf version
	}

	@Override
	public String getId()
	{
		// TODO: get the idf version property to create a name
		String os = getProperty(ATTR_OS);
		String arch = getProperty(ATTR_ARCH);
		String pathToToolChain = getProperty("ATTR_ID"); //$NON-NLS-1$

		StringBuilder idBuilder = new StringBuilder("gcc-"); //$NON-NLS-1$
		if (arch != null)
		{
			idBuilder.append(arch);
		}
		if (os != null)
		{
			idBuilder.append('-');
			idBuilder.append(os);
		}
		idBuilder.append('-');
		if (pathToToolChain != null)
		{
			idBuilder.append(pathToToolChain.replace('\\', '/'));
		}
		return idBuilder.toString();

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
