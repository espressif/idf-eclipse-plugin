package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.dialogs.SbomCommandDialog;

public class SbomCommandHandler extends AbstractHandler
{

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		SbomCommandDialog flashDialog = new SbomCommandDialog(activeShell);
		flashDialog.create();
		flashDialog.open();
		return null;
	}

}
