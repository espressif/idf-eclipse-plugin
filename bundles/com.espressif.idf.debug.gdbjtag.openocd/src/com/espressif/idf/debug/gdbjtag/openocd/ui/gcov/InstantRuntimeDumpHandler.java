package com.espressif.idf.debug.gdbjtag.openocd.ui.gcov;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.debug.gdbjtag.openocd.common.LaunchConfigHandler;
import com.espressif.idf.ui.EclipseUtil;

public class InstantRuntimeDumpHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Shell activeShell = EclipseUtil.getShell();
		IResource project = GcovUtility.getProject(event);
		
		LaunchConfigHandler launchConfigHandler = new LaunchConfigHandler(project, activeShell);
		
		Job job = new Job("Instant Runtime GCOV Dump")
		{
			
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				launchConfigHandler.launchOpenOcd();
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		return null;
	}
}
