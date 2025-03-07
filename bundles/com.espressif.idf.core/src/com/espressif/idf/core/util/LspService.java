/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.config.Configuration;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.ui.PlatformUI;

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
		languageServerWrappers.forEach(w ->
		// ensures that the LS is initialized before proceeding.
		w.execute(ls -> ls.shutdown()).thenRun(w::restart));
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

	public void updateClangdPath()
	{
		if (configuration.metadata() instanceof ClangdMetadata metadata)
		{
			String qualifier = configuration.qualifier();
			InstanceScope.INSTANCE.getNode(qualifier).put(metadata.clangdPath().identifer(),
					metadata.clangdPath().defaultValue());
		}
	}

	public void updateCompileCommandsDir(String buildDir)
	{
		if (configuration.metadata() instanceof ClangdMetadata metadata)
		{
			String qualifier = configuration.qualifier();
			String identifier = metadata.additionalOptions().identifer();
			IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(qualifier);

			String existingOptions = preferences.get(identifier, StringUtil.EMPTY);
			String compileCommandsDirString = "--compile-commands-dir="; //$NON-NLS-1$
			String newCompuileCommandsDirString = compileCommandsDirString + buildDir;
			String updatedOptions = existingOptions.contains(compileCommandsDirString)
					? existingOptions.replaceAll(compileCommandsDirString + ".+", //$NON-NLS-1$
							Matcher.quoteReplacement(newCompuileCommandsDirString))
					: newCompuileCommandsDirString;
			preferences.put(identifier, updatedOptions);
		}
	}
}
