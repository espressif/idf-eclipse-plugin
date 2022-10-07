package com.espressif.idf.core.build;

import java.nio.file.Path;

import org.eclipse.cdt.build.gcc.core.ClangToolChain;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainProvider;

public class ESP32ClangToolChain extends ClangToolChain
{

	public static final String ID = "xtensa"; //$NON-NLS-1$
	public static final String OS = "esp32"; //$NON-NLS-1$
	public static final String ARCH = "xtensa"; //$NON-NLS-1$

	public ESP32ClangToolChain(IToolChainProvider provider, Path pathToToolChain)
	{
		super(provider, pathToToolChain, ARCH, null);
		setProperty(ATTR_OS, OS);
		setProperty(ATTR_ARCH, ARCH);
		setProperty("ATTR_ID", ESP32ClangCmakeToolChainProvider.TOOLCHAIN_NAME); //$NON-NLS-1$
	}

	@Override
	public String getBinaryParserId()
	{
		return CCorePlugin.PLUGIN_ID + ".ELF"; //$NON-NLS-1$
	}
}
