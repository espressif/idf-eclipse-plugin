/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import java.io.File;

import org.eclipse.cdt.debug.internal.core.InternalDebugCoreMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.handlers.NewProjectHandlerUtil;
import com.espressif.idf.ui.templates.IDFProjectGenerator;
import com.espressif.idf.ui.templates.ITemplateNode;
import com.espressif.idf.ui.templates.NewProjectCreationWizardPage;
import com.espressif.idf.ui.templates.TemplatesManager;

/**
 * Creates a wizard for creating a new IDF project resource in the workspace.
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
@SuppressWarnings("restriction")
public class NewIDFProjectWizard extends TemplateWizard
{
	public static final String TARGET_SWITCH_JOB = "TARGET SWITCH JOB"; //$NON-NLS-1$
	private NewProjectCreationWizardPage projectCreationWizardPage;

	public NewIDFProjectWizard()
	{
		IDialogSettings workbenchSettings = IDEWorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		if (section == null)
		{
			section = workbenchSettings.addNewSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}

	@Override
	public void addPages()
	{
		if (!NewProjectHandlerUtil.installToolsCheck())
		{
			return;
		}
		super.addPages();

		this.setWindowTitle(Messages.NewIDFProjectWizard_NewIDFProject);
	
		TemplatesManager templatesManager = new TemplatesManager();
		ITemplateNode templateRoot = templatesManager.getTemplates();
		projectCreationWizardPage = new NewProjectCreationWizardPage(templateRoot, Messages.NewIDFProjectWizard_TemplatesHeader);
		ITemplateNode templateNode = templatesManager.getTemplateNode(IDFConstants.DEFAULT_TEMPLATE_ID);
		if (templateNode != null)
		{
			projectCreationWizardPage.setInitialTemplateId(templateNode);
		}
		
		this.addPage(projectCreationWizardPage);
	}

	@Override
	public boolean performFinish()
	{
		boolean performFinish = super.performFinish();
		if (performFinish)
		{
			IWorkbenchPage page = EclipseHandler.getActiveWorkbenchWindow().getActivePage();
			IViewPart viewPart = page.findView("org.eclipse.ui.navigator.ProjectExplorer"); //$NON-NLS-1$
			if (viewPart != null)
			{
				ISelectionProvider selProvider = viewPart.getSite().getSelectionProvider();
				String projectName = projectCreationWizardPage.getProjectName();
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				selProvider.setSelection(new StructuredSelection(project));
			}
		}
		final String target = projectCreationWizardPage.getSelectedTarget();
		this.getShell().addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				TargetSwtichJob targetSwtichJob = new TargetSwtichJob(target);
				targetSwtichJob.schedule();
			}
		});
		return performFinish;
	}
	
	@Override
	protected IGenerator getGenerator()
	{

		String manifest = IDFConstants.IDF_TEMPLATE_MANIFEST_PATH;
		File selectedTemplate = null;
		if (projectCreationWizardPage != null && projectCreationWizardPage.getSelection() != null)
		{
			selectedTemplate = projectCreationWizardPage.getSelection().getFilePath();
			manifest = null;
		}

		IDFProjectGenerator generator = new IDFProjectGenerator(manifest, selectedTemplate, true, projectCreationWizardPage.getSelectedTarget());
		generator.setProjectName(projectCreationWizardPage.getProjectName());
		if (!projectCreationWizardPage.useDefaults())
		{
			generator.setLocationURI(projectCreationWizardPage.getLocationURI());
		}
		return generator;
	}

	
	private class TargetSwtichJob extends Job
	{
		private ILaunchBarManager launchBarManager;
		private String target;
		public TargetSwtichJob(String target)
		{
			super(TARGET_SWITCH_JOB);
			this.target = target;
			launchBarManager = UIPlugin.getService(ILaunchBarManager.class);
		}


		private Job findInternalJob()
		{
			for (Job job : Job.getJobManager().find(null))
			{
				if (job.getName().equals(InternalDebugCoreMessages.CoreBuildLaunchBarTracker_Job))
				{
					return job;
				}
			}
			
			return null;
		}
		

		@Override
		protected IStatus run(IProgressMonitor monitor)
		{
			Job job = findInternalJob();
			while (job != null && job.getState() == Job.RUNNING)
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e1)
				{
					Logger.log(e1);
				}
			}
			
			Display.getDefault().syncExec(() -> {
				ILaunchTarget launchTarget = findSuitableTargetForSelectedTargetString();
				try
				{
					launchBarManager.setActiveLaunchTarget(launchTarget);
				}
				catch (CoreException e)
				{
					Logger.log(e);
				}	
			});
			
			return Status.OK_STATUS;
			
		}
		
		private ILaunchTarget findSuitableTargetForSelectedTargetString()
		{
			ILaunchTargetManager launchTargetManager = UIPlugin.getService(ILaunchTargetManager.class);
			ILaunchTarget[] targets = launchTargetManager
					.getLaunchTargetsOfType(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE);

			for (ILaunchTarget iLaunchTarget : targets)
			{
				String idfTarget = iLaunchTarget.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET,
						null);			
				if (idfTarget.contentEquals(target))
				{
					return iLaunchTarget;
				}
			}
			
			return null;
		}
	}
}
