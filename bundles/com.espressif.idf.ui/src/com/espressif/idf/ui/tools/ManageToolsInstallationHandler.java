/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.ui.tools.wizard.ToolsManagerWizard;
import com.espressif.idf.ui.tools.wizard.ToolsManagerWizardDialog;

/**
 * Tools Installation Handler for the menu command
 * 
 * @author Ali Azam Rana
 *
 */
public class ManageToolsInstallationHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Map<String, String> existingVarMap = loadExistingVars();	
		ToolsManagerWizard toolsManagerWizard = new ToolsManagerWizard();
		ToolsManagerWizardDialog toolsManagerWizardDialog = new ToolsManagerWizardDialog(
				PlatformUI.getWorkbench().getDisplay().getActiveShell(), toolsManagerWizard, existingVarMap);
		toolsManagerWizard.setParentWizardDialog(toolsManagerWizardDialog);
		toolsManagerWizard.open();
		return null;
	}
	
	private Map<String, String> loadExistingVars()
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		Map<String, String> existingVarMap = new HashMap<>();
		
		existingVarMap.put(IDFEnvironmentVariables.GIT_PATH, idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.GIT_PATH));
		existingVarMap.put(IDFEnvironmentVariables.IDF_COMPONENT_MANAGER, idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.IDF_COMPONENT_MANAGER));
		existingVarMap.put(IDFEnvironmentVariables.IDF_PATH, idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.IDF_PATH));
		existingVarMap.put(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH, idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH));
		existingVarMap.put(IDFEnvironmentVariables.OPENOCD_SCRIPTS, idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS));
		existingVarMap.put(IDFEnvironmentVariables.PATH, idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH));
		existingVarMap.put(IDFEnvironmentVariables.PYTHON_EXE_PATH, idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PYTHON_EXE_PATH));
		
		return existingVarMap;
	}
}
