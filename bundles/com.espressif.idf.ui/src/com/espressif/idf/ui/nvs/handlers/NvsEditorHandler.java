package com.espressif.idf.ui.nvs.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.NvsTableDataService;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.nvs.dialog.NvsEditor;

public class NvsEditorHandler extends AbstractHandler
{
	private static final String NVS_CSV_NAME = "nvs.csv"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IProject selectedProject = EclipseUtil.getSelectedProjectInExplorer();

		IFile csvFile = selectedProject.getFile(NVS_CSV_NAME);
		if (!csvFile.exists())
		{
			// Ensure the file exists before opening the editor
			new NvsTableDataService().saveCsv(csvFile, new ArrayList<>());
		}

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null)
		{
			try
			{
				FileEditorInput input = new FileEditorInput(csvFile);
				page.openEditor(input, NvsEditor.ID);
			}
			catch (PartInitException e)
			{
				Logger.log(e);
			}
		}

		return null;
	}
}
