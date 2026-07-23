package com.espressif.idf.debug.gdbjtag.openocd.dsf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import com.espressif.idf.core.logging.Logger;

/**
 * Stops CDT GDB CLI console read jobs that otherwise busy-loop on
 * {@code LargePipedInputStream} after a stuck debug termination.
 * <p>
 * Routing through {@link CDebugUIPlugin#getDebuggerConsoleManager()} handles both
 * the basic ({@code GdbBasicCliConsole}) and full ({@code GdbFullCliConsole}) GDB
 * consoles, since both implement {@link IDebuggerConsole}.
 */
public final class GdbConsoleCleanup
{
	private static final String GDB_CLI_JOB_MARKER = "GDB CLI"; //$NON-NLS-1$
	private GdbConsoleCleanup()
	{
	}

	public static void stopConsolesForLaunch(ILaunch launch)
	{
		if (launch == null)
		{
			return;
		}

		try
		{
			List<IConsole> toRemove = new ArrayList<>();
			for (IDebuggerConsole console : CDebugUIPlugin.getDebuggerConsoleManager().getConsoles())
			{
				if (!launch.equals(console.getLaunch()))
				{
					continue;
				}

				try
				{
					console.stop();
				}
				catch (Exception e)
				{
					Logger.log(e);
				}

				try
				{
					CDebugUIPlugin.getDebuggerConsoleManager().removeConsole(console);
				}
				catch (Exception e)
				{
					Logger.log(e);
				}

				toRemove.add(console);
			}

			if (!toRemove.isEmpty())
			{
				ConsolePlugin.getDefault().getConsoleManager()
						.removeConsoles(toRemove.toArray(new IConsole[0]));
			}
		}
		catch (Exception e)
		{
			Logger.log(e);
		}

		cancelGdbCliReadJobs();
	}

	private static void cancelGdbCliReadJobs()
	{
		for (Job job : Job.getJobManager().find(null))
		{
			if (job == null)
			{
				continue;
			}
			String name = job.getName();
			if (name != null && name.contains(GDB_CLI_JOB_MARKER))
			{
				job.cancel();
			}
		}
	}
}
