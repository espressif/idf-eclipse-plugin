/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.espressif.idf.core.logging.Logger;

/**
 * Tracing utility functions
 * 
 * @author Ali Azam Rana
 *
 */
public class TracingUtil
{
	public static void launchEditor(String fullFilePath)
	{
		File fileToOpen = new File(fullFilePath);

		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (fileToOpen.exists() && fileToOpen.isFile())
				{
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

					try
					{
						IDE.openEditorOnFileStore(page, fileStore);
					}
					catch (PartInitException e)
					{
						Logger.log(e);
					}
				}
			}
		});
	}

	public static void goToLineNumber(int lineNumber)
	{
		ITextEditor editor = (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		try
		{
			int start = document.getLineOffset(--lineNumber);
			editor.selectAndReveal(start, 0);

			IWorkbenchPage page = editor.getSite().getPage();
			page.activate(editor);

		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

}
