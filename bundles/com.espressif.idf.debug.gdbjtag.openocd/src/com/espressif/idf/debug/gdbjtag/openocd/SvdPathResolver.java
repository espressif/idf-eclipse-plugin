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
import com.espressif.idf.core.build.IDFLaunchConstants;
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

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException
	{
		String selectedTarget = StringUtil.EMPTY;
		String selectedTargetPath = StringUtil.EMPTY;
		try
		{
			ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
			selectedTarget = launchBarManager.getActiveLaunchTarget().getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET,
					StringUtil.EMPTY);
			if (StringUtil.isEmpty(selectedTarget))
				return StringUtil.EMPTY;
			URL svdUrl = Platform.getBundle(Activator.PLUGIN_ID)
					.getResource("svd/".concat(selectedTarget.concat(".svd"))); //$NON-NLS-1$ //$NON-NLS-2$
			String jarPath = new File(TabSvdTarget.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getPath();
			if (!jarPath.contains(".jar")) //$NON-NLS-1$
			{
				selectedTargetPath = new File(FileLocator.resolve(svdUrl).toURI()).getPath();
			}
			else
			{
				IProject project = EclipseUtils
						.getProjectByLaunchConfiguration(launchBarManager.getActiveLaunchConfiguration());
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
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return selectedTargetPath;
	}

}
