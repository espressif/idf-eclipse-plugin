package com.espressif.idf.core.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.logging.Logger;

public class ProjectDescriptionReader
{
	IProject project;

	public ProjectDescriptionReader(IProject project)
	{
		this.project = project;
	}

	public IFile getAppElfFile()
	{
		IFile appElfFile = null;
		try
		{
			String appElfFileName = getAppElfFileName();
			appElfFile = appElfFileName.isEmpty() ? appElfFile
					: project.getFolder(IDFConstants.BUILD_FOLDER).getFile(appElfFileName);
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
