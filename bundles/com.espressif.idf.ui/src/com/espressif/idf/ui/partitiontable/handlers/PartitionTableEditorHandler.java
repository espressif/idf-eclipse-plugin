package com.espressif.idf.ui.partitiontable.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.build.PartitionTableBean;
import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.partitiontable.dialog.PartitionTableEditorDialog;

public class PartitionTableEditorHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		PartitionTableEditorDialog dialog = new PartitionTableEditorDialog(Display.getDefault().getActiveShell());
		IProject selectedProject = EclipseUtil.getSelectedProjectInExplorer();
		SDKConfigJsonReader reader = new SDKConfigJsonReader(selectedProject);
		String csvName = reader.getValue("PARTITION_TABLE_CUSTOM_FILENAME"); //$NON-NLS-1$
		if (csvName == null || csvName.isBlank())
		{
			MessageDialog.openInformation(Display.getDefault().getActiveShell(),
					Messages.PartitionTableEditorHandler_InformationTitle,
					Messages.PartitionTableEditorHandler_InformationMsg);
			return null;
		}
		IFile csvFile = selectedProject.getFile(csvName);
		if (!csvFile.exists())
		{
			List<PartitionTableBean> emptyList = new ArrayList<>();
			PartitionTableBean.saveCsv(csvFile, emptyList);
		}

		dialog.create(csvFile);
		dialog.open();
		return null;
	}

}
