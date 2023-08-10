/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.Messages;
import com.espressif.idf.core.build.ReHintPair;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.PopupDialog;
import com.espressif.idf.core.resources.ResourceChangeListener;
import com.espressif.idf.core.toolchain.ESPToolChainManager;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.dialogs.BuildView;
import com.espressif.idf.ui.dialogs.MessageLinkDialog;
import com.espressif.idf.ui.update.ExportIDFTools;
import com.espressif.idf.ui.update.InstallToolsHandler;

@SuppressWarnings("restriction")
public class InitializeToolsStartup implements IStartup
{

	private static final String BUILDHINTS_ID = "com.espressif.idf.ui.views.buildhints"; //$NON-NLS-1$

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
	private static final String IS_INSTALLER_CONFIG_SET = "isInstallerConfigSet"; //$NON-NLS-1$
	private static final String DOC_URL = "\"https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/partition-tables.html?highlight=partitions%20csv#creating-custom-tables\""; //$NON-NLS-1$

	private String newIdfPath;

	@Override
	public void earlyStartup()
	{
		OpenDialogListenerSupport.getSupport().addPropertyChangeListener(evt -> {
			PopupDialog popupDialog = PopupDialog.valueOf(evt.getPropertyName());
			switch (popupDialog)
			{
			case LOW_PARTITION_SIZE:
				openLowPartitionSizeDialog(evt);
				break;
			case AVAILABLE_HINTS:
				openAvailableHintsDialog(evt);
				break;
			default:
				break;
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
			checkForUpdatedVersion(idf_json_file);
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
					IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
					updateEspIdfJsonFile(idf_json_file, idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.IDF_PATH));
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
					new ESPToolChainManager().configureToolChain();

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
	private void openAvailableHintsDialog(PropertyChangeEvent evt)
	{
		Display.getDefault().asyncExec(() ->
		{
			List<ReHintPair> erroHintPairs = (List<ReHintPair>) evt.getNewValue();
			// if list is empty we don't want to change focus from the console output
			if (erroHintPairs.isEmpty())
			{
				updateValuesInBuildView(erroHintPairs);
				return;
			}
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(BUILDHINTS_ID);
			}
			catch (PartInitException e)
			{
				Logger.log(e);
			}
			updateValuesInBuildView(erroHintPairs);
		}
		);

	}

	private void updateValuesInBuildView(List<ReHintPair> erroHintPairs)
	{
		BuildView view = ((BuildView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(BUILDHINTS_ID));
		if (view != null)
		{
			view.updateReHintsPairs(erroHintPairs);
		}
	}

	private void openLowPartitionSizeDialog(PropertyChangeEvent evt)
	{
		Display.getDefault().asyncExec(() ->
				MessageLinkDialog.openWarning(Display.getDefault().getActiveShell(),
						Messages.IncreasePartitionSizeTitle, MessageFormat.format(Messages.IncreasePartitionSizeMessage,
								evt.getNewValue(), evt.getOldValue(), DOC_URL))
			);
	}
	
	@SuppressWarnings("unchecked")
	private void updateEspIdfJsonFile(File idf_json_file, String newIdfPathToUpdate)
	{
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = null;
		try (FileReader reader = new FileReader(idf_json_file))
		{
			jsonObj = (JSONObject) parser.parse(reader);
			String idfVersionId = (String) jsonObj.get(IDF_VERSIONS_ID);
			JSONObject list = (JSONObject) jsonObj.get(IDF_INSTALLED_LIST_KEY);
			if (list == null)
			{
				return;
			}
			// selected esp-idf version information
			JSONObject selectedIDFInfo = (JSONObject) list.get(idfVersionId);
			selectedIDFInfo.put(IDF_PATH, newIdfPathToUpdate);
			list.put(idfVersionId, selectedIDFInfo);
			jsonObj.put(IDF_INSTALLED_LIST_KEY, list);
		}
		catch (
				IOException
				| ParseException e)
		{
			Logger.log(e);
		}

		if (jsonObj != null)
		{
			try (FileWriter fileWriter = new FileWriter(idf_json_file))
			{
				fileWriter.write(jsonObj.toJSONString());
				fileWriter.flush();

			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		}

	}

	private void checkForUpdatedVersion(File idf_json_file)
	{
		// read esp-idf.json file
		JSONParser parser = new JSONParser();
		try (FileReader reader = new FileReader(idf_json_file))
		{
			JSONObject jsonObj = (JSONObject) parser.parse(reader);
			String idfVersionId = (String) jsonObj.get(IDF_VERSIONS_ID);
			JSONObject list = (JSONObject) jsonObj.get(IDF_INSTALLED_LIST_KEY);
			if (list == null)
			{
				return;
			}
			// selected esp-idf version information
			JSONObject selectedIDFInfo = (JSONObject) list.get(idfVersionId);
			String idfPath = (String) selectedIDFInfo.get(IDF_PATH);
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
		catch (
				IOException
				| ParseException e)
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
