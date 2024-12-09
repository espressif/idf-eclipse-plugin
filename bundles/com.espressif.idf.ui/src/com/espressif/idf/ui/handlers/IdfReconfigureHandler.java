package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.ConsoleManager;
import com.espressif.idf.core.util.IdfCommandExecutor;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.wizard.Messages;

@SuppressWarnings("restriction")
public class IdfReconfigureHandler extends AbstractHandler
{

	private static final String DEFAULT_TARGET = "esp32"; //$NON-NLS-1$

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IProject selectedProject = EclipseUtil.getSelectedProjectInExplorer();

		Job job = new Job(Messages.IdfReconfigureJobName)
		{

			protected IStatus run(IProgressMonitor monitor)
			{

				IdfCommandExecutor executor = new IdfCommandExecutor(getCurrentTarget(),
						ConsoleManager.getConsole("CDT Build Console")); //$NON-NLS-1$
				IStatus status = executor.executeReconfigure(selectedProject);
				try
				{
					IDEWorkbenchPlugin.getPluginWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
				}
				catch (CoreException e)
				{
					Logger.log(e);
				}
				return status;
			}
		};
		job.schedule();
		return null;
	}

	private String getCurrentTarget()
	{
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		ILaunchTarget target;
		try
		{
			target = launchBarManager.getActiveLaunchTarget();
			return target.getAttribute(LaunchBarTargetConstants.TARGET, StringUtil.EMPTY);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return DEFAULT_TARGET;
	}
}
