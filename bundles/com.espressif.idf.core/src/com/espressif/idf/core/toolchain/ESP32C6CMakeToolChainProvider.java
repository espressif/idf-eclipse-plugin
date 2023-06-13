package com.espressif.idf.core.toolchain;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;

public class ESP32C6CMakeToolChainProvider extends AbstractESPCMakeToolChainProvider
{

	public static final String TOOLCHAIN_NAME = "toolchain-esp32c6.cmake"; //$NON-NLS-1$

	@Override
	protected IToolChain getToolchain() throws CoreException
	{
		IToolChain toolChain = tcManager.getToolChain(ESPToolChainProvider.ID, ESP32C6ToolChain.ID);
		return toolChain;
	}

}