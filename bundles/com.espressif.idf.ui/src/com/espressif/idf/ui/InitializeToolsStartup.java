/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

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
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.core.util.ToolChainUtil;
import com.espressif.idf.ui.dialogs.MessageLinkDialog;
import com.espressif.idf.ui.update.ExportIDFTools;
import com.espressif.idf.ui.update.InstallToolsHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@SuppressWarnings("restriction")
public class InitializeToolsStartup implements IStartup
{

	/**
	 * esp-idf.json is file created by the installer
	 */
	public static final String ESP_IDF_JSON_FILE = "esp_idf.json"; //$NON-NLS-1$

	// Variables defined in the esp-idf.json file
	private static final String GIT_PATH = "gitPath"; //$NON-NLS-1$
	private static final String IDF_VERSIONS_ID = "idfSelectedId"; //$NON-NLS-1$
	private static final String IDF_INSTALLED_LIST_KEY = "idfInstalled"; //$NON-NLS-1$
	private static final String PYTHON_PATH = "python"; //$NON-NLS-1$
	private static final String IDF_PATH = "path"; //$NON-NLS-1$
	private static final String IDF_VERSION = "version"; //$NON-NLS-1$
	private static final String IS_INSTALLER_CONFIG_SET = "isInstallerConfigSet"; //$NON-NLS-1$
	private static final String DOC_URL = "\"https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/partition-tables.html?highlight=partitions%20csv#creating-custom-tables\""; //$NON-NLS-1$

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
		else if (isInstallerConfigSet())
		{
			if (!isVersionDifferentInFile(idf_json_file))
			{
				return;
			}

			Logger.log("A different version for idf tool in ide env and esp_idf.json found");
			IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
			Display.getDefault().syncExec(() -> {
				Shell shell = new Shell(Display.getDefault());
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
				messageBox.setText(Messages.ToolsInitializationDifferentPathMessageBoxTitle);
				messageBox.setMessage(MessageFormat.format(Messages.ToolsInitializationDifferentPathMessageBoxMessage,
						idfEnvMgr.getEnvValue(IDFEnvironmentVariables.IDF_PATH)));
				int response = messageBox.open();
				if (response == SWT.YES)
				{
					String version = IDFUtil.getEspIdfVersion();
					java.util.regex.Pattern p = java.util.regex.Pattern.compile("([0-9][.][0-9])"); //$NON-NLS-1$
					java.util.regex.Matcher m = p.matcher(version);
					if (m.find())
					{
						version = m.group(0);
					}
					updateEspIdfJsonFile(idf_json_file, idfEnvMgr.getEnvValue(IDFEnvironmentVariables.IDF_PATH),
							idfEnvMgr.getEnvValue(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH),
							version);
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
				}
			});
		}

		// read esp-idf.json file
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

			// save state
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

		}
		catch (
				IOException
				| ParseException e)
		{
			Logger.log(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updateEspIdfJsonFile(File idf_json_file, String idfPath, String idfPythonPath, String idfVersion)
	{
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = null;
		try (FileReader reader = new FileReader(idf_json_file))
		{
			jsonObj = (JSONObject) parser.parse(reader);
			JSONObject list = (JSONObject) jsonObj.get(IDF_INSTALLED_LIST_KEY);
			if (list == null)
			{
				return;
			}
			
			// verifying if the paths are already in the file we just update the idfSelectedId to that version in esp_idf.json
			for (Object key : list.keySet())
			{
				JSONObject idfInstalled = (JSONObject) list.get(key);
				if (idfInstalled.get(IDF_PATH).equals(idfPath) && idfInstalled.get(PYTHON_PATH).equals(idfPythonPath)
						&& idfInstalled.get(IDF_VERSION).equals(idfVersion))
				{
					jsonObj.put(IDF_VERSIONS_ID, key);
					// write everything to the file here and return
					Logger.log("Already existing paths and versions found in the file with key: " + key);
					addJsonToFile(jsonObj, idf_json_file);
					return;
				}
			}
			
			// since no version was found we create an object into idfInstalled and update the idfSelectedId
			JSONObject idfVersionToAdd = new JSONObject();
			idfVersionToAdd.put(IDF_PATH, idfPath);
			idfVersionToAdd.put(PYTHON_PATH, idfPythonPath);
			idfVersionToAdd.put(IDF_VERSION, idfVersion);
			String idfVersionIdToAdd = "esp-idf-" + System.currentTimeMillis();
			list.put(idfVersionIdToAdd, idfVersionToAdd);
			jsonObj.put(IDF_INSTALLED_LIST_KEY, list);
			jsonObj.put(IDF_VERSIONS_ID, idfVersionIdToAdd);
			
			Logger.log("Created new object for json with key: " + idfVersionIdToAdd + " This will be added to esp_idf.json");
			addJsonToFile(jsonObj, idf_json_file);
		}
		catch (
				IOException
				| ParseException e)
		{
			Logger.log(e);
		}
	}
	
	private void addJsonToFile(JSONObject jsonObject, File idf_json_file) throws IOException
	{
		if (jsonObject != null)
		{
			// using gson for pretty printing
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonElement jsonElement = JsonParser.parseString(jsonObject.toJSONString());
			try (FileWriter fileWriter = new FileWriter(idf_json_file))
			{
				fileWriter.write(gson.toJson(jsonElement));
				fileWriter.flush();

			}
		}
	}

	private boolean isVersionDifferentInFile(File idf_json_file)
	{
		// read esp-idf.json file
		JSONParser parser = new JSONParser();
		try (FileReader reader = new FileReader(idf_json_file))
		{
			JSONObject jsonObj = (JSONObject) parser.parse(reader);
			String idfVersionId = (String) jsonObj.get(IDF_VERSIONS_ID);
			JSONObject list = (JSONObject) jsonObj.get(IDF_INSTALLED_LIST_KEY);
			if (list != null)
			{
				// selected esp-idf version information
				JSONObject selectedIDFInfo = (JSONObject) list.get(idfVersionId);
				String idfPath = (String) selectedIDFInfo.get(IDF_PATH);
				IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
				return !idfEnvMgr.getEnvValue(IDFEnvironmentVariables.IDF_PATH).equals(idfPath);
			}
		}
		catch (
				IOException
				| ParseException e)
		{
			Logger.log(e);
		}

		return false;
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
