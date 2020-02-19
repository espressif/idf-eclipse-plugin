/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.cdt.cmake.core.CMakeToolChainEvent;
import org.eclipse.cdt.cmake.core.ICMakeToolChainListener;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.ICMakeToolChainProvider;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.runtime.CoreException;

import com.espressif.idf.core.IDFCorePlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESP32S2CMakeToolChainProvider implements ICMakeToolChainProvider, ICMakeToolChainListener
{

	public static final String TOOLCHAIN_ESP32_CMAKE = "toolchain-esp32s2.cmake"; //$NON-NLS-1$
	private IToolChainManager tcManager = CCorePlugin.getService(IToolChainManager.class);

	@Override
	public void init(ICMakeToolChainManager manager)
	{
		manager.addListener(this);

		ESPToolChainManager espToolChainManager = new ESPToolChainManager();
		espToolChainManager.initCMakeToolChain(tcManager, manager);
	}

	@Override
	public void handleCMakeToolChainEvent(CMakeToolChainEvent event)
	{
		switch (event.getType())
		{
		case CMakeToolChainEvent.ADDED:
			try
			{
				// This will load up the toolchain
				IToolChain toolChain = tcManager.getToolChain(ESPToolChainProvider.ID, ESP32S2ToolChain.ID);
				assert toolChain != null;
			}
			catch (CoreException e)
			{
				IDFCorePlugin.getPlugin().getLog().log(e.getStatus());
			}
			break;
		}
	}

}