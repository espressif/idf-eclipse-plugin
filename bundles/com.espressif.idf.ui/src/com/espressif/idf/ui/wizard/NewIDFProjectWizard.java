/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import org.eclipse.cdt.cmake.core.CMakeProjectGenerator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.framework.Bundle;

import com.espressif.idf.ui.UIPlugin;

/**
 * Creates a wizard for creating a new IDF project resource in the workspace.
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
@SuppressWarnings("restriction")
public class NewIDFProjectWizard extends TemplateWizard
{

	private WizardNewProjectCreationPage page;

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

		page = new WizardNewProjectCreationPage("basicNewProjectPage") //$NON-NLS-1$
		{
			@Override
			public void createControl(Composite parent)
			{
				super.createControl(parent);
				Dialog.applyDialogFont(getControl());
			}
		};
		page.setTitle(Messages.NewIDFProjectWizard_Project_Title);
		page.setDescription(Messages.NewIDFProjectWizard_ProjectDesc);
		this.addPage(page);

	}

	@Override
	protected IGenerator getGenerator() {
		CMakeProjectGenerator generator = new CMakeProjectGenerator("templates/esp-idf-template/manifest.xml") //$NON-NLS-1$
		{
			@Override
			public Bundle getSourceBundle() {
				return UIPlugin.getDefault().getBundle();
			}
		};
		generator.setProjectName(page.getProjectName());
		if (!page.useDefaults()) {
			generator.setLocationURI(page.getLocationURI());
			
		}
		return generator;
	}
	

}
