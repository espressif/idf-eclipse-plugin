package com.espressif.idf.debug.gdbjtag.openocd.common;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.eclipse.embedcdt.debug.gdbjtag.core.DebugUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.ui.NewLaunchConfigWizard;
import org.eclipse.launchbar.ui.NewLaunchConfigWizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.debug.gdbjtag.openocd.dsf.LaunchConfigurationDelegate;
import com.espressif.idf.debug.gdbjtag.openocd.ui.Messages;
import com.espressif.idf.ui.tracing.AppLvlTracingDialog;

public class LaunchConfigHandler
{

	private IResource project;
	private Shell activeShell;
	
	public LaunchConfigHandler(IResource project, Shell activeShell)
	{
		this.project = project;
		this.activeShell = activeShell;
	}
	
	public void launchOpenOcd()
	{
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
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
						launchOpenocdFromLaunchConfiguration(cg);
						openGcovIstantDumpDialog(activeShell);
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
	}

	private void openGcovIstantDumpDialog(Shell activeShell)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				AppLvlTracingDialog dialog = new AppLvlTracingDialog(activeShell);
				dialog.setProjectPath(project);
				dialog.open();
			}
		});
	}

	private void launchOpenocdFromLaunchConfiguration(LaunchConfiguration config) throws CoreException
	{
		if (!DebugUtils.isLaunchConfigurationStarted(config))
		{
			LaunchConfigurationDelegate debugDelegate = (LaunchConfigurationDelegate) config
					.getPreferredLaunchDelegate(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			debugDelegate.ignoreGdbClient();
			config.launch(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN, null, false);
			debugDelegate.doNotIngoreGdbClient();
		}
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
						Logger.log(e);
					}
				}
			}
		});
	}
}
