/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
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

		// get the selected resource
		IResource resource = getSelectedResource((IEvaluationContext) event.getApplicationContext());
		if (resource == null)
		{
			return resource;
		}
		
		if (!(resource instanceof IFile))
		{
			return resource;
		}
		
		IProject project = resource.getProject();
		IFile file = (IFile) resource;
		
		try
		{
			JsonConfigServer server = ConfigServerManager.INSTANCE.getServer(project, file);
			// load changes
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(IJsonServerConfig.VERSION, 2);
			jsonObject.put(IJsonServerConfig.LOAD, null);
			String command = jsonObject.toJSONString();

			// execute load command
			server.execute(command, CommandType.LOAD);
		}
		catch (Exception e)
		{
			throw new ExecutionException(Messages.LoadSdkConfigHandler_ErrorLoadingJsonConfigServer, e);
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
