/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.templates.IDFProjectGenerator;

/**
 * Wizard to import an existing IDF project
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ImportIDFProjectWizard extends TemplateWizard implements IImportWizard
{

	private ImportIDFProjectWizardPage page;
	private String projectName;
	private String locationStr;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		setWindowTitle(Messages.ImportIDFProjectWizard_0);
	}

	@Override
	public void addPages()
	{
		page = new ImportIDFProjectWizardPage();
		addPage(page);
	}

	@Override
	protected IGenerator getGenerator()
	{
		projectName = page.getProjectName();
		locationStr = page.getLocation();
		boolean canCopyIntoWorkspace = page.canCopyIntoWorkspace();

		IDFProjectGenerator generator = new IDFProjectGenerator(null, new File(locationStr), canCopyIntoWorkspace);
		generator.setProjectName(projectName);
		return generator;
	}

	@Override
	public IWorkbench getWorkbench()
	{
		return PlatformUI.getWorkbench();
	}

}
