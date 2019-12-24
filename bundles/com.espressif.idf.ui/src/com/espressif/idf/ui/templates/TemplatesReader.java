/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;

/**
 * IDF Templates/examples reader from the IDF_PATH
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TemplatesReader
{

	/**
	 * Build the templates tree structure from the idf examples.
	 * 
	 * @return root of the TemplateNode
	 */
	public TemplateNode getTemplates()
	{
		// build a root
		TemplateNode root = new TemplateNode(null, null, null, IResource.ROOT);

		String idf_path =  new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PATH);
		Logger.log("IDF path "+ idf_path); //$NON-NLS-1$
		File idfpath_file = new File(idf_path + IPath.SEPARATOR + IDFConstants.TEMPLATE_FOLDER_NAME);
		if (idfpath_file.isDirectory())
		{
			// read the children under examples directory
			buildTemplatesRecursively(idfpath_file, root);

			return root;
		}
		return root;
	}

	/**
	 * @param idfpath_file
	 * @param root
	 */
	private void buildTemplatesRecursively(File idfpath_file, TemplateNode root)
	{
		File[] listFiles = idfpath_file.listFiles();
		for (File file : listFiles)
		{
			if (isCMakeFileExists(file))
			{
				// it's a cmake project - so add it to template
				root.add(new TemplateNode(file.getName(), file, null, IResource.PROJECT));
			} else
				if (file.isDirectory() && file.getName().equals("get-started")) //$NON-NLS-1$
				{
					// add basic templates at the root
					addBasicTemplatesAtRoot(root, file);
				} else
					if (file.isDirectory())
					{
						// so it's a folder which contains either projects or sub-folders again!
						TemplateNode folderNode = new TemplateNode(file.getName(), file, root, IResource.FOLDER);
						root.add(folderNode);
						buildTemplatesRecursively(file, folderNode);
					}
		}

	}

	/**
	 * Add basic templates directly under the tree root so that it will be easily accessible to the user. <br>
	 * For example: Hello_World and Blink from the get_started folder.
	 * 
	 * @param root
	 * @param file
	 */
	private void addBasicTemplatesAtRoot(TemplateNode root, File file)
	{
		// let's add get-started projects directly
		File[] basicTemplatesProjects = file.listFiles();
		if (basicTemplatesProjects == null)
		{
			return;
		}
		
		for (File template : basicTemplatesProjects)
		{
			if (isCMakeFileExists(template))
			{
				// it's a cmake project - so add it to template
				root.addFirst(new TemplateNode(template.getName(), template, null, IResource.PROJECT));
			}
		}
	}

	/**
	 * Check for the CMakeLists.txt file existence
	 * 
	 * @param file
	 * @return
	 */
	private boolean isCMakeFileExists(File file)
	{
		IPath cmakeListFile = new Path(file.getAbsolutePath()).append(IDFConstants.CMAKE_FILE);
		return cmakeListFile.toFile().exists();
	}

}
