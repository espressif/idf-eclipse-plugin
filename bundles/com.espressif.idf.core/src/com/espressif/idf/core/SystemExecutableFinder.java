/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import java.io.File;
import java.lang.reflect.Method;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.util.StringUtil;

public class SystemExecutableFinder implements ExecutableFinder
{

	private SystemWrapper systemWrapper;

	public SystemExecutableFinder()
	{
		this.systemWrapper = new DefaultSystemWrapper();
	}

	public SystemExecutableFinder(SystemWrapper systemWrapper)
	{
		this.systemWrapper = systemWrapper;
	}

	@Override
	public IPath find(String executableName, boolean appendExtension)
	{
		if (executableName == null)
		{
			return null;
		}

		String pathENV = systemWrapper.getPathEnv();

		if (StringUtil.isEmpty(pathENV))
		{
			return null;
		}
		String[] paths = pathENV.split(File.pathSeparator);
		for (String pathString : paths)
		{
			IPath path = Path.fromOSString(pathString).append(executableName);
			IPath execPath = findExecutable(path, appendExtension);
			if (execPath != null)
			{
				return execPath;
			}
		}
		return null;
	}

	@Override
	public IPath findExecutable(IPath path, boolean appendExtension)
	{
		if (isPlatformWindows() && appendExtension)
		{
			String pathExt = systemWrapper.getEnvExecutables();
			if (StringUtil.isEmpty(pathExt))
			{
				return null;
			}
			String[] extensions = systemWrapper.getEnvExecutables().split(File.pathSeparator);
			for (String ext : extensions)
			{
				if (ext.length() > 0 && ext.charAt(0) == '.') // $NON-NLS-1$
				{
					ext = ext.substring(1);
				}
				IPath pathWithExt = path.addFileExtension(ext);
				if (isExecutable(pathWithExt))
				{
					return pathWithExt;
				}
			}

		}
		else if (isExecutable(path))
		{
			return path;
		}
		return null;
	}

	// seam for testing
	protected boolean isPlatformWindows()
	{
		return Platform.OS_WIN32.equals(Platform.getOS());
	}

	private boolean isExecutable(IPath path)
	{
		if (path == null)
		{
			return false;
		}
		File file = path.toFile();

		if (file == null || !file.exists() || file.isDirectory())
		{
			return false;
		}

		try
		{
			Method m = File.class.getMethod("canExecute"); //$NON-NLS-1$
			if (m != null)
			{
				return (Boolean) m.invoke(file);
			}
		}
		catch (Exception e)
		{
		}

		if (isPlatformWindows())
		{
			return true;
		}
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(path);
		return fileStore.fetchInfo().getAttribute(EFS.ATTRIBUTE_EXECUTABLE);
	}
}
