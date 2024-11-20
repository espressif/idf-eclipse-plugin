/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import java.io.File;

import org.eclipse.cdt.debug.internal.core.InternalDebugCoreMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.ui.NewLaunchConfigWizard;
import org.eclipse.launchbar.ui.NewLaunchConfigWizardDialog;
import org.eclipse.launchbar.ui.internal.dialogs.NewLaunchConfigEditPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.ClangFormatFileHandler;
import com.espressif.idf.core.util.ClangdConfigFileHandler;
import com.espressif.idf.core.util.ConsoleManager;
import com.espressif.idf.core.util.IdfCommandExecutor;
import com.espressif.idf.core.util.LaunchUtil;
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
	private static final String NEW_LAUNCH_CONFIG_EDIT_PAGE = "NewLaunchConfigEditPage"; //$NON-NLS-1$
	public static final String TARGET_SWITCH_JOB = "TARGET SWITCH JOB"; //$NON-NLS-1$
	private NewProjectCreationWizardPage projectCreationWizardPage;
	private IProject project;
	private MessageConsole console;

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
		projectCreationWizardPage = new NewProjectCreationWizardPage(templateRoot,
				Messages.NewIDFProjectWizard_TemplatesHeader);
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
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				selProvider.setSelection(new StructuredSelection(project));
				updateClangFiles(project);
			}
		}

		final String target = projectCreationWizardPage.getSelectedTarget();
		this.getShell().addDisposeListener(event -> {
			ILaunchBarManager launchBarManager = UIPlugin.getService(ILaunchBarManager.class);
			TargetSwitchJob targetSwtichJob = new TargetSwitchJob(target);
			targetSwtichJob.schedule();
			try
			{
				ILaunchDescriptor desc = launchBarManager.getActiveLaunchDescriptor();
				if (new LaunchUtil(DebugPlugin.getDefault().getLaunchManager()).findAppropriateLaunchConfig(desc,
						IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE) == null)
				{

					// this ensures that the configuration exists
					launchBarManager.getActiveLaunchConfiguration();

					createDefaultDebugConfig();
					launchBarManager.setActiveLaunchDescriptor(desc);
				}
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
			if (projectCreationWizardPage.isRunIdfReconfigureEnabled())
			{
				runIdfReconfigureCommandJob(target);

			}
		});
		return performFinish;
	}

	private void runIdfReconfigureCommandJob(final String target)
	{
		Job job = new Job(Messages.IdfReconfigureJobName)
		{

			protected IStatus run(IProgressMonitor monitor)
			{
				IdfCommandExecutor executor = new IdfCommandExecutor(target,
						ConsoleManager.getConsole("CDT Build Console")); //$NON-NLS-1$
				IStatus status = executor.executeReconfigure(project);
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
	}

	private void updateClangFiles(IProject project)
	{
		try
		{
			new ClangdConfigFileHandler().update(project);
			new ClangFormatFileHandler(project).update();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	private void createDefaultDebugConfig()
	{
		Shell activeShell = Display.getDefault().getActiveShell();

		NewLaunchConfigWizard wizard = new NewLaunchConfigWizard();
		WizardDialog dialog = new NewLaunchConfigWizardDialog(activeShell, wizard);
		dialog.create();

		NewLaunchConfigEditPage editPage = (NewLaunchConfigEditPage) wizard.getPage(NEW_LAUNCH_CONFIG_EDIT_PAGE);
		ILaunchConfigurationType debugLaunchConfigType = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE);
		editPage.setLaunchConfigType(debugLaunchConfigType);

		PageChangingEvent pageChangingEvent = new PageChangingEvent(wizard, wizard.getStartingPage(), editPage);
		editPage.handlePageChanging(pageChangingEvent);
		wizard.performFinish();

		try
		{
			String originalName = wizard.getWorkingCopy().getName();
			int configPartIndex = originalName.lastIndexOf("Configuration"); //$NON-NLS-1$
			String debugConfigName = configPartIndex != -1 ? originalName.substring(0, configPartIndex) + "Debug" //$NON-NLS-1$
					: originalName;
			wizard.getWorkingCopy().copy(debugConfigName).doSave();
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		wizard.dispose();
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

		IDFProjectGenerator generator = new IDFProjectGenerator(manifest, selectedTemplate, true,
				projectCreationWizardPage.getSelectedTarget());
		generator.setProjectName(projectCreationWizardPage.getProjectName());
		if (!projectCreationWizardPage.useDefaults())
		{
			generator.setLocationURI(projectCreationWizardPage.getLocationURI());
		}
		return generator;
	}

	private class TargetSwitchJob extends Job
	{
		private ILaunchBarManager launchBarManager;
		private String target;

		public TargetSwitchJob(String target)
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
			if (job != null)
			{
				try
				{
					job.join();
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
				String idfTarget = iLaunchTarget.getAttribute(LaunchBarTargetConstants.TARGET, null);
				if (idfTarget.contentEquals(target))
				{
					return iLaunchTarget;
				}
			}

			return null;
		}
	}
}
