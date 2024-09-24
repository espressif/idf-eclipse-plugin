package com.espressif.idf.sdk.config.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

public class OpenSdkConfigEditor extends AbstractHandler
{

	private static final String SDKCONFIG_FILE_NAME = "sdkconfig";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		IProject project = getCurrentProject();
		try
		{
			IFile sdkConfigFile = project.getFile(SDKCONFIG_FILE_NAME);
			if (sdkConfigFile.exists())
			{
				IDE.openEditor(page, sdkConfigFile);
			}
			else
			{
				MessageDialog.openError(HandlerUtil.getActiveShell(event), Messages.SDKConfigurationFileNotFound_Title,
						Messages.SDKConfigFileNotFound_ErrorMessage);
			}
		}
		catch (CoreException e)
		{
			throw new ExecutionException("Error opening sdkconfig file", e);
		}

		return null;
	}

	/**
	 * Get the currently selected project in the workspace.
	 */
	private IProject getCurrentProject()
	{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (IProject project : projects)
		{
			if (project.isOpen() && project.getFile(SDKCONFIG_FILE_NAME).exists())
			{
				return project;
			}
		}
		return null;
	}
}