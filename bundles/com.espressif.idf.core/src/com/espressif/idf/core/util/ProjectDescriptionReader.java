package com.espressif.idf.core.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
			JSONObject object = read();
			appElfFile = object.isEmpty() ? appElfFile
					: project.getFolder(IDFConstants.BUILD_FOLDER).getFile((String) object.get("app_elf")); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return appElfFile;

	}

	private JSONObject read()
	{
		IPath pathToProjectDescriptionJsonFilePath = Optional.ofNullable(project)
				.map(p -> p.getFolder(IDFConstants.BUILD_FOLDER)).map(b -> b.getFile("project_description.json")) //$NON-NLS-1$
				.map(IResource::getLocation).orElse(new Path(StringUtil.EMPTY));
		File projectDescriptionJsonFile = pathToProjectDescriptionJsonFilePath.toFile();
		JSONParser parser = new JSONParser();
		try (FileReader reader = new FileReader(projectDescriptionJsonFile))
		{
			return (JSONObject) parser.parse(reader);
		}
		catch (
				IOException
				| ParseException e)
		{
			Logger.log(e);
		}

		return new JSONObject();
	}
}
