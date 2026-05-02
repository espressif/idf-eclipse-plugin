/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.ui.ILaunchBarUIManager;
import org.eclipse.launchbar.ui.internal.commands.LaunchActiveCommandHandler;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;

@SuppressWarnings("restriction")
public class RunActionHandler extends LaunchActiveCommandHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		try
		{
			ILaunchBarManager launchBarManager = UIPlugin.getService(ILaunchBarManager.class);
			new StopLaunchBuildHandler().stop();

			ILaunchConfiguration config = launchBarManager.getActiveLaunchConfiguration();
			if (config == null)
			{
				return Status.OK_STATUS;
			}
			boolean isUnifiedEspConfig = config.getType().getIdentifier()
					.contentEquals(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE);

			if (!isUnifiedEspConfig)
			{
				return super.execute(event);
			}

			ILaunchMode launchMode = launchBarManager.getActiveLaunchMode();
			if (launchMode == null)
			{
				return Status.OK_STATUS;
			}

			String projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					StringUtil.EMPTY);
			if (projectName.isBlank())
			{
				Boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
						Messages.RunActionHandler_NoProjectQuestionTitle,
						Messages.RunActionHandler_NoProjectQuestionText);
				if (Boolean.TRUE.equals(isYes))
				{
					ILaunchBarUIManager uiManager = UIPlugin.getService(ILaunchBarUIManager.class);
					uiManager.openConfigurationEditor(launchBarManager.getActiveLaunchDescriptor());
				}
				return Status.CANCEL_STATUS;
			}

			launchBarManager.setActiveLaunchMode(launchMode);
			config = launchBarManager.getActiveLaunchConfiguration();
			DebugUITools.launch(config, launchMode.getIdentifier());

			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			return e.getStatus();
		}
	}
}
