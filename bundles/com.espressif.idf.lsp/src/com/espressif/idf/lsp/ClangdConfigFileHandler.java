/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.lsp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.yaml.snakeyaml.Yaml;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
public class ClangdConfigFileHandler
{
	@SuppressWarnings("unchecked")
	public void update(IProject project) throws FileNotFoundException
	{
		File file = getClangdConfigFile(project);

		// Load existing clangd file
		FileInputStream inputStream = new FileInputStream(file);
		Yaml yaml = new Yaml();
		Object obj = yaml.load(inputStream);
		if (obj instanceof Map)
		{
			Map<String, Object> data = (Map<String, Object>) obj;

			// Add new attribute to CompileFlags
			Map<String, Object> compileFlags = (Map<String, Object>) data.get("CompileFlags"); //$NON-NLS-1$
			compileFlags.put("Remove", new String[] { "-fno-tree-switch-conversion", "-fstrict-volatile-bitfields" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			// Write updated clangd back to file
			try (Writer writer = new FileWriter(file))
			{
				yaml.dump(data, writer);
			}
			catch (IOException e)
			{
				Logger.log("Error writing .clangd file: " + e.getMessage()); //$NON-NLS-1$
			}
		}
		else
		{
			Logger.log("Invalid .clangd file format."); //$NON-NLS-1$
		}
	}

	private File getClangdConfigFile(IProject project)
	{
		// Path to the existing clangd file
		IFile file = project.getFile(ILSPConstants.CLANGD_CONFIG_FILE);
		return file.getLocation().toFile();
	}
}
