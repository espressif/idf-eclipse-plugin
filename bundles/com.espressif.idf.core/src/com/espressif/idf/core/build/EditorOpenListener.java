/*******************************************************************************
 * Copyright 2023-2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import com.espressif.idf.core.logging.Logger;

/**
 * Editor opener listener to check for the files and help run the marker remover job
 * @see MarkerCleanupJob
 * @author Ali Azam Rana
 *
 */
public class EditorOpenListener implements IPartListener2
{
	@Override
	public void partOpened(IWorkbenchPartReference partRef)
	{
		try
		{
			if (partRef instanceof IEditorReference)
			{
				IEditorReference editorRef = (IEditorReference) partRef;
				if (editorRef.getEditorInput() instanceof IEditorInput)
				{
					IEditorInput fileInput = editorRef.getEditorInput();
					IFile file = fileInput.getAdapter(IFile.class);
					String filePath = file.getFullPath().toString();
					if (filePath.contains("/ide/esp_idf_components/")) //$NON-NLS-1$
					{
						// Start the job
						MarkerCleanupJob job = new MarkerCleanupJob(file);
						job.schedule(1000);
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}
}
