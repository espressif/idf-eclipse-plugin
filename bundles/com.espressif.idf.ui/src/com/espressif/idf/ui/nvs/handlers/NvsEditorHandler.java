package com.espressif.idf.ui.nvs.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.util.NvsTableDataService;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.nvs.dialog.NvsEditorDialog;

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
			new NvsTableDataService().saveCsv(csvFile, new ArrayList<>());
		}
		NvsEditorDialog dialog = new NvsEditorDialog(Display.getDefault().getActiveShell());
		dialog.setCsvFile(csvFile);
		dialog.open();
		return null;
	}

}
