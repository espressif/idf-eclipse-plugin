package com.espressif.idf.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

public class ProcessBuilderFactory
{

	private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

	/**
	 * @param commands
	 * @param workingDirectory
	 * @param environment
	 * @return
	 * @throws IOException
	 */
	public Process run(List<String> commands, IPath workingDirectory, Map<String, String> environment)
			throws IOException
	{
		ProcessBuilder processBuilder = new ProcessBuilder(commands);
		if (workingDirectory != null)
		{
			processBuilder.directory(workingDirectory.toFile());
		}
		if (environment != null && !environment.isEmpty())
		{
			processBuilder.environment().putAll(environment);
		}
		// let's merge the error stream with the standard output
		processBuilder.redirectErrorStream(true);
		return processBuilder.start();
	}

	public IStatus runInBackground(List<String> commands, IPath workingDirectory, Map<String, String> environment)
			throws IOException
	{
		Process process = run(commands, workingDirectory, environment);
		return processData(process, null);
	}

	/**
	 * @param process
	 * @param input
	 * @return
	 */
	private IStatus processData(Process process, String input)
	{
		return processData(process.getInputStream(), process.getErrorStream(), process.getOutputStream(), input,
				process, false, -1);
	}

	/**
	 * @param inputStream
	 * @param errorStream
	 * @param outputStream
	 * @param input
	 * @param process
	 * @param earlyWait
	 * @param timeOut
	 * @return
	 */
	private IStatus processData(InputStream inputStream, InputStream errorStream, OutputStream outputStream,
			String input, Process process, boolean earlyWait, long timeOut)
	{

		String lineSeparator = IDFUtil.getLineSeparatorValue();
		InputStreamThread readerGobbler = null;
		InputStreamThread errorGobbler = null;
		try
		{
			int exitValue = 0;
			if (earlyWait)
			{
				exitValue = process.waitFor();
			}
			// Read and write in threads to avoid from choking the process streams
			OutputStreamThread writerThread = null;
			if (!StringUtil.isEmpty(input))
			{
				writerThread = new OutputStreamThread(outputStream, input, UTF_8);
			}
			readerGobbler = new InputStreamThread(inputStream, lineSeparator, UTF_8);
			errorGobbler = new InputStreamThread(errorStream, lineSeparator, null);

			// Start the threads
			if (writerThread != null)
			{
				writerThread.start();
			}
			readerGobbler.start();
			errorGobbler.start();
			if (!earlyWait)
			{
				if (timeOut > 0)
				{
					boolean waitFor = process.waitFor(timeOut, TimeUnit.MILLISECONDS);
					if (!waitFor)
					{
						exitValue = -1;
						process.destroy();
					}
				}
				else
				{
					// This will wait till the process is done.
					exitValue = process.waitFor();
				}
			}
			if (writerThread != null)
			{
				writerThread.interrupt();
				writerThread.join();
			}
			readerGobbler.interrupt();
			errorGobbler.interrupt();
			readerGobbler.join();
			errorGobbler.join();

			String stdout = readerGobbler.getResult();
			String stderr = errorGobbler.getResult();

			return new Status(exitValue == 0 ? IStatus.OK : IStatus.ERROR, IDFCorePlugin.PLUGIN_ID, exitValue,
					createMessage(exitValue, stdout, stderr), null);

		}
		catch (InterruptedException e)
		{
			String stdout = ""; //$NON-NLS-1$
			String stderr = ""; //$NON-NLS-1$
			try
			{
				if (readerGobbler != null)
				{
					readerGobbler.interrupt();
				}
				if (errorGobbler != null)
				{
					errorGobbler.interrupt();
				}
				if (readerGobbler != null)
				{
					readerGobbler.join();
					stdout = readerGobbler.getResult();
				}
				if (errorGobbler != null)
				{
					errorGobbler.join();
					stderr = errorGobbler.getResult();
				}
			}
			catch (InterruptedException e1)
			{
				// ignore
			}
			return new Status(IStatus.ERROR, IDFCorePlugin.PLUGIN_ID, createMessage(1, stdout, stderr), e);
		}
	}

	/**
	 * @param exitCode
	 * @param stdOut
	 * @param stderr
	 * @return
	 */
	private String createMessage(int exitCode, String stdOut, String stderr)
	{
		if (exitCode != 0 && StringUtil.isEmpty(stdOut))
		{
			return stderr;
		}
		if (stdOut != null && stdOut.endsWith("\n")) //$NON-NLS-1$
		{
			return stdOut.substring(0, stdOut.length() - 1);
		}
		if (!StringUtil.isEmpty(stderr))
		{
			String[] lines = stderr.split("[\n\r]+"); //$NON-NLS-1$
			for (int i = lines.length - 1; i >= 0; i--)
			{
				String line = lines[i];
				if (line.startsWith("[ERROR] :")) //$NON-NLS-1$
				{
					stdOut = line.substring(9).trim() + '\n' + stdOut;
					break;
				}
			}
		}
		return stdOut;
	}

}