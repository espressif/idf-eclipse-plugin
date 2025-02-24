/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.util.List;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.config.Configuration;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;

@SuppressWarnings("restriction")
public class LspService
{
	private final Configuration configuration;
	private final List<LanguageServerWrapper> languageServerWrappers;

	public LspService()
	{
		this(PlatformUI.getWorkbench().getService(ClangdConfiguration.class),
				LanguageServiceAccessor.getStartedWrappers(null, true).stream()
						.filter(w -> "org.eclipse.cdt.lsp.server".equals(w.serverDefinition.id)).toList()); //$NON-NLS-1$
	}

	public LspService(Configuration configuration, List<LanguageServerWrapper> languageServerWrappers)
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
			catch (Exception e)
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
