package com.espressif.idf.lsp.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.lsp.config.ConfigurationMetadataBase;
import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public class IDFEditorMetadataDefaults extends ConfigurationMetadataBase implements EditorMetadata
{

	@Override
	protected List<PreferenceMetadata<?>> definePreferences()
	{
		Set<String> filteredKeys = Set.of(Predefined.showTryLspBanner.identifer(),
				Predefined.preferLspEditor.identifer());
		var filteredDefaults = Predefined.defaults.stream().filter(pref -> filteredKeys.contains(pref.identifer()))
				.toList();

		var showTryLspBannerCustomDefault = wrapWithCustomDefaultValue(false, Predefined.showTryLspBanner);
		var preferLspEditorCustomDefault = wrapWithCustomDefaultValue(true, Predefined.preferLspEditor);

		List<PreferenceMetadata<?>> mergedPreferences = new ArrayList<>(filteredDefaults);
		mergedPreferences.add(showTryLspBannerCustomDefault);
		mergedPreferences.add(preferLspEditorCustomDefault);

		return mergedPreferences;
	}

	private <T> PreferenceMetadata<T> wrapWithCustomDefaultValue(T customDefaultValue, PreferenceMetadata<T> metadata)
	{
		return new PreferenceMetadata<>(metadata.valueClass(), metadata.identifer(), customDefaultValue,
				metadata.name(), metadata.description());
	}
}
