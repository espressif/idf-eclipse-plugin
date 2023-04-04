/*******************************************************************************
 * Copyright (c) 2003, 2020, 2023 IBM Corporation and others and Espressif Systems (Shanghai) PTE LTD.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helmut J. Haigermoser -  Bug 359838 - The "Workspace Unavailable" error
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422954
 *     Christian Georgi (SAP) - Bug 423882 - Warn user if workspace is newer than IDE
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 427393, 455162
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 514355
 *******************************************************************************/
package com.espressif.idf.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.ide.ChooseWorkspaceData;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.ide.application.IDEApplication;
import org.osgi.framework.Bundle;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.EspIdfJsonParser;

/**
 * This is the first class that the application will start 
 * We can utilize it to configure workspace and default params as well
 * The class is derived from internal IDEApplication class and is overriden 
 * in areas where its required a lot of code is reused until a better alternative is found
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
public class InitializeEclipseStartup extends IDEApplication
{
	// Use the branding plug-in of the platform feature since this is most likely
	// to change on an update of the IDE.
	private static final String WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME = "org.eclipse.platform"; //$NON-NLS-1$
	private static final org.osgi.framework.Version WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION;
	static
	{
		Bundle bundle = Platform.getBundle(WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME);
		WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION = bundle != null ? bundle.getVersion() : null/* not installed */;
	}
	private static final String WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME_LEGACY = "org.eclipse.core.runtime"; //$NON-NLS-1$
	private static final String WORKSPACE_CHECK_LEGACY_VERSION_INCREMENTED = "2"; //$NON-NLS-1$ legacy version=1
	/**
	 * A special return code that will be recognized by the PDE launcher and used to show an error dialog if the
	 * workspace is locked.
	 */
	private static final Integer EXIT_WORKSPACE_LOCKED = Integer.valueOf(15);
	/**
	 * Return value when the user wants to retry loading the current workspace
	 */
	private static final int RETRY_LOAD = 0;

	private EspIdfJsonParser espIdfJsonParser;

	@Override
	protected Object checkInstanceLocation(Shell shell, Map applicationArguments)
	{
		Location instanceLoc = Platform.getInstanceLocation();
		if (instanceLoc == null)
		{
			MessageDialog.openError(shell, IDEWorkbenchMessages.IDEApplication_workspaceMandatoryTitle,
					IDEWorkbenchMessages.IDEApplication_workspaceMandatoryMessage);
			return EXIT_OK;
		}

		boolean force = false;

		// -data "/valid/path", workspace already set
		if (instanceLoc.isSet())
		{
			// make sure the meta data version is compatible (or the user has
			// chosen to overwrite it).
			ReturnCode result = checkValidWorkspace(shell, instanceLoc.getURL());
			if (result == ReturnCode.EXIT)
			{
				return EXIT_OK;
			}
			if (result == ReturnCode.VALID)
			{
				// at this point its valid, so try to lock it and update the
				// metadata version information if successful
				try
				{
					if (instanceLoc.lock())
					{
						writeWorkspaceVersion();
						return null;
					}

					// we failed to create the directory.
					// Two possibilities:
					// 1. directory is already in use
					// 2. directory could not be created
					File workspaceDirectory = new File(instanceLoc.getURL().getFile());
					if (workspaceDirectory.exists())
					{
						if (isDevLaunchMode(applicationArguments))
						{
							return EXIT_WORKSPACE_LOCKED;
						}
						MessageDialog.openError(shell, IDEWorkbenchMessages.IDEApplication_workspaceCannotLockTitle,
								NLS.bind(IDEWorkbenchMessages.IDEApplication_workspaceCannotLockMessage,
										workspaceDirectory.getAbsolutePath()));
					}
					else
					{
						MessageDialog.openError(shell, IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetTitle,
								IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetMessage);
					}
				}
				catch (IOException e)
				{
					IDEWorkbenchPlugin.log("Could not obtain lock for workspace location", //$NON-NLS-1$
							e);
					MessageDialog.openError(shell, IDEWorkbenchMessages.InternalError, e.getMessage());
				}
				return EXIT_OK;
			}
			if (result == ReturnCode.INVALID)
			{
				force = true;
			}
		}

		// Modifying only this part in the internal code to specify our configuration for the default workspace
		ChooseWorkspaceData launchData = new ChooseWorkspaceData(getDefaultWorkspaceURL());

		boolean parentShellVisible = false;
		if (isValid(shell))
		{
			parentShellVisible = shell.getVisible();
			// bug 455162, bug 427393: hide the splash if the workspace
			// prompt dialog should be opened
			if (parentShellVisible && launchData.getShowDialog())
			{
				shell.setVisible(false);
			}
		}

		int returnValue = -1;
		URL workspaceUrl = null;
		while (true)
		{
			if (returnValue != RETRY_LOAD)
			{
				try
				{
					workspaceUrl = promptForWorkspace(shell, launchData, force);
				}
				catch (OperationCanceledException e)
				{
					// Chosen workspace location was not compatible, select default one
					launchData = new ChooseWorkspaceData(instanceLoc.getDefault());

					// Bug 551260: ignore 'use default location' setting on retries. If the user has
					// no opportunity to set another location it would only fail again and again and
					// again.
					force = true;
					continue;
				}
			}
			if (workspaceUrl == null)
			{
				return EXIT_OK;
			}

			// if there is an error with the first selection, then force the
			// dialog to open to give the user a chance to correct
			force = true;

			try
			{
				if (instanceLoc.isSet())
				{
					// restart with new location
					return Workbench.setRestartArguments(workspaceUrl.getFile());
				}

				// the operation will fail if the url is not a valid
				// instance data area, so other checking is unneeded
				if (instanceLoc.set(workspaceUrl, true))
				{
					launchData.writePersistedData();
					writeWorkspaceVersion();
					return null;
				}
			}
			catch (IllegalStateException e)
			{
				MessageDialog.openError(shell, IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetTitle,
						IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetMessage);
				return EXIT_OK;
			}
			catch (IOException e)
			{
				MessageDialog.openError(shell, IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetTitle,
						IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetMessage);
			}

			// by this point it has been determined that the workspace is
			// already in use -- force the user to choose again
			MessageDialog dialog = new MessageDialog(null, IDEWorkbenchMessages.IDEApplication_workspaceInUseTitle,
					null, NLS.bind(IDEWorkbenchMessages.IDEApplication_workspaceInUseMessage, workspaceUrl.getFile()),
					MessageDialog.ERROR, 1, IDEWorkbenchMessages.IDEApplication_workspaceInUse_Retry,
					IDEWorkbenchMessages.IDEApplication_workspaceInUse_Choose);
			// the return value influences the next loop's iteration
			returnValue = dialog.open();
			// Remember the locked workspace as recent workspace
			launchData.writePersistedData();
		}
	}

	private String getDefaultWorkspaceURL()
	{
		espIdfJsonParser = new EspIdfJsonParser();
		espIdfJsonParser.parseJsonAndLoadValues();
		
		try
		{
			if (espIdfJsonParser.isIdfJsonPresent())
			{
				String defaultWorkspacePath = espIdfJsonParser.getDefaultWorkspaceLocation();
				Logger.log(defaultWorkspacePath);
				return defaultWorkspacePath;
			}
			else
			{
				String userHome = System.getProperty("user.home"); //$NON-NLS-1$
				Logger.log(userHome);
				String workspaceUrl = userHome.concat(File.separator).concat("workspace"); //$NON-NLS-1$
				Logger.log(workspaceUrl);
				return workspaceUrl;
			}	
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		
		return null;
	}

	/**
	 * Open a workspace selection dialog on the argument shell, populating the argument data with the user's selection.
	 * Perform first level validation on the selection by comparing the version information. This method does not
	 * examine the runtime state (e.g., is the workspace already locked?).
	 *
	 * @param shell
	 * @param launchData
	 * @param force      setting to true makes the dialog open regardless of the showDialog value
	 * @return An URL storing the selected workspace or null if the user has canceled the launch operation.
	 */
	private URL promptForWorkspace(Shell shell, ChooseWorkspaceData launchData, boolean force)
	{
		URL url = null;

		do
		{
			showChooseWorkspaceDialog(shell, launchData, force);

			String instancePath = launchData.getSelection();
			if (instancePath == null)
			{
				return null;
			}

			// the dialog is not forced on the first iteration, but is on every
			// subsequent one -- if there was an error then the user needs to be
			// allowed to fix it
			force = true;

			// 70576: don't accept empty input
			if (instancePath.length() <= 0)
			{
				MessageDialog.openError(shell, IDEWorkbenchMessages.IDEApplication_workspaceEmptyTitle,
						IDEWorkbenchMessages.IDEApplication_workspaceEmptyMessage);
				continue;
			}

			// create the workspace if it does not already exist
			File workspace = new File(instancePath);
			if (!workspace.exists())
			{
				workspace.mkdir();
			}

			try
			{
				// Don't use File.toURL() since it adds a leading slash that Platform does not
				// handle properly. See bug 54081 for more details.
				String path = workspace.getAbsolutePath().replace(File.separatorChar, '/');
				url = new URL("file", null, path); //$NON-NLS-1$
			}
			catch (MalformedURLException e)
			{
				MessageDialog.openError(shell, IDEWorkbenchMessages.IDEApplication_workspaceInvalidTitle,
						IDEWorkbenchMessages.IDEApplication_workspaceInvalidMessage);
				continue;
			}
			ReturnCode result = checkValidWorkspace(shell, url);
			if (result == ReturnCode.INVALID)
			{
				throw new OperationCanceledException("Invalid workspace location: " + url); //$NON-NLS-1$
			}
			if (result == ReturnCode.EXIT)
			{
				return null;
			}
			return url;
		} while (true);
	}

	/**
	 * Write the version of the metadata into a known file overwriting any existing file contents. Writing the version
	 * file isn't really crucial, so the function is silent about failure
	 */
	private static void writeWorkspaceVersion()
	{
		if (WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION == null)
		{
			// no reference bundle installed, no check possible
			return;
		}

		Location instanceLoc = Platform.getInstanceLocation();
		if (instanceLoc == null || instanceLoc.isReadOnly())
		{
			return;
		}

		File versionFile = getVersionFile(instanceLoc.getURL(), true);
		if (versionFile == null)
		{
			return;
		}

		Properties props = new Properties();

		// write new property
		props.setProperty(WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME, WORKSPACE_CHECK_REFERENCE_BUNDLE_VERSION.toString());

		// write legacy property with an incremented version,
		// so that pre-4.4 IDEs will also warn about the workspace
		props.setProperty(WORKSPACE_CHECK_REFERENCE_BUNDLE_NAME_LEGACY, WORKSPACE_CHECK_LEGACY_VERSION_INCREMENTED);

		try (OutputStream output = new FileOutputStream(versionFile))
		{
			props.store(output, null);
		}
		catch (IOException e)
		{
			IDEWorkbenchPlugin.log("Could not write version file", //$NON-NLS-1$
					StatusUtil.newError(e));
		}
	}

	@SuppressWarnings("rawtypes")
	private static boolean isDevLaunchMode(Map args)
	{
		// see org.eclipse.pde.internal.core.PluginPathFinder.isDevLaunchMode()
		if (Boolean.getBoolean("eclipse.pde.launch")) //$NON-NLS-1$
			return true;
		return args.containsKey("-pdelaunch"); //$NON-NLS-1$
	}

	/**
	 * Helper method to check if a widget is not null and not disposed
	 *
	 * @param widget to be checked
	 * @return true if widget is not disposed or null, false otherwise
	 * @since 3.22
	 */
	public static boolean isValid(Widget widget)
	{
		return widget != null && !widget.isDisposed();
	}
}
