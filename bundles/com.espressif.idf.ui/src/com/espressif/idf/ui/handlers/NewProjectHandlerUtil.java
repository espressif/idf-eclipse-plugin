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
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.update.InstallToolsHandler;

public class NewProjectHandlerUtil
{
	private static final String INSTALL_TOOLS_FLAG = "INSTALL_TOOLS_FLAG"; //$NON-NLS-1$

	public static boolean installToolsCheck()
	{
		// IDF_PATH
		String idfPath = IDFUtil.getIDFPath();

		// PATH
		IEnvironmentVariable pathEnv = new IDFEnvironmentVariables().getEnv(IDFEnvironmentVariables.PATH);
		String path = Optional.ofNullable(pathEnv).map(o -> o.getValue()).orElse(null);

		Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
		boolean isToolsInstalled = scopedPreferenceStore.getBoolean(INSTALL_TOOLS_FLAG, false);

		if (StringUtil.isEmpty(idfPath) || StringUtil.isEmpty(path) || !isToolsInstalled)
		{
			showMessage();
			return false;
		}
		return true;
	}

	private static void showMessage()
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
						Messages.NewProjectHandler_PathErrorTitle,
						Messages.NewProjectHandler_CouldntFindPaths + Messages.NewProjectHandler_NavigateToHelpMenu
								+ Messages.NewProjectHandler_MandatoryMsg);
				if (isYes)
				{
					InstallToolsHandler installToolsHandler = new InstallToolsHandler();
					try
					{
						installToolsHandler.setCommandId("com.espressif.idf.ui.command.install"); //$NON-NLS-1$
						installToolsHandler.execute(null);
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
