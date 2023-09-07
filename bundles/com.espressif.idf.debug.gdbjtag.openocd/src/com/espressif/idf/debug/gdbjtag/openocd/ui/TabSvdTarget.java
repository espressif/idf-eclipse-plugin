/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.ui;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.embedcdt.core.EclipseUtils;
import org.eclipse.embedcdt.debug.gdbjtag.ui.TabSvd;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;

/**
 * Svd target class for loading the svd files from the plugin directly
 * 
 * @author Ali Azam Rana
 *
 */
public class TabSvdTarget extends TabSvd
{
	public TabSvdTarget()
	{
		super();
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		String selectedTarget = StringUtil.EMPTY;
		try
		{
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			selectedTarget = wc.getAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, StringUtil.EMPTY);
			if (StringUtil.isEmpty(selectedTarget))
			{
				selectedTarget = Activator.getService(ILaunchBarManager.class).getActiveLaunchTarget()
						.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY);
			}
			updateSvd(selectedTarget, wc);
			wc.doSave();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}

		super.initializeFrom(configuration);
	}

	public static void updateSvd(String selectedTarget, ILaunchConfigurationWorkingCopy wc)
	{
		try
		{
			if (StringUtil.isEmpty(selectedTarget))
				return;
			URL svdUrl = Platform.getBundle(Activator.PLUGIN_ID)
					.getResource("svd/".concat(selectedTarget.concat(".svd"))); //$NON-NLS-1$ //$NON-NLS-2$
			String jarPath = new File(TabSvdTarget.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getPath();
			String selectedTargetPath = StringUtil.EMPTY;
			if (!jarPath.contains(".jar")) //$NON-NLS-1$
			{
				selectedTargetPath = new File(FileLocator.resolve(svdUrl).toURI()).getPath();
			}
			else
			{
				IProject project = EclipseUtils.getProjectByLaunchConfiguration(wc);
				IFolder svdFolder = project.getFolder(IDFConstants.BUILD_FOLDER).getFolder("svd"); //$NON-NLS-1$
				if (!svdFolder.exists())
				{
					svdFolder.create(true, true, new NullProgressMonitor());
				}
				IFile svdFile = project.getFolder(IDFConstants.BUILD_FOLDER).getFile(svdUrl.getPath());
				if (!svdFile.exists())
				{
					JarFile jarFile = new JarFile(jarPath);
					JarEntry file = (JarEntry) jarFile.getEntry(svdUrl.getFile().substring(1));
					if (file != null)
					{
						InputStream inputStream = jarFile.getInputStream(file);
						svdFile.create(inputStream, true, new NullProgressMonitor());
						inputStream.close();
					}
					jarFile.close();
				}
				project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
				selectedTargetPath = svdFile.getRawLocation().toOSString();
			}

			String currentSvdPath = wc.getAttribute(
					org.eclipse.embedcdt.debug.gdbjtag.core.ConfigurationAttributes.SVD_PATH, StringUtil.EMPTY);
			if (StringUtil.isEmpty(currentSvdPath) || !currentSvdPath.equals(selectedTargetPath))
			{
				wc.setAttribute(org.eclipse.embedcdt.debug.gdbjtag.core.ConfigurationAttributes.SVD_PATH,
						selectedTargetPath);
			}
		}
		catch (Exception e)
		{
			selectedTarget = StringUtil.EMPTY;
			Logger.log(e);
		}
	}
}
