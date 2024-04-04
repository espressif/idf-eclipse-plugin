/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.lsp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
public class ClangdConfigFileHandler
{
	@SuppressWarnings("unchecked")
	public void update(IProject project) throws CoreException, IOException
	{
		File file = getClangdConfigFile(project);

		// Load existing clangd file
		FileInputStream inputStream = new FileInputStream(file);
		Yaml yaml = new Yaml();
		Object obj = yaml.load(inputStream);

		// Create new YAML structure if file is empty
		Map<String, Object> data = createOrGetExistingYamlStructure(obj);

		// Add or update CompileFlags section
		Map<String, Object> compileFlags = (Map<String, Object>) data.get("CompileFlags");
		if (compileFlags == null)
		{
			compileFlags = new LinkedHashMap<>();
			data.put("CompileFlags", compileFlags); //$NON-NLS-1$
		}
		updateCompileFlagsSection(compileFlags);

		// Write updated clangd back to file
		try (Writer writer = new FileWriter(file))
		{
			yaml.dump(data, writer);
		}
		catch (IOException e)
		{
			throw new IOException("Error writing .clangd file: " + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> createOrGetExistingYamlStructure(Object obj)
	{
		if (obj instanceof Map)
		{
			return (Map<String, Object>) obj;
		}
		return new LinkedHashMap<>();
	}

	private void updateCompileFlagsSection(Map<String, Object> compileFlags)
	{
		compileFlags.put("CompilationDatabase", "build"); //$NON-NLS-1$ //$NON-NLS-2$
		compileFlags.put("Remove", Arrays.asList("-m*", "-f*")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private File getClangdConfigFile(IProject project) throws IOException, CoreException
	{
		// Resolve the path of the clangd config file within the project directory
		Path clangdPath = project.getLocation().toPath().resolve(ILSPConstants.CLANGD_CONFIG_FILE);
		if (!Files.exists(clangdPath))
		{
			try
			{
				Files.createFile(clangdPath);
			}
			catch (IOException e)
			{
				throw new IOException("Failed to create clangd config file: " + e.getMessage(), e); //$NON-NLS-1$
			}
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}
		return clangdPath.toFile();
	}

}
