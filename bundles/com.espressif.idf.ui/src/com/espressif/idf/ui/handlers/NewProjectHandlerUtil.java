/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.handlers;

import java.util.Optional;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.ManageEspIdfVersionsHandler;

/**
 * Utility class for ESP-IDF project creation checks.
 */
public class NewProjectHandlerUtil
{

	private NewProjectHandlerUtil()
	{
	}

	/**
	 * Performs the tools check and shows a popup dialog if validation fails. Maintained for backward compatibility with
	 * existing handlers. * @return true if environment is valid, false otherwise.
	 */
	public static boolean installToolsCheck()
	{
		String errorMessage = getErrorMessage();
		if (errorMessage != null)
		{
			showMessage(errorMessage);
			return false;
		}
		return true;
	}

	/**
	 * Validates the IDF environment and returns a localized error message describing the first failure found. * @return
	 * String error message or null if validation passes.
	 */
	public static String getErrorMessage()
	{
		String idfPath = IDFUtil.getIDFPath();
		if (StringUtil.isEmpty(idfPath))
		{
			return Messages.NewProjectHandler_CouldntFindIdfPath;
		}

		IEnvironmentVariable pathEnv = new IDFEnvironmentVariables().getEnv(IDFEnvironmentVariables.PATH);
		String path = Optional.ofNullable(pathEnv).map(o -> o.getValue()).orElse(null);
		if (StringUtil.isEmpty(path))
		{
			return Messages.NewProjectHandler_CouldntFindPath;
		}

		Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
		boolean isToolsInstalled = scopedPreferenceStore.getBoolean(EimConstants.INSTALL_TOOLS_FLAG, false);
		if (!isToolsInstalled)
		{
			return Messages.NewProjectHandler_CouldntFindTools;
		}

		return StringUtil.EMPTY;
	}

	/**
	 * Internal helper to show the original question dialog and trigger the version manager if the user selects 'Yes'.
	 */
	private static void showMessage(String missingMsg)
	{
		Display.getDefault().asyncExec(() ->
		{
			boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					Messages.NewProjectHandler_PathErrorTitle, missingMsg
							+ Messages.NewProjectHandler_NavigateToHelpMenu + Messages.NewProjectHandler_MandatoryMsg);
			if (isYes)
			{
				ManageEspIdfVersionsHandler manageEspIdfVersionsHandler = new ManageEspIdfVersionsHandler();
				try
				{
					manageEspIdfVersionsHandler.execute(null);
				}
				catch (ExecutionException e)
				{
					Logger.log(e);
				}
			}
		});
	}
}