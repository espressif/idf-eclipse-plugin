package com.espressif.idf.core.actions;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.LaunchTargetHelper;
import com.espressif.idf.core.util.StringUtil;

public class ApplyTargetJob extends Job
{

	private IWizard wizard;
	private ILaunchBarManager launchBarManager;
	private ILaunchTargetManager targetManager;
	private String targetNameAttr;

	public ApplyTargetJob(ILaunchBarManager launchBarManager, ILaunchTargetManager targetManager, String targetNameAttr,
			IWizard wizard)
	{
		super(Messages.SettingTargetJob);
		this.launchBarManager = launchBarManager;
		this.targetManager = targetManager;
		this.wizard = wizard;
		this.targetNameAttr = targetNameAttr;
	}

	protected IStatus run(IProgressMonitor monitor)
	{
		try
		{
			if (launchBarManager.getActiveLaunchConfiguration() == null)
			{
				return Status.CANCEL_STATUS;
			}
			String targetName = launchBarManager.getActiveLaunchConfiguration().getAttribute(targetNameAttr,
					StringUtil.EMPTY);
			if (!targetName.isEmpty())
			{
				Optional<ILaunchTarget> optSuitableTarget = LaunchTargetHelper
						.findSuitableTargetForSelectedItem(targetManager, launchBarManager, targetName);

				if (optSuitableTarget.isPresent())
				{
					launchBarManager.setActiveLaunchTarget(optSuitableTarget.get());
				}
				else
				{
					showNoTargetMessage(targetName);
				}
			}
			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			Logger.log(e);
			return Status.CANCEL_STATUS;
		}
	}

	protected void showNoTargetMessage(String selectedTarget)
	{

		Display.getDefault().asyncExec(() -> {
			boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					Messages.IDFLaunchTargetNotFoundIDFLaunchTargetNotFoundTitle,
					Messages.IDFLaunchTargetNotFoundMsg1 + selectedTarget + Messages.IDFLaunchTargetNotFoundMsg2
							+ Messages.IDFLaunchTargetNotFoundMsg3);
			if (isYes)
			{
				WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						wizard);
				dialog.open();
			}
		});
	}
};
