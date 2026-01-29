package com.espressif.idf.terminal.connector.controls.themes;

import org.eclipse.terminal.model.TerminalColor;

public class EspressifLightTheme extends CustomTheme {

	public EspressifLightTheme() {
		super("LIGHT", Messages.EspressifLightTheme_Name); //$NON-NLS-1$
	}

	@Override
	protected void configure() {
		set(TerminalColor.BACKGROUND, 255, 255, 255);
		set(TerminalColor.FOREGROUND, 0, 0, 0);

		// Fix visibility for light background
		set(TerminalColor.WHITE, 80, 80, 80);
		set(TerminalColor.YELLOW, 204, 102, 0);
		set(TerminalColor.BRIGHT_YELLOW, 204, 102, 0);
		set(TerminalColor.BRIGHT_WHITE, 80, 80, 80);
	}
}
