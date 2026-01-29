package com.espressif.idf.terminal.connector.controls.themes;

import org.eclipse.jface.preference.IPreferenceStore;

public interface ITerminalTheme {
	String getId();

	String getLabel();

	void apply(IPreferenceStore store);
}
