package com.espressif.idf.lsp.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.config.ConfigurationMetadataBase;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.osgi.service.component.annotations.Component;

import com.espressif.idf.core.ILSPConstants;
import com.espressif.idf.core.util.IDFUtil;

@Component(property = { "service.ranking:Integer=0" })
public class IDFClangdMetadataDefaults extends ConfigurationMetadataBase implements ClangdMetadata
{

	@Override
	protected List<PreferenceMetadata<?>> definePreferences()
	{
		Set<String> filteredKeys = Set.of(Predefined.clangdPath.identifer(), Predefined.queryDriver.identifer(),
				Predefined.setCompilationDatabase.identifer());

		var filteredDefaults = Predefined.defaults.stream().filter(pref -> filteredKeys.contains(pref.identifer()))
				.toList();

		String clangdPath = Optional.ofNullable(IDFUtil.findCommandFromBuildEnvPath(ILSPConstants.CLANGD_EXECUTABLE))
				.orElse(ClangdMetadata.Predefined.clangdPath.defaultValue());

		var clangdMetadataWithDefault = wrapWithCustomDefaultValue(clangdPath, ClangdMetadata.Predefined.clangdPath);

		// Allow clangd to use the driver specified in compile_commands.json
		String defaultIdfQueryDriver = "**"; //$NON-NLS-1$

		var queryDriverMetadataWithDefault = wrapWithCustomDefaultValue(defaultIdfQueryDriver,
				ClangdMetadata.Predefined.queryDriver);
		var setCompilationDatabaseDefault = wrapWithCustomDefaultValue(false, Predefined.setCompilationDatabase);

		List<PreferenceMetadata<?>> mergedPreferences = new ArrayList<>(filteredDefaults);
		mergedPreferences.add(clangdMetadataWithDefault);
		mergedPreferences.add(queryDriverMetadataWithDefault);
		mergedPreferences.add(setCompilationDatabaseDefault);

		return mergedPreferences;
	}

	private <T> PreferenceMetadata<T> wrapWithCustomDefaultValue(T customDefaultValue, PreferenceMetadata<T> metadata)
	{
		return new PreferenceMetadata<>(metadata.valueClass(), metadata.identifer(), customDefaultValue,
				metadata.name(), metadata.description());
	}
}
