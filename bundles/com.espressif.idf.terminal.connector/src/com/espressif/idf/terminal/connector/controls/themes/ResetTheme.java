package com.espressif.idf.terminal.connector.controls.themes;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.terminal.model.TerminalColor;

public class ResetTheme implements ITerminalTheme {
	@Override
	public String getId() {
		return "DEFAULT"; //$NON-NLS-1$
	}

	@Override
	public String getLabel() {
		return Messages.ResetTheme_Name;
	}

	@Override
	public void apply(IPreferenceStore store) {
		for (TerminalColor color : TerminalColor.values()) {
			store.setToDefault(color.name());
		}
	}
}
