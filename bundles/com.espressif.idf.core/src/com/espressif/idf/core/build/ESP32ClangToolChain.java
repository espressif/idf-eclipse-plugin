package com.espressif.idf.core.build;

import java.nio.file.Path;
import java.util.List;

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

	// API after Changes in CDT 10.5.0
	public List<String> getBinaryParserIds()
	{
		return List.<String>of("org.eclipse.cdt.core.ELF"); //$NON-NLS-1$
	}

	// API before CDT 10.5.0
	public String getBinaryParserId()
	{
		return CCorePlugin.PLUGIN_ID + ".ELF"; //$NON-NLS-1$
	}
}
