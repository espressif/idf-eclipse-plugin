/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.core.util.WinNativeFileTagOperations;

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
	public IPath find(String executableName)
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
			IPath execPath = findExecutable(path);
			if (execPath != null)
			{
				return execPath;
			}
		}
		return null;
	}

	private IPath findExecutable(IPath path)
	{
		if (isPlatformWindows())
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
		else 
		{
			try
			{
				Runtime.getRuntime().exec("/bin/chmod 755 ".concat(path.toOSString())); //$NON-NLS-1$
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
			
			if (isExecutable(path))
			{
				return path;
			}
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
		File file = path.toFile();
		
		if (isPlatformWindows() && !file.exists())
		{
			if (WinNativeFileTagOperations.fileExists(file))
			{
				return IDFUtil.isReparseTag(file);
			}
		}
		
		
		if (!file.exists() || file.isDirectory())
		{
			return false;
		}
		
		return file.canExecute();
	}
}
