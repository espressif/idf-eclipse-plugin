/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD.
 * All rights reserved. Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.progress.IProgressService;

import com.espressif.idf.core.bug.BugReportGenerator;
import com.espressif.idf.core.bug.GithubIssueOpener;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.help.ProductInformationHandler;
import com.espressif.idf.ui.update.ListInstalledToolsHandler;
import com.espressif.idf.ui.update.Messages;

/**
 * Generates a bug report by invoking existing handlers that log details to the ESP-IDF console, waits deterministically
 * for their jobs to finish, and then captures the relevant console output slices.
 * 
 * @author Ali Azam Rana
 *
 */
public class GenerateBugReportCommandHandler extends AbstractHandler
{
	// ------------------------------------------------------------------------
	// Configuration
	// ------------------------------------------------------------------------
	private static final int JOB_WAIT_TIMEOUT_SECONDS = 120; // $NON-NLS-1$

	// ------------------------------------------------------------------------
	// Entry point
	// ------------------------------------------------------------------------
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		// 1) Resolve console and capture initial offsets (UI thread)
		MessageConsole console = uiGetConsole(Messages.IDFToolsHandler_ToolsManagerConsole);
		final int[] offsets = uiGetInitialOffsets(console);

		// 2) Prepare latches and register listener BEFORE scheduling jobs
		CountDownLatch installedDone = new CountDownLatch(1);
		CountDownLatch productDone = new CountDownLatch(1);
		JobChangeAdapter listener = createDoneListener(Messages.ListInstalledToolsHandler_InstalledToolsListJobName,
				installedDone, Messages.ProductInformationHandler_ProductInformationLogJobName, productDone);

		Job.getJobManager().addJobChangeListener(listener);
		try
		{
			// 3) Schedule both handlers (they enqueue their Jobs)
			new ListInstalledToolsHandler().execute(event);
			new ProductInformationHandler().execute(event);

			// 4) Wait in a background thread for both jobs and capture deltas on UI thread
			final String[] installedOut = new String[1];
			final String[] productOut = new String[1];

			runInProgressService(monitor -> {
				awaitAndCaptureSegment(installedDone, console, offsets, Segment.INSTALLED, installedOut);
				awaitAndCaptureSegment(productDone, console, offsets, Segment.PRODUCT, productOut);
			});

			// 5) Build the report
			BugReportGenerator generator = new BugReportGenerator(
					installedOut[0] == null ? StringUtil.EMPTY : installedOut[0],
					productOut[0] == null ? StringUtil.EMPTY : productOut[0]);
			String report = generator.generateBugReport();
			MessageConsoleStream msgConsole = console.newMessageStream();
			msgConsole.println(String.format(Messages.BugReportHandler_CompletedBugReportMsg, report)); //$NON-NLS-1$
			
			msgConsole.println();
			msgConsole.println("Opening browser to create a new issue..."); //$NON-NLS-1$
			GithubIssueOpener.openNewIssue();

			return null;
		}
		catch (Exception e)
		{
			throw new ExecutionException("Bug report generation failed", e); //$NON-NLS-1$
		} finally
		{
			Job.getJobManager().removeJobChangeListener(listener);
		}
	}

	private enum Segment
	{
		INSTALLED, PRODUCT
	}

	/**
	 * Holds two offsets inside the console document: - installedStart: where "List Installed Tools" output begins -
	 * productStart: where "Product Information" output begins We update productStart after capturing the first segment,
	 * so the second segment slice is precise.
	 */
	private static final class Offsets
	{
		int installedStart;
		int productStart;
	}

	// ------------------------------------------------------------------------
	// Waiting + capture
	// ------------------------------------------------------------------------
	private void awaitAndCaptureSegment(CountDownLatch latch, MessageConsole console, int[] flatOffsets, Segment which,
			String[] out) throws InterruptedException
	{
		// Wrap the simple int[] into a mutable struct for clarity
		Offsets offsets = new Offsets();
		offsets.installedStart = flatOffsets[0];
		offsets.productStart = flatOffsets[1];

		// Wait OFF the UI thread
		latch.await(JOB_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

		// Then capture ON the UI thread
		if (which == Segment.INSTALLED)
		{
			String captured = uiCaptureDelta(console, offsets.installedStart);
			out[0] = captured;
			// Move the second segment start right after the first capture
			uiUpdateProductStart(console, flatOffsets);
		}
		else // PRODUCT
		{
			out[0] = uiCaptureDelta(console, offsets.productStart);
		}
	}

	private MessageConsole uiGetConsole(String name)
	{
		final MessageConsole[] result = new MessageConsole[1];
		Display.getDefault().syncExec(() -> {
			result[0] = findConsole(name);
		});
		return result[0];
	}

	private int[] uiGetInitialOffsets(MessageConsole console)
	{
		final int[] offsets = new int[2];
		Display.getDefault().syncExec(() -> {
			IDocument doc = console.getDocument();
			int len = doc.getLength();
			offsets[0] = len; // installedStart
			offsets[1] = len; // productStart (initially same)
		});
		return offsets;
	}

	/**
	 * Capture console text from 'start' to current end. Must be called on UI thread.
	 */
	private String uiCaptureDelta(MessageConsole console, int start)
	{
		final String[] slice = new String[1];
		Display.getDefault().syncExec(() -> {
			try
			{
				IDocument doc = console.getDocument();
				int len = Math.max(0, doc.getLength() - start);
				slice[0] = (len > 0) ? doc.get(start, len) : ""; //$NON-NLS-1$
			}
			catch (Exception e)
			{
				slice[0] = ""; //$NON-NLS-1$
			}
		});
		return slice[0];
	}

	/**
	 * After capturing the first segment, update productStart to the new end.
	 */
	private void uiUpdateProductStart(MessageConsole console, int[] flatOffsets)
	{
		Display.getDefault().syncExec(() -> {
			IDocument doc = console.getDocument();
			flatOffsets[1] = doc.getLength(); // productStart becomes current end
		});
	}

	// ------------------------------------------------------------------------
	// Job listener & progress service
	// ------------------------------------------------------------------------
	private JobChangeAdapter createDoneListener(String jobNameA, CountDownLatch latchA, String jobNameB,
			CountDownLatch latchB)
	{
		return new JobChangeAdapter()
		{
			@Override
			public void done(IJobChangeEvent e)
			{
				Job j = e.getJob();
				if (j == null)
					return;

				String name = j.getName();
				if (jobNameA.equals(name))
				{
					latchA.countDown();
				}
				else if (jobNameB.equals(name))
				{
					latchB.countDown();
				}
			}
		};
	}

	private void runInProgressService(IRunnableWithProgress runnable) throws Exception
	{
		IProgressService ps = PlatformUI.getWorkbench().getProgressService();
		ps.run(/* fork */ true, /* cancelable */ false, runnable);
	}

	// ------------------------------------------------------------------------
	// Console lookup
	// ------------------------------------------------------------------------
	private MessageConsole findConsole(String name)
	{
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (IConsole ic : existing)
		{
			if (name.equals(ic.getName()))
			{
				return (MessageConsole) ic;
			}
		}
		// No console found, create a new one (no icon)
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
}
