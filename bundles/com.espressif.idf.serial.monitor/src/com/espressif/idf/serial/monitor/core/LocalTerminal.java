package com.espressif.idf.serial.monitor.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.console.MessageConsoleStream;

import com.pty4j.PtyProcess;

public class LocalTerminal
{
	private Thread thread;
	private PtyProcess pty;
	private List<String> arguments;
	private File workingDir;
	private MessageConsoleStream stream;
	private Map<String, String> environment;

	public LocalTerminal(List<String> commandArgs, File projectWorkingDir, Map<String, String> environment,
			MessageConsoleStream stream)
	{
		this.arguments = commandArgs;
		this.workingDir = projectWorkingDir;
		this.environment = environment;
		this.stream = stream;
	}

	public void connect() throws IOException
	{
		if (pty != null && pty.isAlive())
		{
			return;
		}

		String[] args = arguments.toArray(new String[arguments.size()]);

		pty = PtyProcess.exec(args, environment, workingDir.getAbsolutePath());

		// OutputStream os = pty.getOutputStream();
		InputStream is = pty.getInputStream();

		// PrintStream printStream = new PrintStream(os, true);
		// printStream.print(b);

		Runnable run = new TerminalWatcher(is, stream);
		thread = new Thread(run);
		thread.start();
	}

	public void disconnect()
	{
		if (pty != null)
		{
			pty.destroy();
		}
		if (thread != null)
		{
			thread.interrupt();
		}
	}

}
