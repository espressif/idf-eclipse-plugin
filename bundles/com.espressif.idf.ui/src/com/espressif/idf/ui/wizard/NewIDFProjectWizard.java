/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import java.io.File;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.build.IDFBuildConfigurationSetup;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.ClangFormatFileHandler;
import com.espressif.idf.core.util.ClangdConfigFileHandler;
import com.espressif.idf.core.util.ConsoleManager;
import com.espressif.idf.core.util.IdfCommandExecutor;
import com.espressif.idf.core.util.LaunchTargetHelper;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.handlers.NewProjectHandlerUtil;
import com.espressif.idf.ui.templates.IDFProjectGenerator;
import com.espressif.idf.ui.templates.ITemplateNode;
import com.espressif.idf.ui.templates.NewProjectCreationWizardPage;
import com.espressif.idf.ui.templates.TemplatesManager;
import com.espressif.idf.ui.tools.ManageEspIdfVersionsHandler;

/**
 * Creates a wizard for creating a new IDF project resource in the workspace.
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
@SuppressWarnings("restriction")
public class NewIDFProjectWizard extends TemplateWizard
{
	private NewProjectCreationWizardPage projectCreationWizardPage;
	private IProject project;

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
		var errorMsg = NewProjectHandlerUtil.getErrorMessage();
		if (!errorMsg.isEmpty())
		{
			addPage(new ToolsMissingWizardPage(errorMsg));
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
		if (getContainer().getCurrentPage() instanceof ToolsMissingWizardPage)
		{
			try
			{
				new ManageEspIdfVersionsHandler().execute(null);
			}
			catch (ExecutionException e)
			{
				Logger.log(e);
			}
			return true;
		}

		boolean performFinish = super.performFinish();
		if (performFinish)
		{
			project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectCreationWizardPage.getProjectName());
			IWorkbenchPage page = EclipseHandler.getActiveWorkbenchWindow().getActivePage();
			IViewPart viewPart = page.findView("org.eclipse.ui.navigator.ProjectExplorer"); //$NON-NLS-1$
			if (viewPart != null)
			{
				ISelectionProvider selProvider = viewPart.getSite().getSelectionProvider();
				selProvider.setSelection(new StructuredSelection(project));
				updateClangFiles(project);
			}
		}

		final String target = projectCreationWizardPage.getSelectedTarget();
		this.getShell().addDisposeListener(event -> {
			IDFBuildConfigurationSetup.schedule(project, activateLaunchTarget(target));
			if (projectCreationWizardPage.isRunIdfReconfigureEnabled())
			{
				runIdfReconfigureCommandJob(target);
			}
		});
		return performFinish;
	}

	/**
	 * Selects the chip chosen in the wizard in the Launch Bar. The Launch Bar keeps the target of the previously active
	 * project when a new descriptor has no remembered target of its own, so the choice has to be applied explicitly.
	 *
	 * @param idfTargetName ESP-IDF target selected in the wizard
	 * @return the matching launch target, or {@code null} when none is registered for that chip
	 */
	private ILaunchTarget activateLaunchTarget(String idfTargetName)
	{
		ILaunchTargetManager launchTargetManager = UIPlugin.getService(ILaunchTargetManager.class);
		ILaunchTarget launchTarget = LaunchTargetHelper.findLaunchTargetByName(launchTargetManager, idfTargetName);
		if (launchTarget == null)
		{
			return null;
		}

		try
		{
			UIPlugin.getService(ILaunchBarManager.class).setActiveLaunchTarget(launchTarget);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return launchTarget;
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
}
