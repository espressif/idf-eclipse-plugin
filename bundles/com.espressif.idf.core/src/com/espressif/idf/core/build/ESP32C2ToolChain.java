package com.espressif.idf.core.build;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.IToolChainProvider;

public class ESP32C2ToolChain extends AbstractESPToolchain
{

	/**
	 * Toolchain target ID
	 */
	public static final String ID = "riscv32-esp-elf"; //$NON-NLS-1$

	/**
	 * Property: The OS the toolchain builds for.
	 */
	public static final String OS = "esp32c2"; //$NON-NLS-1$

	/**
	 * Property: The CPU architecture the toolchain supports.
	 */
	public static final String ARCH = "riscv32"; //$NON-NLS-1$

	/**
	 * @param provider
	 * @param pathToToolChain
	 */
	public ESP32C2ToolChain(IToolChainProvider provider, Path pathToToolChain)
	{
		super(provider, pathToToolChain, OS, ARCH);
	}

	@Override
	public String getId()
	{
		// TODO Auto-generated method stub
		return super.getId() + "-" + OS; //$NON-NLS-1$
	}

}
