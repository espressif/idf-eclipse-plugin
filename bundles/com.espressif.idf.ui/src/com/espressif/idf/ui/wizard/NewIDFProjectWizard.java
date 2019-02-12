/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.osgi.framework.Bundle;

import com.espressif.idf.core.util.FileUtil;
import com.espressif.idf.ui.UIPlugin;

/**
 * Creates a wizard for creating a new IDF project resource in the workspace.
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
@SuppressWarnings("restriction")
public class NewIDFProjectWizard extends BasicNewResourceWizard
{

	public static final String GCC_TOOLCHAIN = "cdt.managedbuild.toolchain.gnu.cross.base"; //$NON-NLS-1$
	private static final String IDF_TEMPLATE_PATH = "resources/esp-idf-default-template"; //$NON-NLS-1$
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
	public boolean performFinish()
	{

		final String projectName = page.getProjectName();
		final File templateLocation = getTemplateLocationPath();
		final IToolChain toolChain = getToolChain(GCC_TOOLCHAIN);
		final IPath targetLocation = page.getLocationPath();

		IRunnableWithProgress op = new WorkspaceModifyOperation()
		{
			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException, InterruptedException
			{
				monitor.beginTask(Messages.NewIDFProjectWizard_TaskName, 10);

				// Create Project
				try
				{
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IProject project = workspace.getRoot().getProject(projectName);

					IProjectDescription description = workspace.newProjectDescription(projectName);
					IPath defaultWorkspaceLocation = workspace.getRoot().getLocation().append(projectName);

					IPath targetLocationPath = targetLocation.append(projectName);
					if (!targetLocation.isEmpty() && !targetLocationPath.equals(defaultWorkspaceLocation))
					{
						description.setLocation(targetLocationPath);
					}

					CCorePlugin.getDefault().createCDTProject(description, project, monitor);

					// Copy the default IDF template to the workspace
					copyIDFTemplateToWorkspace(projectName, templateLocation, project);

					// TODO Shall we add IDF nature?

					// Set up build information
					ICProjectDescriptionManager pdMgr = CoreModel.getDefault().getProjectDescriptionManager();
					ICProjectDescription projDesc = pdMgr.createProjectDescription(project, false);
					ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
					ManagedProject mProj = new ManagedProject(projDesc);
					info.setManagedProject(mProj);
					monitor.worked(1);

					CfgHolder cfgHolder = new CfgHolder(toolChain, null);
					String s = toolChain == null ? "0" : ((ToolChain) toolChain).getId(); //$NON-NLS-1$
					Configuration config = new Configuration(mProj, (ToolChain) toolChain,
							ManagedBuildManager.calculateChildId(s, null), cfgHolder.getName());

					IBuilder builder = config.getEditableBuilder();
					builder.setManagedBuildOn(false);
					CConfigurationData data = config.getConfigurationData();
					projDesc.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
					monitor.worked(1);

					pdMgr.setProjectDescription(project, projDesc);
				} catch (Throwable e)
				{
					ManagedBuilderUIPlugin.log(e);
				}
				monitor.done();
			}
		};

		try
		{
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
			return false;
		} catch (InterruptedException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;

	}

	/**
	 * Read default IDF project template location path
	 * 
	 * @return File path for project template
	 */
	protected File getTemplateLocationPath()
	{

		Bundle bundle = Platform.getBundle(UIPlugin.PLUGIN_ID);
		URL templateURL = bundle.getEntry(IDF_TEMPLATE_PATH);
		File file = null;
		try
		{
			file = new File(FileLocator.resolve(templateURL).toURI());
		} catch (URISyntaxException e1)
		{
			e1.printStackTrace();
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}

		return file;
	}

	/**
	 * Tool chain for IDF Project
	 * 
	 * @return Cross GCC toolchain instance
	 */
	protected IToolChain getToolChain(String toolChainId)
	{
		IToolChain[] toolChains = ManagedBuildManager.getRealToolChains();
		for (IToolChain iToolChain : toolChains)
		{

			// Cross GCC
			if (iToolChain.getId().equals(toolChainId))
			{
				return iToolChain;
			}
		}
		return null;
	}

	/**
	 * Copy template project into the workspace or user selected directory in the wizard
	 * 
	 * @param projectName
	 * @param sourceTemplateLocation
	 * @param project
	 * @throws IOException
	 */
	private void copyIDFTemplateToWorkspace(String projectName, File sourceTemplateLocation, IProject project)
			throws IOException
	{

		File projectFile = project.getLocation().toFile();
		File[] files = sourceTemplateLocation.listFiles();

		for (File file : files)
		{
			// create the file/directory
			File dest = new File(projectFile, file.getName());
			if (file.isDirectory())
			{
				FileUtil.copyDirectory(file, dest); // TODO: log the IStatus
			} else
			{
				FileUtil.copyFile(file, dest);
			}
		}
	}

}
