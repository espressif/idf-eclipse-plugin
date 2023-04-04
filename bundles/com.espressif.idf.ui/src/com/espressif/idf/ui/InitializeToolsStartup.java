/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.Messages;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.ResourceChangeListener;
import com.espressif.idf.core.util.EspIdfJsonParser;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.core.util.ToolChainUtil;
import com.espressif.idf.ui.dialogs.MessageLinkDialog;
import com.espressif.idf.ui.update.ExportIDFTools;
import com.espressif.idf.ui.update.InstallToolsHandler;

@SuppressWarnings("restriction")
public class InitializeToolsStartup implements IStartup
{
	private static final String IS_INSTALLER_CONFIG_SET = "isInstallerConfigSet"; //$NON-NLS-1$
	private static final String DOC_URL = "\"https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/partition-tables.html?highlight=partitions%20csv#creating-custom-tables\""; //$NON-NLS-1$

	private String newIdfPath;
	private EspIdfJsonParser espIdfJsonParser;

	@Override
	public void earlyStartup()
	{
		OpenDialogListenerSupport.getSupport().addPropertyChangeListener(new PropertyChangeListener()
		{

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Display.getDefault().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						MessageLinkDialog.openWarning(Display.getDefault().getActiveShell(),
								Messages.IncreasePartitionSizeTitle,
								MessageFormat.format(Messages.IncreasePartitionSizeMessage, evt.getNewValue(),
										evt.getOldValue(), DOC_URL));
					}
				});

			}
		});
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener());
		ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
		launchBarManager.addListener(new LaunchBarListener());

		espIdfJsonParser = new EspIdfJsonParser();
		espIdfJsonParser.parseJsonAndLoadValues();
		if (!espIdfJsonParser.isIdfJsonPresent())
		{
			return;
		}
		else if (isInstallerConfigSet())
		{
			checkForUpdatedVersion();
			if (isInstallerConfigSet())
			{
				Logger.log("Ignoring esp_idf.json settings as it was configured earilier and idf_path is similar.");
				return;
			}

			IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
			Display.getDefault().syncExec(() -> {
				Shell shell = new Shell(Display.getDefault());
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
				messageBox.setText(Messages.ToolsInitializationDifferentPathMessageBoxTitle);
				messageBox.setMessage(MessageFormat.format(Messages.ToolsInitializationDifferentPathMessageBoxMessage,
						newIdfPath, idfEnvMgr.getEnvValue(IDFEnvironmentVariables.IDF_PATH)));
				int response = messageBox.open();
				if (response == SWT.NO)
				{
					Preferences prefs = getPreferences();
					prefs.putBoolean(IS_INSTALLER_CONFIG_SET, true);
					try
					{
						prefs.flush();
					}
					catch (BackingStoreException e)
					{
						Logger.log(e);
					}

					return;
				}
			});
		}

		// read esp-idf.json file configs
		String gitExecutablePath = espIdfJsonParser.getGitExecutablePath();
		String idfPath = espIdfJsonParser.getIdfPath();
		String pythonExecutablePath = espIdfJsonParser.getPythonExecutablePath();
		IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
		idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfPath);

		if (!StringUtil.isEmpty(pythonExecutablePath) && !StringUtil.isEmpty(gitExecutablePath))
		{
			ExportIDFTools exportIDFTools = new ExportIDFTools();
			exportIDFTools.runToolsExport(pythonExecutablePath, gitExecutablePath, null, null);

			// Configure toolchains
			ToolChainUtil.configureToolChain();

			Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
			scopedPreferenceStore.putBoolean(InstallToolsHandler.INSTALL_TOOLS_FLAG, true);
			try
			{
				scopedPreferenceStore.flush();
			}
			catch (BackingStoreException e)
			{
				Logger.log(e);
			}
		}
	}

	private void checkForUpdatedVersion()
	{
		String idfPath = espIdfJsonParser.getIdfPath();
		IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
		if (idfEnvMgr.getEnvValue(IDFEnvironmentVariables.IDF_PATH).equals(idfPath))
			return;
		newIdfPath = idfPath;
		Preferences prefs = getPreferences();
		prefs.putBoolean(IS_INSTALLER_CONFIG_SET, false);
		try
		{
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			Logger.log(e);
		}
	}

	private Preferences getPreferences()
	{
		return InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
	}

	private boolean isInstallerConfigSet()
	{
		return getPreferences().getBoolean(IS_INSTALLER_CONFIG_SET, false);
	}
}
