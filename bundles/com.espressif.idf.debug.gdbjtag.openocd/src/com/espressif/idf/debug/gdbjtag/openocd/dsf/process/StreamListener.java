/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.dsf.process;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.espressif.idf.core.build.ReHintPair;
import com.espressif.idf.core.logging.LogFileWriterManager;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.HintsUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.debug.gdbjtag.openocd.dsf.console.IdfProcessConsole;
import com.espressif.idf.debug.gdbjtag.openocd.dsf.console.IdfProcessConsoleFactory;
import com.espressif.idf.debug.gdbjtag.openocd.ui.Messages;

/**
 * This class listens to a specified stream monitor to get notified on output from the process connected to console. Its
 * separated so that we can apply filters and pass the text onto the respected stream. It is designed to handle both
 * error and standard stream. This class is built using {@link ProcessConsole} StreamListner. The original class is
 * private there.
 * 
 * @author Ali Azam Rana
 */
public class StreamListener implements IStreamListener
{
	private static final String OPENOCD_FAQ_LINK = "https://github.com/espressif/openocd-esp32/wiki/Troubleshooting-FAQ"; //$NON-NLS-1$

	private IOConsoleOutputStream fConsoleErrorOutputStream;
	private IOConsoleOutputStream fConsoleOutputStream;

	private IStreamMonitor fErrorStreamMonitor;
	private IStreamMonitor fOutputStreamMonitor;

	private IdfProcessConsole idfProcessConsole;
	private boolean fStreamClosed = false;
	private List<ReHintPair> reHintsList;

	private final PrintWriter fileWriter;
	private final String resolvedOutputFilePath;

	public StreamListener(IProcess iProcess, IStreamMonitor errorStreamMonitor, IStreamMonitor outputStreamMonitor,
			Charset charset, String outputFile, boolean append)
	{
		this.fErrorStreamMonitor = errorStreamMonitor;
		this.fOutputStreamMonitor = outputStreamMonitor;

		this.resolvedOutputFilePath = resolveOutputFilePath(outputFile);

		this.idfProcessConsole = IdfProcessConsoleFactory.showAndActivateConsole(charset);

		// Clear the console only at the beginning, when OpenOCD starts
		if (iProcess.getLabel().contains("openocd")) //$NON-NLS-1$
		{
			idfProcessConsole.clearConsole();
		}

		this.fileWriter = LogFileWriterManager.getWriter(resolvedOutputFilePath, append);
		this.reHintsList = HintsUtil.getReHintsList(new File(HintsUtil.getOpenocdHintsYmlPath()));

		this.fConsoleErrorOutputStream = idfProcessConsole.getErrorStream();
		this.fConsoleErrorOutputStream.setActivateOnWrite(true);
		this.fConsoleOutputStream = idfProcessConsole.getOutputStream();
		this.fConsoleOutputStream.setActivateOnWrite(true);

		flushAndDisableBuffer();
	}

	/**
	 * Process existing content in monitor and flush and disable buffering if it is a {@link IFlushableStreamMonitor}.
	 *
	 * @param monitor the monitor which might have buffered content
	 */
	private void flushAndDisableBuffer()
	{
		String contents;
		synchronized (fErrorStreamMonitor)
		{
			contents = fErrorStreamMonitor.getContents();
			if (fErrorStreamMonitor instanceof IFlushableStreamMonitor m)
			{
				m.flushContents();
				m.setBuffered(false);
			}
			streamAppended(contents, fErrorStreamMonitor);
		}

		synchronized (fOutputStreamMonitor)
		{
			contents = fOutputStreamMonitor.getContents();
			if (fOutputStreamMonitor instanceof IFlushableStreamMonitor m)
			{
				m.flushContents();
				m.setBuffered(false);
			}
			streamAppended(contents, fOutputStreamMonitor);
		}
	}

	@Override
	public void streamAppended(String text, IStreamMonitor monitor)
	{
		text.lines().forEach(line -> {
			fileWriter.println(line);

			try
			{
				if (line.startsWith("Error:") && fConsoleErrorOutputStream != null) //$NON-NLS-1$
				{
					fConsoleErrorOutputStream.write((line + System.lineSeparator()).getBytes());
					fConsoleErrorOutputStream.flush();

					boolean[] hintMatched = { false };
					reHintsList.stream()
							.filter(reHintEntry -> reHintEntry.getRe()
									.map(pattern -> pattern.matcher(line).find() || line.contains(pattern.toString()))
									.orElse(false))
							.forEach(matchedReHintEntry -> {
								try
								{
									hintMatched[0] = true;
									String cyanHint = "\u001B[36mHint: " + matchedReHintEntry.getHint() + "\u001B[0m"; //$NON-NLS-1$ //$NON-NLS-2$

									fConsoleOutputStream.write(cyanHint + System.lineSeparator()
											+ matchedReHintEntry.getRef().orElse(StringUtil.EMPTY)
											+ System.lineSeparator());
									fConsoleOutputStream.flush();
								}
								catch (IOException e)
								{
									Logger.log(e);
								}
							});
					if (!hintMatched[0] && fConsoleOutputStream != null)
					{
						fConsoleOutputStream.write((Messages.OpenOCDConsole_ErrorGuideMessage + System.lineSeparator()
								+ OPENOCD_FAQ_LINK + System.lineSeparator()).getBytes());
					}
				}
				else if (fConsoleOutputStream != null)
				{
					fConsoleOutputStream.write((line + System.lineSeparator()).getBytes());
					fConsoleOutputStream.flush();
				}
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		});
	}

	public void closeStreams()
	{
		synchronized (fErrorStreamMonitor)
		{
			fErrorStreamMonitor.removeListener(this);
		}
		synchronized (fOutputStreamMonitor)
		{
			fOutputStreamMonitor.removeListener(this);
		}
		fStreamClosed = true;
	}

	public void dispose()
	{
		if (!fStreamClosed)
		{
			closeStreams();
		}
		if (resolvedOutputFilePath != null)
		{
			LogFileWriterManager.closeWriter(resolvedOutputFilePath);
		}
		fErrorStreamMonitor = null;
		fOutputStreamMonitor = null;
	}

	/**
	 * Resolves the output file path, expands variables, handles directory case, and ensures file/parent directories
	 * exist.
	 */
	private static String resolveOutputFilePath(String outputFile)
	{
		if (outputFile == null || outputFile.isEmpty())
		{
			return null;
		}
		try
		{
			// Expand Eclipse variables (e.g., ${workspace_loc:...})
			String expanded = VariablesPlugin.getDefault().getStringVariableManager()
					.performStringSubstitution(outputFile);
			File file = new File(expanded);
			if (file.isDirectory() || (!file.exists() && expanded.endsWith(File.separator)))
			{
				// If it's a directory or ends with a separator, append openocd.log
				file = new File(file, "openocd.log"); //$NON-NLS-1$
			}
			File parent = file.getParentFile();
			if (parent != null && !parent.exists())
			{
				parent.mkdirs();
			}
			if (!file.exists())
			{
				file.createNewFile();
			}
			return file.getAbsolutePath();
		}
		catch (Exception e)
		{
			Logger.log(e);
			return null;
		}
	}
}
