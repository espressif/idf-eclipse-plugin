/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.IOException;
import java.util.stream.Stream;

import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.editor.Configuration;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;

@SuppressWarnings("restriction")
public class LspService
{
	private final Configuration configuration;
	private final Stream<LanguageServerWrapper> languageServerWrappers;

	public LspService()
	{
		this(PlatformUI.getWorkbench().getService(ClangdConfiguration.class), LspUtils.getLanguageServers());
	}

	public LspService(Configuration configuration, Stream<LanguageServerWrapper> languageServerWrappers)
	{
		this.configuration = configuration;
		this.languageServerWrappers = languageServerWrappers;
	}

	public void restartLspServers()
	{
		languageServerWrappers.forEach(w -> {
			try
			{
				w.restart();
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		});
	}

	public void updateAdditionalOptions(String additionalOptions)
	{
		if (configuration.metadata() instanceof ClangdMetadata metadata)
		{
			String qualifier = configuration.qualifier();
			InstanceScope.INSTANCE.getNode(qualifier).put(metadata.additionalOptions().identifer(), additionalOptions);
		}
	}

	public void updateLspQueryDrivers()
	{
		if (configuration.metadata() instanceof ClangdMetadata metadata)
		{
			String qualifier = configuration.qualifier();
			InstanceScope.INSTANCE.getNode(qualifier).put(metadata.queryDriver().identifer(),
					metadata.queryDriver().defaultValue());
		}
	}
}
