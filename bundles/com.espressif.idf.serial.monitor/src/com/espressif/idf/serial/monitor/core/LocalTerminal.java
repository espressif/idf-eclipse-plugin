package com.espressif.idf.serial.monitor.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.espressif.idf.serial.monitor.SerialMonitorBundle;
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
		int numberOfRows = SerialMonitorBundle.getInstance().getPreferenceStore()
				.getInt(SerialMonitorBundle.SERIAL_MONITOR_NUMBER_OF_LINES);
		int numberOfCols = SerialMonitorBundle.getInstance().getPreferenceStore()
				.getInt(SerialMonitorBundle.SERIAL_MONITOR_NUMBER_OF_CHARS_IN_LINE);
		PtyProcessBuilder ptyProcessBuilder = new PtyProcessBuilder(args).setEnvironment(environment)
				.setDirectory(workingDir.getAbsolutePath()).setInitialColumns(numberOfCols).setInitialRows(numberOfRows)
				.setConsole(false).setCygwin(false).setLogFile(null);

		pty = ptyProcessBuilder.start();
		return pty;
	}

}
