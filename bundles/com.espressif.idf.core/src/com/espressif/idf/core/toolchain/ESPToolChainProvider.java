/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.toolchain;

import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESPToolChainProvider implements IToolChainProvider
{

	/*
	 * ESP Toolchain provider registered in the extension point
	 */
	public static final String ID = "com.espressif.idf.core.esp.toolchainprovider"; //$NON-NLS-1$

	private ESPToolChainManager espToolChainManager;

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) throws CoreException
	{
		espToolChainManager = new ESPToolChainManager();
		espToolChainManager.initToolChain(manager, this);

	}

}