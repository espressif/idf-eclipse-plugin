/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

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

public class ClangFormatFileHandler {

    private final IProject project;
    private final Path clangFormatPath;

    public ClangFormatFileHandler(IProject project) throws CoreException {
        this.project = project;
        this.clangFormatPath = project.getLocation().toPath().resolve(ILSPConstants.CLANG_FORMAT_FILE);
    }

	/**
	 * Updates the .clang-format file. If the file does not exist, it is created and initialized with default settings.
	 *
	 * @throws IOException   if an I/O error occurs during file creation or writing
	 * @throws CoreException if an error occurs while refreshing the project
	 */
    public void update() throws IOException, CoreException {
		boolean isNewFile = createNewClangFormatFile();

        if (isNewFile) {
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("BasedOnStyle", "LLVM"); //$NON-NLS-1$ //$NON-NLS-2$
			data.put("IndentWidth", 4); //$NON-NLS-1$

            writeYamlFile(data);
        }
    }

    private DumperOptions createYamlOptions() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        return options;
    }

    private void writeYamlFile(Map<String, Object> data) throws IOException {
        try (Writer writer = new FileWriter(clangFormatPath.toFile())) {
            new Yaml(createYamlOptions()).dump(data, writer);
        }
    }

	/**
	 * Ensures that the .clang-format file exists. If the file does not exist, it is created and the project is
	 * refreshed.
	 *
	 * @return true if the file was created, false if it already existed
	 * @throws IOException   if an I/O error occurs during file creation
	 * @throws CoreException if an error occurs while refreshing the project
	 */
	private boolean createNewClangFormatFile() throws IOException, CoreException
	{
        if (Files.exists(clangFormatPath)) {
            return false;
        }

        try {
            Files.createFile(clangFormatPath);
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            return true;
        } catch (IOException e) {
            throw new IOException("Failed to create .clang_format file: " + e.getMessage(), e); //$NON-NLS-1$
        }
    }
}
