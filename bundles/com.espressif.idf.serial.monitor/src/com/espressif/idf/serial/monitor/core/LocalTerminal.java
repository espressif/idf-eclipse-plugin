package com.espressif.idf.serial.monitor.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
	private String numberOfCols;
	private String numberOfRows;

	public LocalTerminal(List<String> commandArgs, File projectWorkingDir, Map<String, String> environment,
			String numberOfCols, String numberOfRows)
	{
		this.arguments = commandArgs;
		this.workingDir = projectWorkingDir;
		this.environment = environment;
		this.numberOfCols = numberOfCols;
		this.numberOfRows = numberOfRows;
	}

	public Process connect() throws IOException
	{
		String[] args = arguments.toArray(new String[arguments.size()]);
		PtyProcessBuilder ptyProcessBuilder = new PtyProcessBuilder(args).setEnvironment(environment)
				.setDirectory(workingDir.getAbsolutePath()).setInitialColumns(Integer.parseInt(numberOfCols))
				.setInitialRows(Integer.parseInt(numberOfRows)).setConsole(false).setCygwin(false).setLogFile(null);

		pty = ptyProcessBuilder.start();
		return pty;
	}

}
