/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.cmake.core.CMakeProjectGenerator;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.FileUtil;
import com.espressif.idf.ui.UIPlugin;

/**
 * IDF CMake project Generator
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
public class IDFProjectGenerator extends CMakeProjectGenerator
{

	private File sourceTemplatePath;
	private boolean copyIntoWorkspace;

	public IDFProjectGenerator(String manifestFile, File source, boolean copyIntoWorkspace)
	{
		super(manifestFile);
		this.sourceTemplatePath = source;
		this.copyIntoWorkspace = copyIntoWorkspace;
	}

	@Override
	protected void initProjectDescription(IProjectDescription description)
	{
		description.setNatureIds(
				new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, IDFProjectNature.ID });
		ICommand command = description.newCommand();
		CBuilder.setupBuilder(command);
		description.setBuildSpec(new ICommand[] { command });
		if (!copyIntoWorkspace)
		{
			description.setLocation(new Path(sourceTemplatePath.getAbsolutePath()));
		}
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException
	{
		super.generate(model, monitor);
		Logger.log("Source Template path:" + sourceTemplatePath); //$NON-NLS-1$
		if (sourceTemplatePath == null)
		{
			return; // let's go with the default generate
		}

		// Target project
		IProject project = getProject();

		if (copyIntoWorkspace)
		{
			// copy IDF template resources
			try
			{
				copyIDFTemplateToWorkspace(project.getName(), sourceTemplatePath, project);
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		}

		// refresh to see the copied resources in the project explorer
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	@Override
	public Bundle getSourceBundle()
	{
		return UIPlugin.getDefault().getBundle();
	}

	/**
	 * Copy template project resources into target project resource
	 * 
	 * @param projectName
	 * @param sourceTemplateFile
	 * @param targetProject
	 * @throws IOException
	 */
	protected void copyIDFTemplateToWorkspace(String projectName, File sourceTemplateFile, IProject targetProject)
			throws IOException
	{

		File projectFile = targetProject.getLocation().toFile();
		File[] files = sourceTemplateFile.listFiles();

		for (File file : files)
		{
			// Don't copy build folder as CMakeCache.txt file contains full path entries and leads to build issues.
			if (file.getName().equals(IDFConstants.BUILD_FOLDER))
			{
				continue;
			}

			// create the file/directory
			File dest = new File(projectFile, file.getName());
			if (file.isDirectory())
			{
				FileUtil.copyDirectory(file, dest);
			}
			else
			{
				FileUtil.copyFile(file, dest);
			}
		}
	}
}
