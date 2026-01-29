package com.espressif.idf.terminal.connector.controls.themes;

import org.eclipse.terminal.model.TerminalColor;

public class PowerShellTheme extends CustomTheme {

	public PowerShellTheme() {
		super("POWERSHELL", "PowerShell Optimized (Blue/Contrast)");
	}

	@Override
	protected void configure() {
		set(TerminalColor.BACKGROUND, 1, 36, 86);
		set(TerminalColor.FOREGROUND, 238, 237, 240);
		set(TerminalColor.CYAN, 0, 255, 255);
		set(TerminalColor.BLUE, 0, 200, 255);
	}
}
