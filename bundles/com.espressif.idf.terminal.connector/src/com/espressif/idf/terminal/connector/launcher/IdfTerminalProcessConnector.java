package com.espressif.idf.terminal.connector.launcher;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.terminal.connector.process.ProcessConnector;

import com.espressif.idf.core.IDFEnvironmentVariables;

public class IdfTerminalProcessConnector extends ProcessConnector {

	private static final String ENV_SCRIPT_KEY = "ESP_IDF_ACTIVATION_SCRIPT"; //$NON-NLS-1$

	@Override
	public void connect(ITerminalControl control) {
		super.connect(control);

		var process = getProcess();
		if (process == null) {
			return;
		}

		String scriptPath = new IDFEnvironmentVariables().getEnvValue(ENV_SCRIPT_KEY);
		if (scriptPath == null || scriptPath.isBlank()) {
			writeToTerminal(process.getOutputStream(), "Error: ESP-IDF activation script path is missing.\r\n"); //$NON-NLS-1$
			return;
		}

		try {
			OutputStream out = process.getOutputStream();
			String command;

			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				// Windows (PowerShell): Remove-Module: Fixes the color/white-screen issue.
				command = "Remove-Module PSReadLine -ErrorAction SilentlyContinue; . \"" + scriptPath + "\"\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// Linux / macOS (Bash/Zsh):
				command = ". \"" + scriptPath + "\"\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
			}

			out.write(command.getBytes(StandardCharsets.UTF_8));
			out.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeToTerminal(OutputStream out, String message) {
		try {
			out.write(message.getBytes(StandardCharsets.UTF_8));
			out.flush();
		} catch (IOException ignored) {
		}
	}
}