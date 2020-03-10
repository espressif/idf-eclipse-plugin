package com.espressif.idf.core;

import java.io.File;
import java.lang.reflect.Method;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.util.StringUtil;

public class ExecutableFinder
{
	private static final String PATH = "PATH"; //$NON-NLS-1$
	private static final String PATHEXT = "PATHEXT"; //$NON-NLS-1$

	public static IPath find(String executableName, boolean appendExtension)
	{
		if (executableName == null)
		{
			return null;
		}

		String pathENV = System.getenv(PATH);
		if (StringUtil.isEmpty(pathENV))
		{
			return null;
		}
		String[] paths = pathENV.split(File.pathSeparator);
		for (String pathString : paths)
		{
			IPath path = Path.fromOSString(pathString).append(executableName);
			return findExecutable(path, appendExtension);
		}
		return null;
	}

	private static IPath findExecutable(IPath path, boolean appendExtension)
	{
		if (Platform.OS_WIN32.equals(Platform.getOS()) && appendExtension)
		{
			String pathExt = System.getenv(PATHEXT);
			if (StringUtil.isEmpty(pathExt))
			{
				return null;
			}
			String[] extensions = System.getenv(PATHEXT).split(File.pathSeparator);
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

	private static boolean isExecutable(IPath path)
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

		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			return true;
		}
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(path);
		return fileStore.fetchInfo().getAttribute(EFS.ATTRIBUTE_EXECUTABLE);
	}
}
