/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONObject;

import com.espressif.idf.sdk.config.core.IJsonServerConfig;
import com.espressif.idf.sdk.config.core.server.CommandType;
import com.espressif.idf.sdk.config.core.server.ConfigServerManager;
import com.espressif.idf.sdk.config.core.server.JsonConfigServer;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class LoadSdkConfigHandler extends AbstractHandler
{

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		// get the selected project
		IResource project = getSelectedProject(IPageLayout.ID_PROJECT_EXPLORER);
		if (project == null)
		{
			project = getSelectedResource((IEvaluationContext) event.getApplicationContext());
		}

		if (project == null)
		{
			return project;
		}

		// get the active server instance for the project
		if (project instanceof IProject)
		{
			try
			{
				JsonConfigServer server = ConfigServerManager.INSTANCE.getServer((IProject) project);
				// load changes
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(IJsonServerConfig.VERSION, 2);
				jsonObject.put(IJsonServerConfig.LOAD, null);
				String command = jsonObject.toJSONString();

				// execute load command
				server.execute(command, CommandType.LOAD);
			}
			catch (IOException e)
			{
				throw new ExecutionException("Error while starting the json configuration server", e);
			}

		}

		return null;
	}

	public static IResource getSelectedResource(IEvaluationContext evaluationContext)
	{
		if (evaluationContext == null)
		{
			return null;
		}

		Object variable = evaluationContext.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (variable instanceof IStructuredSelection)
		{
			Object selectedObject = ((IStructuredSelection) variable).getFirstElement();
			if (selectedObject instanceof IAdaptable)
			{
				IResource resource = ((IAdaptable) selectedObject).getAdapter(IResource.class);
				if (resource != null)
				{
					return resource;
				}
			}
		}
		else
		{
			// checks the active editor
			variable = evaluationContext.getVariable(ISources.ACTIVE_EDITOR_NAME);
			if (variable instanceof IEditorPart)
			{
				IEditorInput editorInput = ((IEditorPart) variable).getEditorInput();
				if (editorInput instanceof IFileEditorInput)
				{
					return ((IFileEditorInput) editorInput).getFile();
				}
			}
		}
		return null;
	}

	private static IProject getSelectedProject(String viewID)
	{
		ISelectionService service = getActiveWorkbenchWindow().getSelectionService();
		IStructuredSelection structured = (IStructuredSelection) service.getSelection(viewID);
		if (structured instanceof IStructuredSelection)
		{
			Object selectedObject = structured.getFirstElement();
			if (selectedObject instanceof IAdaptable)
			{
				IResource resource = ((IAdaptable) selectedObject).getAdapter(IResource.class);
				if (resource != null)
				{
					return resource.getProject();
				}
			}
		}
		return null;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow()
	{
		try
		{
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		catch (IllegalStateException e)
		{
			// Workbench has not been created yet
			return null;
		}
	}

}
