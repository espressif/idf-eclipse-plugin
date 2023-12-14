package com.espressif.idf.ui.handlers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.LaunchBarListener;

public class RenameIdfProjectParticipant extends RenameParticipant
{

	protected boolean initialize(Object element)
	{
		return true;
	}

	public String getName()
	{
		return null;
	}

	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException
	{
		return null;
	}

	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
	{
		// workaround to save active launch target when renaming the project
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		ILaunchTarget activeLaunchTarget = launchBarManager.getActiveLaunchTarget();
		LaunchBarListener.setIgnoreTargetChange(true);
		Display.getDefault().syncExec(() ->
			Display.getDefault().getActiveShell().addDisposeListener(disposeEvent -> {
				try
				{
					LaunchBarListener.setIgnoreTargetChange(false);
					launchBarManager.setActiveLaunchTarget(activeLaunchTarget);
				}
				catch (CoreException e)
				{
					Logger.log(e);
				}
		}));
		return null;
	}

}
