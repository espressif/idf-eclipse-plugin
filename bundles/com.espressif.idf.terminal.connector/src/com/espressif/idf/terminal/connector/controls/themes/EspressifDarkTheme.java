package com.espressif.idf.terminal.connector.controls.themes;

import org.eclipse.terminal.model.TerminalColor;

public class EspressifDarkTheme extends CustomTheme {

	public EspressifDarkTheme() {
		super("DARK", Messages.EspressifDarkTheme_Name); //$NON-NLS-1$
	}

	@Override
	protected void configure() {
		set(TerminalColor.BACKGROUND, 33, 33, 33);
		set(TerminalColor.FOREGROUND, 230, 230, 230);

		// ANSI Overrides
		set(TerminalColor.RED, 255, 80, 80);
		set(TerminalColor.GREEN, 80, 255, 80);
		set(TerminalColor.YELLOW, 255, 255, 128);
		set(TerminalColor.BRIGHT_YELLOW, 255, 255, 128);
	}
}
