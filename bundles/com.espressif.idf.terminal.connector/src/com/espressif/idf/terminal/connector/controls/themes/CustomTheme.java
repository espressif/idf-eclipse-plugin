package com.espressif.idf.terminal.connector.controls.themes;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.terminal.model.TerminalColor;

/**
 * Base class for defining a terminal theme using a Map.
 */
public class CustomTheme implements ITerminalTheme {

	private final String id;
	private final String label;
	protected final Map<TerminalColor, String> colorMap = new EnumMap<>(TerminalColor.class);

	public CustomTheme(String id, String label) {
		this.id = id;
		this.label = label;
		loadDefaults();
		configure();
	}

	/**
	 * Subclasses should override this to set their specific colors.
	 */
	protected void configure() {
		// Default implementation does nothing
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * Helper to set a color.
	 */
	protected void set(TerminalColor color, int r, int g, int b) {
		colorMap.put(color, r + "," + g + "," + b); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void apply(IPreferenceStore store) {
		for (Map.Entry<TerminalColor, String> entry : colorMap.entrySet()) {
			store.setValue(entry.getKey().name(), entry.getValue());
		}
	}

	private void loadDefaults() {
		set(TerminalColor.BLACK, 0, 0, 0);
		set(TerminalColor.RED, 205, 0, 0);
		set(TerminalColor.GREEN, 0, 205, 0);
		set(TerminalColor.YELLOW, 205, 205, 0);
		set(TerminalColor.BLUE, 0, 0, 238);
		set(TerminalColor.MAGENTA, 205, 0, 205);
		set(TerminalColor.CYAN, 0, 205, 205);
		set(TerminalColor.WHITE, 229, 229, 229);

		set(TerminalColor.BRIGHT_BLACK, 0, 0, 0);
		set(TerminalColor.BRIGHT_RED, 255, 0, 0);
		set(TerminalColor.BRIGHT_GREEN, 0, 255, 0);
		set(TerminalColor.BRIGHT_YELLOW, 255, 255, 0);
		set(TerminalColor.BRIGHT_BLUE, 92, 92, 255);
		set(TerminalColor.BRIGHT_MAGENTA, 255, 0, 255);
		set(TerminalColor.BRIGHT_CYAN, 0, 255, 255);
		set(TerminalColor.BRIGHT_WHITE, 255, 255, 255);
	}
}
