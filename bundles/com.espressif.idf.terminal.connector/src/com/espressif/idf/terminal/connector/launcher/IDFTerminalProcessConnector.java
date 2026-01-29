/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.terminal.connector.launcher;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.Platform;
import org.eclipse.terminal.connector.ITerminalControl;
import org.eclipse.terminal.connector.process.ProcessConnector;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class IDFTerminalProcessConnector extends ProcessConnector {

	private static final String KEY_INSTALLED = "idfInstalled"; //$NON-NLS-1$
	private static final String KEY_ID = "id"; //$NON-NLS-1$
	private static final String KEY_SCRIPT = "activationScript"; //$NON-NLS-1$

	@Override
	public void connect(ITerminalControl control) {
		super.connect(control);

		var process = getProcess();
		if (process == null) {
			return;
		}

		getActivationScriptPath().ifPresentOrElse(
				scriptPath -> sendCommand(process.getOutputStream(), buildActivationCommand(scriptPath)),
				() -> sendCommand(process.getOutputStream(), "# Error: ESP-IDF activation script is missing.\r\n") //$NON-NLS-1$
		);
	}

	private String buildActivationCommand(String scriptPath) {
		return ". \"" + scriptPath + "\"\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void sendCommand(OutputStream out, String command) {
		try {
			out.write(command.getBytes(StandardCharsets.UTF_8));
			out.flush();
		} catch (IOException e) {
			Logger.log(e);
		}
	}

	private Optional<String> getActivationScriptPath() {
		var envPath = Objects.equals(Platform.getOS(), Platform.OS_WIN32) ? EimConstants.EIM_WIN_PATH
				: EimConstants.EIM_POSIX_PATH;

		if (envPath == null) {
			return Optional.empty();
		}

		var path = Path.of(envPath);
		if (!Files.exists(path)) {
			return Optional.empty();
		}

		try (var reader = Files.newBufferedReader(path)) {
			var root = JsonParser.parseReader(reader).getAsJsonObject();

			var selectedId = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.ESP_IDF_EIM_ID);
			if (selectedId == null) {
				return Optional.empty();
			}

			if (!root.has(KEY_INSTALLED) || !root.get(KEY_INSTALLED).isJsonArray()) {
				return Optional.empty();
			}
			var installed = root.get(KEY_INSTALLED).getAsJsonArray();

			return StreamSupport.stream(installed.spliterator(), false).map(JsonElement::getAsJsonObject)
					.filter(item -> item.has(KEY_ID) && !item.get(KEY_ID).isJsonNull())
					.filter(item -> selectedId.equals(item.get(KEY_ID).getAsString()))
					.filter(item -> item.has(KEY_SCRIPT) && !item.get(KEY_SCRIPT).isJsonNull())
					.map(item -> item.get(KEY_SCRIPT).getAsString()).findFirst();

		} catch (IOException | IllegalStateException | JsonParseException e) {
			Logger.log(e);
			return Optional.empty();
		}
	}
}
