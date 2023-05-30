/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.launchbar.core.ILaunchBarListener;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.Messages;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.ResourceChangeListener;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.core.util.ToolChainUtil;
import com.espressif.idf.ui.dialogs.MessageLinkDialog;
import com.espressif.idf.ui.update.ExportIDFTools;
import com.espressif.idf.ui.update.InstallToolsHandler;

@SuppressWarnings("restriction")
public class InitializeToolsStartup implements IStartup
{

	/**
	 * esp-idf.json is file created by the installer
	 */
	private static final String ESP_IDF_JSON_FILE = "esp_idf.json"; //$NON-NLS-1$

	// Variables defined in the esp-idf.json file
	private static final String GIT_PATH = "gitPath"; //$NON-NLS-1$
	private static final String IDF_VERSIONS_ID = "idfSelectedId"; //$NON-NLS-1$
	private static final String IDF_INSTALLED_LIST_KEY = "idfInstalled"; //$NON-NLS-1$
	private static final String PYTHON_PATH = "python"; //$NON-NLS-1$
	private static final String IDF_PATH = "path"; //$NON-NLS-1$
	private static final String DOC_URL = "\"https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/partition-tables.html?highlight=partitions%20csv#creating-custom-tables\""; //$NON-NLS-1$
	private static final String LAST_MODIFIED_ESP_IDF_JSON_FILE = "lastModifed-esp_idf.json"; //$NON-NLS-1$

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
		ILaunchBarListener launchBarListener = new LaunchBarListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(launchBarListener));
		ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
		launchBarManager.addListener(launchBarListener);

		// Get the location of the eclipse root directory
		Location installLocation = Platform.getInstallLocation();
		URL url = installLocation.getURL();
		Logger.log("Eclipse Install location::" + url);
		File idf_json_file = new File(url.getPath() + File.separator + ESP_IDF_JSON_FILE);
		if (!idf_json_file.exists())
		{
			Logger.log(MessageFormat.format("esp-idf.json file doesn't exist at this location: '{0}'", url.getPath()));
			return;
		}
		else if (isConfigFileUpdated(idf_json_file))
		{
			Display.getDefault().syncExec(() -> {
				Shell shell = new Shell(Display.getDefault());
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
				messageBox.setText(Messages.ToolsInitializationDifferentPathMessageBoxTitle);
				messageBox.setMessage(MessageFormat.format(Messages.ToolsInitializationDifferentPathMessageBoxMessage,
						getFormattedDateAndTime(idf_json_file.lastModified())));
				int response = messageBox.open();
				if (response == SWT.NO)
				{
					Preferences prefs = getPreferences();
					prefs.putLong(LAST_MODIFIED_ESP_IDF_JSON_FILE, idf_json_file.lastModified());
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

		updateEnvUsingIdfJsonFile(idf_json_file);
	}

	private void updateEnvUsingIdfJsonFile(File idf_json_file)
	{
		JSONParser parser = new JSONParser();
		try
		{
			JSONObject jsonObj = (JSONObject) parser.parse(new FileReader(idf_json_file));
			String gitExecutablePath = (String) jsonObj.get(GIT_PATH);
			String idfVersionId = (String) jsonObj.get(IDF_VERSIONS_ID);
			JSONObject list = (JSONObject) jsonObj.get(IDF_INSTALLED_LIST_KEY);
			if (list != null)
			{
				// selected esp-idf version information
				JSONObject selectedIDFInfo = (JSONObject) list.get(idfVersionId);
				String idfPath = (String) selectedIDFInfo.get(IDF_PATH);
				String pythonExecutablePath = (String) selectedIDFInfo.get(PYTHON_PATH);

				// Add IDF_PATH to the eclipse CDT build environment variables
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
					scopedPreferenceStore.putLong(LAST_MODIFIED_ESP_IDF_JSON_FILE, idf_json_file.lastModified());
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
		}
		catch (
				IOException
				| ParseException e)
		{
			Logger.log(e);
		}
	}

	private boolean isConfigFileUpdated(File idfJsonFile)
	{
		long currentFileLastModified = idfJsonFile.lastModified();
		long lastModifiedDateInPrefStore = getPreferences().getLong(LAST_MODIFIED_ESP_IDF_JSON_FILE, -1);
		return currentFileLastModified > lastModifiedDateInPrefStore;
	}

	private Preferences getPreferences()
	{
		return InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
	}

	private String getFormattedDateAndTime(long timestamp)
	{
		Date date = new Date(timestamp);
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		return dateFormat.format(date);
	}
}
