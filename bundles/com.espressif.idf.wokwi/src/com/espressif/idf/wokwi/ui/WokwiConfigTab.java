/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.wokwi.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.wokwi.IWokwiLaunchConstants;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class WokwiConfigTab extends AbstractLaunchConfigurationTab
{

	private Label fProjLabel;
	private Text projectTxt;
	private Button fProjButton;
	private IProject selectedProject;
	private Text wokwiProjectIdTxt;

	@Override
	public void createControl(Composite parent)
	{

		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);

		createProjectGroup(mainComposite, 1);

		Dialog.applyDialogFont(parent);
	}

	private void createProjectGroup(Composite parent, int colSpan)
	{
		Group projectGroup = new Group(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 3;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projectGroup.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projectGroup.setLayoutData(gd);

		fProjLabel = new Label(projectGroup, SWT.NONE);
		fProjLabel.setText("Project:");
		gd = new GridData();
		fProjLabel.setLayoutData(gd);

		projectTxt = new Text(projectGroup, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projectTxt.setLayoutData(gd);
		fProjButton = createPushButton(projectGroup, "Browse...", null);
		projectTxt.addModifyListener(evt -> {
			updateLaunchConfigurationDialog();
		});
		fProjButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent evt)
			{
				chooseProject();
				updateLaunchConfigurationDialog();
			}
		});

		Label projectIdLbl = new Label(projectGroup, SWT.NONE);
		projectIdLbl.setText("Wokwi Project ID:");

		wokwiProjectIdTxt = new Text(projectGroup, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		wokwiProjectIdTxt.setLayoutData(gd);
		wokwiProjectIdTxt.setMessage("328451800839488084");

	}

	private void chooseProject()
	{
		ICProject projects[];
		try
		{
			projects = CoreModel.getDefault().getCModel().getCProjects();
			ILabelProvider labelProvider = new CElementLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
			dialog.setTitle("Project Selection");
			dialog.setMessage("Choose a project to constrain the search for a program");
			dialog.setElements(projects);

			String initialProjectName = projectTxt != null ? projectTxt.getText().trim() : StringUtil.EMPTY;
			ICProject cProject = initialProjectName.isEmpty() ? null
					: CoreModel.getDefault().getCModel().getCProject(projectTxt.getText());
			if (cProject != null)
			{
				dialog.setInitialSelections(new Object[] { cProject });
			}
			if (dialog.open() == Window.OK)
			{
				selectedProject = ((ICProject) dialog.getFirstResult()).getProject();
			}
			if (projectTxt != null && selectedProject != null)
			{
				projectTxt.setText(selectedProject.getName());
			}
		}
		catch (CModelException e)
		{
			Logger.log(e);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		selectedProject = getSelectedProject();
		if (selectedProject != null)
		{
			initializeCProject(selectedProject, configuration);
		}
		try
		{
			configuration.doSave();
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	private IProject getSelectedProject()
	{
		List<IProject> projectList = new ArrayList<>(1);
		Display.getDefault().syncExec(new Runnable()
		{

			@Override
			public void run()
			{
				IProject project = EclipseUtil.getSelectedProjectInExplorer();
				projectList.add(project);
			}
		});
		IProject project = projectList.get(0);
		return project;
	}

	protected void initializeCProject(IProject project, ILaunchConfigurationWorkingCopy config)
	{
		String name = null;
		if (project != null && project.exists())
		{
			name = project.getName();
			config.setMappedResources(new IResource[] { project });

			ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(project);
			if (projDes != null)
			{
				String buildConfigID = projDes.getActiveConfiguration().getId();
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, buildConfigID);
			}

		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig)
	{
		boolean isConfigValid = super.isValid(launchConfig);
		boolean hasProject = false;
		try
		{
			hasProject = launchConfig.getMappedResources() != null
					? launchConfig.getMappedResources()[0].getProject().exists()
					: false;
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		String projectName = projectTxt.getText().trim();
		if (projectName.length() == 0)
		{
			setErrorMessage("Project not specified");
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists())
		{
			setErrorMessage("Project does not exist");
			return false;
		}
		if (!project.isOpen())
		{
			setErrorMessage("Project must be opened");
			return false;
		}

		if (isConfigValid && hasProject)
		{
			setErrorMessage(null);
		}
		return isConfigValid && hasProject;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		try
		{
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectTxt.getText());
			wc.setAttribute(IWokwiLaunchConstants.ATTR_WOKWI_PROJECT_ID, wokwiProjectIdTxt.getText());

			if (selectedProject != null)
			{
				wc.setMappedResources(new IResource[] { selectedProject });
			}

			wc.doSave();

		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		updateProjetFromConfig(configuration);
	}

	private void updateProjetFromConfig(ILaunchConfiguration configuration)
	{
		String projectName = StringUtil.EMPTY;
		String wokwiProjectId = StringUtil.EMPTY;
		try
		{
			projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					StringUtil.EMPTY);

			wokwiProjectId = configuration.getAttribute(IWokwiLaunchConstants.ATTR_WOKWI_PROJECT_ID, StringUtil.EMPTY);

		}
		catch (CoreException ce)
		{
			Logger.log(ce);
		}
		if (!projectTxt.getText().equals(projectName))
			projectTxt.setText(projectName);

		if (!StringUtil.isEmpty(wokwiProjectId))
		{
			wokwiProjectIdTxt.setText(wokwiProjectId);
		}
	}

	@Override
	public String getName()
	{
		return "Wokwi Server";
	}

}
