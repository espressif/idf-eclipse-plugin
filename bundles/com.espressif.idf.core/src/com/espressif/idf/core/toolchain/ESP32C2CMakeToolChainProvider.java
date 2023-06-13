package com.espressif.idf.core.toolchain;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;

public class ESP32C2CMakeToolChainProvider extends AbstractESPCMakeToolChainProvider
{

	public static final String TOOLCHAIN_NAME = "toolchain-esp32c2.cmake"; //$NON-NLS-1$

	@Override
	protected IToolChain getToolchain() throws CoreException
	{
		IToolChain toolChain = tcManager.getToolChain(ESPToolChainProvider.ID, ESP32C2ToolChain.ID);
		return toolChain;
	}

}
