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

public class NewProjectHandlerUtil
{

	public static boolean installToolsCheck()
	{
		// IDF_PATH
		String idfPath = IDFUtil.getIDFPath();

		// PATH
		IEnvironmentVariable pathEnv = new IDFEnvironmentVariables().getEnv(IDFEnvironmentVariables.PATH);
		String path = Optional.ofNullable(pathEnv).map(o -> o.getValue()).orElse(null);

		Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
		boolean isToolsInstalled = scopedPreferenceStore.getBoolean(EimConstants.INSTALL_TOOLS_FLAG, false);

		if (StringUtil.isEmpty(idfPath))
		{
			showMessage(Messages.NewProjectHandler_CouldntFindIdfPath);
			return false;
		}
		if (StringUtil.isEmpty(path))
		{
			showMessage(Messages.NewProjectHandler_CouldntFindPath);
			return false;
		}
		if (!isToolsInstalled)
		{
			showMessage(Messages.NewProjectHandler_CouldntFindTools);
			return false;
		}
		return true;
	}

	private static void showMessage(String missingMsg)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
						Messages.NewProjectHandler_PathErrorTitle,
						missingMsg + Messages.NewProjectHandler_NavigateToHelpMenu
								+ Messages.NewProjectHandler_MandatoryMsg);
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
			}
		});
	}
}
