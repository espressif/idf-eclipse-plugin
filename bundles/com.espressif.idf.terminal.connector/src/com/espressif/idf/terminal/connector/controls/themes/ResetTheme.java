package com.espressif.idf.terminal.connector.controls.themes;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.terminal.model.TerminalColor;

public class ResetTheme implements ITerminalTheme {
	@Override
	public String getId() {
		return "DEFAULT";
	}

	@Override
	public String getLabel() {
		return "Eclipse Standard (Restore Defaults)";
	}

	@Override
	public void apply(IPreferenceStore store) {
		for (TerminalColor color : TerminalColor.values()) {
			store.setToDefault(color.name());
		}
	}
}
