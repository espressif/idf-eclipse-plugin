package com.espressif.idf.core.build;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;

public class ESP32H2CMakeToolChainProvider extends AbstractESPCMakeToolChainProvider
{

	public static final String TOOLCHAIN_NAME = "toolchain-esp32h2.cmake"; //$NON-NLS-1$

	@Override
	protected IToolChain getToolchain() throws CoreException
	{
		IToolChain toolChain = tcManager.getToolChain(ESPToolChainProvider.ID, ESP32H2ToolChain.ID);
		return toolChain;
	}
}
