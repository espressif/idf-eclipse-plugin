package com.espressif.idf.debug.gdbjtag.openocd.ui.gcov;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;

import com.espressif.idf.debug.gdbjtag.openocd.dsf.Launch;

public class GcovDumpHandler extends AbstractHandler
{
	private IExecutionDMContext executionDMContext;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		ILaunch launchActive = null;
		for (ILaunch launch : launches)
		{
			if (!launch.isTerminated())
			{
				launchActive = launch;
				break;
			}
		}

		DsfServicesTracker dsfServicesTracker = ((Launch) launchActive).getDsfServicesTracker();
		ICommandControlService commandControlService = dsfServicesTracker.getService(ICommandControlService.class);
		DsfExecutor dsfExecutor = ((Launch) launchActive).getDsfExecutor();
		IRunControl runControl = dsfServicesTracker.getService(IRunControl.class);
		ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(commandControlService.getContext(),
				ICommandControlDMContext.class);

		IProcesses processControl = dsfServicesTracker.getService(IProcesses.class);
		processControl.getProcessesBeingDebugged(controlDmc, new DataRequestMonitor<IDMContext[]>(dsfExecutor, null)
		{
			@Override
			protected void handleSuccess()
			{
				executionDMContext = (IExecutionDMContext) (getData()[0]);
			}
		});

		runControl.suspend(executionDMContext, new RequestMonitor(dsfExecutor, null)
		{
			@Override
			protected void handleSuccess()
			{
				commandControlService.queueCommand(
						new CLICommand<>(commandControlService.getContext(), "mon esp gcov dump"),
						new ImmediateDataRequestMonitor<>()
						{
							@Override
							protected void handleSuccess()
							{
								runControl.resume(executionDMContext, new RequestMonitor(dsfExecutor, null));
							}

							@Override
							protected void handleError()
							{
								runControl.resume(executionDMContext, new RequestMonitor(dsfExecutor, null));
							}
						});
			}
		});
		return null;
	}
}
