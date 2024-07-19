package com.espressif.idf.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.espressif.idf.core.ILSPConstants;

public class ClangFormatFileHandler
{

	public void update(IProject project) throws CoreException, IOException
	{
		File file = getClangFormatFile(project);

		// Load existing .clang-format file
		FileInputStream inputStream = new FileInputStream(file);
		final DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		Yaml yaml = new Yaml(options);
		Map<String, Object> obj = yaml.load(inputStream);

		// Create new YAML structure if file is empty
		Map<String, Object> data = createOrGetExistingYamlStructure(obj);
		data.put("BasedOnStyle", data.getOrDefault("BasedOnStyle", "LLVM")); //$NON-NLS-1$
		data.put("IndentWidth", data.getOrDefault("IndentWidth", 4));
		// Write updated clang-format back to file
		try (Writer writer = new FileWriter(file))
		{
			yaml.dump(data, writer);
		}
		catch (IOException e)
		{
			throw new IOException("Error writing .clang-format file: " + e.getMessage(), e); //$NON-NLS-1$
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

	private File getClangFormatFile(IProject project) throws IOException, CoreException
	{
		// Resolve the path of the clang-format config file within the project directory
		Path clangFormatPath = project.getLocation().toPath().resolve(ILSPConstants.CLANG_FORMAT_FILE);
		if (!Files.exists(clangFormatPath))
		{
			try
			{
				Files.createFile(clangFormatPath);
			}
			catch (IOException e)
			{
				throw new IOException("Failed to create .clang_format file: " + e.getMessage(), e); //$NON-NLS-1$
			}
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}
		return clangFormatPath.toFile();
	}

}
