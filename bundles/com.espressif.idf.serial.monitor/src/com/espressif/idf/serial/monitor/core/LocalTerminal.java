package com.espressif.idf.serial.monitor.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.pty4j.PtyProcess;

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

		pty = PtyProcess.exec(args, environment, workingDir.getAbsolutePath());
		return pty;
	}

}
