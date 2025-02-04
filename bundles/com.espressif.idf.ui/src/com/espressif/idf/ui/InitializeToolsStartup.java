/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.build.Messages;
import com.espressif.idf.core.build.ReHintPair;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.PopupDialog;
import com.espressif.idf.core.resources.ResourceChangeListener;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.dialogs.BuildView;
import com.espressif.idf.ui.dialogs.MessageLinkDialog;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerEditor;
import com.espressif.idf.ui.tools.manager.EimEditorInput;

@SuppressWarnings("restriction")
public class InitializeToolsStartup implements IStartup
{

	private static final String BUILDHINTS_ID = "com.espressif.idf.ui.views.buildhints"; //$NON-NLS-1$

	/**
	 * esp-idf.json is file created by the installer
	 */
	public static final String ESP_IDF_JSON_FILE = "esp_idf.json"; //$NON-NLS-1$

	private static final String DOC_URL = "\"https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/partition-tables.html?highlight=partitions%20csv#creating-custom-tables\""; //$NON-NLS-1$

	private LaunchBarListener launchBarListener;
	private EimIdfConfiguratinParser eimIdfConfiguratinParser;
	private EimJson eimJson;
	
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
			case DISABLE_LAUNCHABAR_EVENTS:
				disableLaunchBarEvents(evt);
				break;
			case ENABLE_LAUNCHBAR_EVENTS:
				enableLaunchBarEvents(evt);
				break;
			default:
				break;
			}
		});
		launchBarListener = new LaunchBarListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(launchBarListener));
		ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
		launchBarManager.addListener(launchBarListener);

		checkAndHandleOldConfig();
		if (isEimInstalled())
		{
			notifyAndSetEspIdf();
		}
	}
	
	private void notifyAndSetEspIdf()
	{
		if (eimIdfConfiguratinParser == null)
		{
			eimIdfConfiguratinParser = new EimIdfConfiguratinParser();
		}
		
		try
		{
			eimJson = eimIdfConfiguratinParser.getEimJson(true);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		
		if (!isEspIdfSet())
		{
			Display.getDefault().syncExec(()-> {
				String skipTestsValue = System.getProperty("skipTests");
				Logger.log("skipTests: " + skipTestsValue);
				if (!StringUtil.isEmpty(skipTestsValue) && !Boolean.parseBoolean(skipTestsValue))
				{
					openEspIdfManager();
					return;
				}
				Shell shell = Display.getDefault().getActiveShell();
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING| SWT.YES | SWT.NO);
				messageBox.setText(Messages.NoActiveEspIdfInWorkspaceMsgTitle);
				messageBox.setMessage(Messages.NoActiveEspIdfInWorkspaceMsg);
				
				int response = messageBox.open();
				if (response == SWT.YES)
				{
					openEspIdfManager();
				}
			});
		}
	}
	
	private void openEspIdfManager()
	{
		IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
		IDFUtil.closeWelcomePage(activeww);
		try
		{
			IDE.openEditor(activeww.getActivePage(), new EimEditorInput(eimJson), ESPIDFManagerEditor.EDITOR_ID);
		}
		catch (PartInitException e)
		{
			Logger.log(e);
		}
	}
	
	private boolean isEimInstalled()
	{
		boolean eimIdfJsonExists = isEimIdfJsonPresent();
		if (!eimIdfJsonExists)
		{
			userNotficationToInstallEim();
		}
		
		return eimIdfJsonExists;
	}

	private void checkAndHandleOldConfig()
	{
		if (isOldConfigExported())
			return;
		
		if (isOldEspIdfConfigPresnet())
		{
			File oldConfig = getOldConfigFile();
			Display.getDefault().asyncExec(() -> {
				Shell shell = Display.getDefault().getActiveShell();
				boolean userWantsToExport = MessageDialog.openQuestion(shell, Messages.OldConfigFoundMsgBoxTitle,
						Messages.OldConfigFoundMsgBoxMsg);

				String selectedDirectory = StringUtil.EMPTY;
				if (userWantsToExport)
				{
					DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.SAVE);
					directoryDialog.setText(Messages.OldConfigExportDirectorSelectionDialogTitle);
					directoryDialog.setMessage(Messages.OldConfigExportDirectorSelectionDialogInfo);
					selectedDirectory = directoryDialog.open();
				}

				if (!StringUtil.isEmpty(selectedDirectory))
				{
					File destinationFile = new File(selectedDirectory, oldConfig.getName());
					try
					{
						Files.copy(oldConfig.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
						MessageDialog.openInformation(shell, Messages.OldConfigExportCompleteSuccessMsgTitle,
								MessageFormat.format(Messages.OldConfigExportCompleteSuccessMsg,
										destinationFile.getAbsolutePath()));
					}
					catch (IOException e)
					{
						MessageDialog.openError(shell, Messages.OldConfigExportCompleteFailMsgTitle,
								MessageFormat.format(Messages.OldConfigExportCompleteFailMsg, e.getMessage()));
						Logger.log(e);
					}
					
					getPreferences().putBoolean(EimConstants.OLD_CONFIG_EXPORTED_FLAG, true);
				}
			});
		}
	}

	private boolean isEimIdfJsonPresent()
	{
		String path = StringUtil.EMPTY;
		if (Platform.getOS().equals(Platform.OS_WIN32))
		{
			 path = EimConstants.EIM_WIN_PATH;
		}
		else
		{
			path = EimConstants.EIM_POSIX_PATH;
		}
		
		File file = new File(path);
		return file.exists();
	}
	
	private boolean isOldEspIdfConfigPresnet()
	{
		return getOldConfigFile().exists();
	}
	
	private File getOldConfigFile()
	{
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(path.toOSString());
		stringBuilder.append(File.separatorChar);
		stringBuilder.append(EimConstants.TOOL_SET_CONFIG_LEGACY_CONFIG_FILE);
		return new File(stringBuilder.toString());
	}

	private void userNotficationToInstallEim()
	{
		Display.getDefault().asyncExec(()-> {
			Shell shell = new Shell(Display.getDefault());
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
			
			messageBox.setText(Messages.ToolsInitializationEimMissingMsgBoxTitle);
			messageBox.setMessage(Messages.ToolsInitializationEimMissingMsgBoxMessage);
			int response = messageBox.open();
			if (response == SWT.YES)
			{
				Program.launch(EimConstants.EIM_URL);
			}	
		});
	}

	@SuppressWarnings("static-access")
	private void disableLaunchBarEvents(PropertyChangeEvent evt)
	{
		launchBarListener.setIgnoreTargetChange(true);
	}
	
	@SuppressWarnings("static-access")
	private void enableLaunchBarEvents(PropertyChangeEvent evt)
	{
		launchBarListener.setIgnoreTargetChange(false);
	}

	@SuppressWarnings("unchecked")
	private void openAvailableHintsDialog(PropertyChangeEvent evt)
	{
		Display.getDefault().asyncExec(() -> {
			List<ReHintPair> erroHintPairs = (List<ReHintPair>) evt.getNewValue();
			// if list is empty we don't want to change focus from the console output
			if (erroHintPairs.isEmpty())
			{
				updateValuesInBuildView(erroHintPairs);
				return;
			}
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(BUILDHINTS_ID);
			}
			catch (PartInitException e)
			{
				Logger.log(e);
			}
			updateValuesInBuildView(erroHintPairs);
		});
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
		Display.getDefault()
				.asyncExec(() -> MessageLinkDialog.openWarning(Display.getDefault().getActiveShell(),
						Messages.IncreasePartitionSizeTitle, MessageFormat.format(Messages.IncreasePartitionSizeMessage,
								evt.getNewValue(), evt.getOldValue(), DOC_URL)));
	}

	private Preferences getPreferences()
	{
		return InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
	}
	
	private boolean isOldConfigExported()
	{
		return getPreferences().getBoolean(EimConstants.OLD_CONFIG_EXPORTED_FLAG, false);
	}

	private boolean isEspIdfSet()
	{
		return getPreferences().getBoolean(EimConstants.INSTALL_TOOLS_FLAG, false);
	}
}
