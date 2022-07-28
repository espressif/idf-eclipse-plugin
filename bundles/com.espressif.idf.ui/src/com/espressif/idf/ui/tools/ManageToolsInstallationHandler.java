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
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.wizard.IToolsInstallationWizardConstants;
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
		boolean exisitngInstallPreferencesStatus = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID).getBoolean(IToolsInstallationWizardConstants.INSTALL_TOOLS_FLAG, false);
		ToolsManagerWizard toolsManagerWizard = new ToolsManagerWizard();
		ToolsManagerWizardDialog toolsManagerWizardDialog = new ToolsManagerWizardDialog(
				PlatformUI.getWorkbench().getDisplay().getActiveShell(), toolsManagerWizard, existingVarMap, exisitngInstallPreferencesStatus);
		toolsManagerWizard.setParentWizardDialog(toolsManagerWizardDialog);
		toolsManagerWizard.open();
		return null;
	}
	
	private Map<String, String> loadExistingVars()
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		return new HashMap<>(idfEnvironmentVariables.getEnvMap());
	}
}
