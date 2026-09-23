package com.espressif.idf.core.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.logging.Logger;

public class ProjectDescriptionReader
{
	IProject project;

	public ProjectDescriptionReader(IProject project)
	{
		this.project = project;
	}

	/**
	 * @return workspace file for the application ELF, or {@code null} when the configured build directory is external
	 * @deprecated Use {@link #getAppElfFileLocation()} for custom directories outside the workspace.
	 */
	@Deprecated(forRemoval = true)
	public IFile getAppElfFile()
	{
		File appElfFile = getAppElfFileLocation();
		return appElfFile == null ? null
				: ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(appElfFile.getPath()));
	}

	public File getAppElfFileLocation()
	{
		File appElfFile = null;
		try
		{
			String appElfFileName = getAppElfFileName();
			appElfFile = appElfFileName.isEmpty() ? appElfFile : new File(IDFUtil.getBuildDir(project), appElfFileName);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return appElfFile;

	}

	private String getAppElfFileName()
	{
		String appElfFileName = StringUtil.EMPTY;
		try
		{
			String buildDir = IDFUtil.getBuildDir(project);
			String filePath = buildDir + File.separator + IDFConstants.PROECT_DESCRIPTION_JSON;
			GenericJsonReader jsonReader = new GenericJsonReader(filePath);
			appElfFileName = jsonReader.getValue("app_elf"); //$NON-NLS-1$
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		return appElfFileName;
	}

	public String getIdfPath()
	{
		String idfPath = StringUtil.EMPTY;
		try
		{
			String buildDir = IDFUtil.getBuildDir(project);
			String filePath = buildDir + File.separator + IDFConstants.PROECT_DESCRIPTION_JSON;
			if (Files.notExists(Paths.get(filePath)))
			{
				return idfPath;
			}
			GenericJsonReader jsonReader = new GenericJsonReader(filePath);
			idfPath = jsonReader.getValue("idf_path"); //$NON-NLS-1$
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		return idfPath;
	}
}
