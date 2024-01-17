package com.espressif.idf.core.actions.test;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

import com.espressif.idf.core.actions.ApplyTargetJob;

public class TestableApplyTargetJob extends ApplyTargetJob
{

	@SuppressWarnings("serial")
	public static class TestableApplyTargetJobException extends RuntimeException
	{
		public TestableApplyTargetJobException(String message)
		{
			super(message);
		}
	}

	public TestableApplyTargetJob(ILaunchBarManager launchBarManager, ILaunchTargetManager targetManager,
			String targetNameAttr, IWizard wizard)
	{
		super(launchBarManager, targetManager, targetNameAttr, wizard);
	}

	@Override
	public IStatus run(IProgressMonitor monitor)
	{
		return super.run(monitor);
	}

	@Override
	public void showNoTargetMessage(String selectedTarget)
	{
		throw new TestableApplyTargetJobException(selectedTarget);
	}

}
