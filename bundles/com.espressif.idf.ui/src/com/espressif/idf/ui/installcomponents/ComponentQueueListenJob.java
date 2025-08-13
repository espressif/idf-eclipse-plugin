/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.espressif.idf.core.component.registry.ComponentToAdd;
import com.espressif.idf.core.component.vo.ComponentVO;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.installcomponents.handler.InstallCommandHandler;

/**
 * Job to listen for components that are available to be added
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class ComponentQueueListenJob extends Job
{
	private static final IStatus JOB_EXIT_STATUS = new Status(IStatus.WARNING, UIPlugin.PLUGIN_ID, IStatus.WARNING,
			StringUtil.EMPTY, null);

	public ComponentQueueListenJob()
	{
		super(ComponentQueueListenJob.class.getSimpleName());
		setSystem(true); // Set the job as a system job
		setUser(false); // Do not show in the UI
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		ComponentToAdd componentToAdd = ComponentToAdd.getInstance();
		while (true)
		{
			if (componentToAdd.isComponentAvailableToAdd())
			{
				try
				{
					// Get the component to add from the queue
					ComponentVO component = componentToAdd.getComponentToAdd();
					if (component != null)
					{
						Logger.log("Processing component for addition: " + component.getName() + " from namespace: "
								+ component.getNamespace() + " with version: " + component.getVersion());

						// get list of all the opened projects in ide
						IWorkspace workspace = ResourcesPlugin.getWorkspace();
						if (workspace == null || workspace.getRoot() == null)
						{
							Logger.log("Workspace is null, cannot process component: " + component.toString());
							componentToAdd.addComponent(component); // Re-add to queue for later processing
							continue;
						}

						IProject[] projects = workspace.getRoot().getProjects();
						if (projects == null || projects.length == 0)
						{
							Logger.log("No projects found in workspace, cannot process component: "
									+ component.toString());
							continue;
						}

						if (projects.length > 1)
						{
							Logger.log(
									"Multiple projects found in workspace, get user to select a project to add component: "
											+ component.toString());
							AddComponentMultiProjectSelectionDialog dialog = new AddComponentMultiProjectSelectionDialog(projects);
							IProject selectedProject = dialog.getSelectedProject();
							if (selectedProject == null)
							{
								Logger.log("No project selected, cannot process component: " + component.toString());
								componentToAdd.addComponent(component); // Re-add to queue for later processing
								continue;
							}
							else
							{
								InstallCommandHandler installCommandHandler = new InstallCommandHandler(
										component.getName(), component.getNamespace(), component.getVersion(),
										selectedProject);
								installCommandHandler.executeInstallCommand();
								Logger.log("Component added Successfully: " + component.toString());
							}
						}
						else
						{
							InstallCommandHandler installCommandHandler = new InstallCommandHandler(component.getName(),
									component.getNamespace(), component.getVersion(), projects[0]);
							installCommandHandler.executeInstallCommand();
							Logger.log("Component added Successfully: " + component.toString());
						}
					}
				}
				catch (Exception e)
				{
					Logger.log("Error processing component: " + componentToAdd.toString());
					Logger.log(e);
				}
			}
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				break;
			}
		}
		return JOB_EXIT_STATUS;
	}

	private class AddComponentMultiProjectSelectionDialog
	{
		IProject[] projects;

		public AddComponentMultiProjectSelectionDialog(IProject[] projects)
		{
			this.projects = projects;
			// Initialize dialog with the list of projects
			if (projects == null || projects.length == 0)
			{
				Logger.log("No projects available for selection.");
			}
			else
			{
				for (IProject project : projects)
				{
					Logger.log("Available project: " + project.getName());
				}
			}
		}

		public IProject getSelectedProject()
		{
			final IProject[] selectedProject = new IProject[1];
			Display display = Display.getDefault();
			display.syncExec(() -> {
				Shell shell = new Shell(display);
				List<String> projectNames = new ArrayList<>();
				for (IProject project : projects)
				{
					projectNames.add(project.getName());
				}
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
				dialog.setElements(projectNames.toArray());
				dialog.setTitle("Select Project");
				dialog.setMessage("Select the project to add the component to:");
				if (dialog.open() == Window.OK)
				{
					Object[] result = dialog.getResult();
					if (result != null && result.length > 0)
					{
						String selectedName = (String) result[0];
						for (IProject project : projects)
						{
							if (project.getName().equals(selectedName))
							{
								selectedProject[0] = project;
								break;
							}
						}
					}
				}
				shell.dispose();
			});
			return selectedProject[0];
		}
	}
}