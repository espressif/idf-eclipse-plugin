package com.espressif.idf.debug.gdbjtag.openocd.ui;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.ui.NewLaunchConfigWizard;
import org.eclipse.launchbar.ui.NewLaunchConfigWizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.debug.gdbjtag.openocd.dsf.LaunchConfigurationDelegate;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.tracing.AppLvlTracingDialog;

@SuppressWarnings("restriction")
public class AppLvlTracingHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		IResource projectOut = EclipseHandler.getSelectedProject(IPageLayout.ID_PROJECT_EXPLORER);

		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		Job job = new Job("AppLvlTracing")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				IResource project = projectOut;
				if (projectOut == null)
				{
					project = EclipseHandler.getSelectedResource((IEvaluationContext) event.getApplicationContext());
				}

				try
				{
					for (ILaunchConfiguration config : launchManager.getLaunchConfigurations())
					{
						IResource[] mappedResource = config.getMappedResources();
						if (mappedResource != null && mappedResource[0].getProject() == project)
						{
							LaunchConfiguration cg = (LaunchConfiguration) config;
							if (cg.getPreferredLaunchDelegate(
									ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN) instanceof LaunchConfigurationDelegate)
							{
								LaunchConfigurationDelegate debugDelegate = (LaunchConfigurationDelegate) cg
										.getPreferredLaunchDelegate(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
								debugDelegate.ignoreGdbClient();
								cg.launch(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN, null, false);
								debugDelegate.doNotIngoreGdbClient();
								Display.getDefault().asyncExec(new Runnable()
								{
									public void run()
									{
										IResource project = projectOut;
										if (projectOut == null)
										{
											project = EclipseHandler.getSelectedResource((IEvaluationContext) event.getApplicationContext());
										}
										AppLvlTracingDialog dialog = new AppLvlTracingDialog(activeShell);
										dialog.setProjectPath(project);
										dialog.open();
									}
								});

								return Status.OK_STATUS;
							}

						}
					}
					showMessage(Messages.DebugConfigurationNotFoundMsg);
				}

				catch (CoreException e)
				{
					Display.getDefault().asyncExec(new Runnable()
					{

						@Override
						public void run()
						{
							MessageDialog.openError(activeShell, Messages.OpenOcdFailedMsg, e.getMessage());

						}
					});
					Logger.log(e);
				}
				return Status.OK_STATUS;
			}
		};

		job.schedule();

		return null;
	}

	private void showMessage(final String message)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				Shell activeShell = Display.getDefault().getActiveShell();
				boolean isYes = MessageDialog.openQuestion(activeShell, Messages.MissingDebugConfigurationTitle,
						message);
				if (isYes)
				{

					NewLaunchConfigWizard wizard = new NewLaunchConfigWizard();
					WizardDialog dialog = new NewLaunchConfigWizardDialog(activeShell, wizard);
					dialog.open();
					try
					{
						wizard.getWorkingCopy().doSave();
					}
					catch (CoreException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}
}
