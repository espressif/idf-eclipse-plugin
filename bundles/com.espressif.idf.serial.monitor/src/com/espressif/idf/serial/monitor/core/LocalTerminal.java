package com.espressif.idf.serial.monitor.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.preferences.EspresssifPreferencesPage;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class LocalTerminal
{
	private PtyProcess pty;
	private List<String> arguments;
	private File workingDir;
	private Map<String, String> environment;
	
	public LocalTerminal(List<String> commandArgs, File projectWorkingDir, Map<String, String> environment)
	{
		this.arguments = commandArgs;
		this.workingDir = projectWorkingDir;
		this.environment = environment;
	}

	public Process connect() throws IOException
	{
		String[] args = arguments.toArray(new String[arguments.size()]);
		int numberOfRows =  Platform.getPreferencesService().getInt(UIPlugin.PLUGIN_ID, EspresssifPreferencesPage.NUMBER_OF_LINES, EspresssifPreferencesPage.DEFAULT_SERIAL_MONITOR_NUBMER_OF_LINES, null);
		int numberOfCols =  Platform.getPreferencesService().getInt(UIPlugin.PLUGIN_ID, EspresssifPreferencesPage.NUMBER_OF_CHARS_IN_A_LINE, EspresssifPreferencesPage.DEFAULT_SERIAL_MONITOR_NUMBER_OF_CHARS_IN_LINE, null);
		PtyProcessBuilder ptyProcessBuilder = new PtyProcessBuilder(args).setEnvironment(environment)
				.setDirectory(workingDir.getAbsolutePath()).setInitialColumns(numberOfCols).setInitialRows(numberOfRows)
				.setConsole(false).setCygwin(false).setLogFile(null);

		pty = ptyProcessBuilder.start();
		return pty;
	}

}
