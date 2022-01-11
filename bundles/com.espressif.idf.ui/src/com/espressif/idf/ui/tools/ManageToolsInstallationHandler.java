/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.tools.shell.ManageToolsInstallationShell;

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
		ToolsJsonParser toolsJsonParser = new ToolsJsonParser();
		if (StringUtil.isEmpty(IDFUtil.getIDFPath()))
		{
			// Select IDF DIRECTORY
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING);
			messageBox.setText(Messages.Warning);
			messageBox.setMessage(Messages.WarningMessagebox);
			messageBox.open();

			DirectoryDialog dlg = new DirectoryDialog(new Shell());
			dlg.setText(Messages.DirectorySelectionDialog_IDFDirLabel);
			dlg.setMessage(Messages.DirectorySelectionDialog_SelectIDFDirMessage);

			String dir = dlg.open();
			if (dir != null)
			{
				IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
				idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, dir);
			}
		}
		try
		{
			toolsJsonParser.loadJson();
			ManageToolsInstallationShell manageToolsInstallationShell = new ManageToolsInstallationShell(
					toolsJsonParser.getToolsList());
			manageToolsInstallationShell.openShell();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}
}
