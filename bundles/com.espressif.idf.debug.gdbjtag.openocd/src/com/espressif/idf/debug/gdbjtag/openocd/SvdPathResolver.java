/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.embedcdt.core.EclipseUtils;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.debug.gdbjtag.openocd.ui.TabSvdTarget;

/**
 * A resolver class for the esp_svd_path dynamic variable. Resolves SVD path by looking for appropriate files inside the
 * plugin resources
 * 
 * @author Denys Almazov <denys.almazov@espressif.com>
 */
public class SvdPathResolver implements IDynamicVariableResolver
{

	private static final ILaunchBarManager LAUNCH_BAR_MANAGER = Activator.getService(ILaunchBarManager.class);

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException
	{
		String selectedTarget = StringUtil.EMPTY;
		String selectedTargetPath = StringUtil.EMPTY;
		try
		{
			selectedTarget = LAUNCH_BAR_MANAGER.getActiveLaunchTarget().getAttribute(LaunchBarTargetConstants.TARGET,
					StringUtil.EMPTY);
			if (StringUtil.isEmpty(selectedTarget))
				return StringUtil.EMPTY;
			selectedTargetPath = resolveSvdPath(selectedTarget);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return selectedTargetPath;
	}

	private String resolveSvdPath(String target) throws Exception
	{
		URL svdUrl = Platform.getBundle(Activator.PLUGIN_ID).getResource("svd/".concat(target.concat(".svd"))); //$NON-NLS-1$ //$NON-NLS-2$
		String jarPath = new File(TabSvdTarget.class.getProtectionDomain().getCodeSource().getLocation().toURI())
				.getPath();
		String selectedTargetPath;

		if (svdUrl == null)
		{
			Logger.log("svd file is missing"); //$NON-NLS-1$
			return StringUtil.EMPTY;
		}

		if (!jarPath.contains(".jar")) //$NON-NLS-1$
			selectedTargetPath = new File(FileLocator.resolve(svdUrl).toURI()).getPath();
		else
			selectedTargetPath = resolveSvdPathFromJar(svdUrl, jarPath);
		return selectedTargetPath;
	}

	private String resolveSvdPathFromJar(URL svdUrl, String jarPath) throws Exception
	{
		IProject project = EclipseUtils
				.getProjectByLaunchConfiguration(LAUNCH_BAR_MANAGER.getActiveLaunchConfiguration());
		IFolder svdFolder = project.getFolder(IDFConstants.BUILD_FOLDER).getFolder("svd"); //$NON-NLS-1$
		if (!svdFolder.exists())
		{
			svdFolder.create(true, true, new NullProgressMonitor());
		}
		IFile svdFile = project.getFolder(IDFConstants.BUILD_FOLDER).getFile(svdUrl.getPath());
		if (!svdFile.exists())
		{
			try (JarFile jarFile = new JarFile(jarPath))
			{
				JarEntry file = (JarEntry) jarFile.getEntry(svdUrl.getFile().substring(1));
				if (file != null)
				{
					InputStream inputStream = jarFile.getInputStream(file);
					svdFile.create(inputStream, true, new NullProgressMonitor());
					inputStream.close();
				}
			}
		}
		project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
		return svdFile.getRawLocation().toOSString();
	}

}
