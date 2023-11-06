/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.espressif.idf.core.IDFConstants;
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

}
