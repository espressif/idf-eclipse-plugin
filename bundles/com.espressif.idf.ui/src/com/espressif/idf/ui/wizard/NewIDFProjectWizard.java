/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.handlers.NewProjectHandlerUtil;
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
	private static final String cmakeProjectNamePattern = "(project[(].+[)])"; //$NON-NLS-1$
	private static final String[] cmakeListCommands = {"get_filename_component(ProjectId ${CMAKE_CURRENT_LIST_DIR} NAME)", //$NON-NLS-1$
			"string(REPLACE \" \" \"_\" ProjectId ${ProjectId})", //$NON-NLS-1$
			"project(${ProjectId})"}; //$NON-NLS-1$
	
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
			templatesPage = new TemplateListSelectionPage(templateRoot, Messages.NewIDFProjectWizard_TemplatesHeader);
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
				String projectName = mainPage.getProjectName();
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				selProvider.setSelection(
						new StructuredSelection(project));
				renameProjectInCmakeList(project);
			}
		}
		return performFinish;
	}

	private void renameProjectInCmakeList(IProject project)
	{
		Path cMakeListLocation = new File(project.getLocation() + "/CMakeLists.txt").toPath(); //$NON-NLS-1$
		List<String> fileContent;
		try
		{
			fileContent = new ArrayList<>(Files.readAllLines(cMakeListLocation));
			for (int i = 0; i < fileContent.size(); i++)
			{

				Pattern p = Pattern.compile(cmakeProjectNamePattern); 
				Matcher m = p.matcher(fileContent.get(i));
				if (m.find())
				{
					fileContent.set(i, cmakeListCommands[0]);
					fileContent.add(cmakeListCommands[1]);
					fileContent.add(cmakeListCommands[2]);
					break;
				}
			}

			Files.write(cMakeListLocation, fileContent, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			Logger.log(e);
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
