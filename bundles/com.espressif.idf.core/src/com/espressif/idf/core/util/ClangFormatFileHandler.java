/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.ILSPConstants;

public class ClangFormatFileHandler
{
	private final IFile clangFormatFile;

	public ClangFormatFileHandler(IProject project) throws CoreException
	{
		this.clangFormatFile = project.getFile(ILSPConstants.CLANG_FORMAT_FILE);
	}

	/**
	 * Updates the .clang-format file. If the file does not exist, it is created and initialized with default settings.
	 *
	 * @throws IOException   if an I/O error occurs during file creation or writing
	 * @throws CoreException if an error occurs while refreshing the project
	 */
	public void update() throws IOException, CoreException
	{

		if (!shouldCreateClangFormat())
		{
			return;
		}
		try (final var source = getClass().getResourceAsStream(".clang-format-project");) //$NON-NLS-1$
		{
			clangFormatFile.create(source, true, new NullProgressMonitor());
		}
	}

	private boolean shouldCreateClangFormat()
	{
		return !clangFormatFile.exists() && Platform.getPreferencesService().getBoolean(IDFCorePlugin.PLUGIN_ID,
				IDFCorePreferenceConstants.AUTOMATE_CLANGD_FORMAT_FILE,
				IDFCorePreferenceConstants.AUTOMATE_CLANGD_FORMAT_FILE_DEFAULT, null);
	}
}
