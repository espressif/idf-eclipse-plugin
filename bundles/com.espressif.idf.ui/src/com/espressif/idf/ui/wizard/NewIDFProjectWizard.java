/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.ui.templates.IDFProjectGenerator;
import com.espressif.idf.ui.templates.ITemplateNode;
import com.espressif.idf.ui.templates.TemplateListSelectionPage;
import com.espressif.idf.ui.templates.TemplatesManager;

/**
 * Creates a wizard for creating a new IDF project resource in the workspace.
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
@SuppressWarnings("restriction")
public class NewIDFProjectWizard extends TemplateWizard
{

	private WizardNewProjectCreationPage mainPage;
	private TemplateListSelectionPage templatesPage;

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
		super.addPages();

		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") //$NON-NLS-1$
		{
			@Override
			public void createControl(Composite parent)
			{
				super.createControl(parent);
				Dialog.applyDialogFont(getControl());
			}
		};
		mainPage.setTitle(Messages.NewIDFProjectWizard_Project_Title);
		mainPage.setDescription(Messages.NewIDFProjectWizard_ProjectDesc);
		this.setWindowTitle(Messages.NewIDFProjectWizard_NewIDFProject);
		
		TemplatesManager templatesManager = new TemplatesManager();
		ITemplateNode templateRoot = templatesManager.getTemplates();

		boolean hasTemplates = templateRoot.getChildren().isEmpty();
		if (!hasTemplates)
		{
			templatesPage = new TemplateListSelectionPage(templateRoot,
					Messages.NewIDFProjectWizard_TemplatesHeader);
			ITemplateNode templateNode = templatesManager.getTemplateNode(IDFConstants.DEFAULT_TEMPLATE_ID);
			if (templateNode != null)
			{
				templatesPage.setInitialTemplateId(templateNode);
			}
		}

		this.addPage(mainPage);

		// Add templates page only if templates are available
		if (!hasTemplates)
		{
			this.addPage(templatesPage);
		}

	}

	@Override
	protected IGenerator getGenerator()
	{

		String manifest = IDFConstants.IDF_TEMPLATE_MANIFEST_PATH;
		File selectedTemplate = null;
		if (templatesPage != null && templatesPage.getSelection() != null)
		{
			selectedTemplate = templatesPage.getSelection().getFilePath();
			manifest = null;
		}

		IDFProjectGenerator generator = new IDFProjectGenerator(manifest, selectedTemplate, true);
		generator.setProjectName(mainPage.getProjectName());
		if (!mainPage.useDefaults())
		{
			generator.setLocationURI(mainPage.getLocationURI());

		}
		return generator;
	}
	
	

}
