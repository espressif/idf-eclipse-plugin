package com.espressif.idf.ui.tools;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerEditor;
import com.espressif.idf.ui.tools.manager.EimEditorInput;

public class ManageEspIdfVersionsHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		launchEditor();
		return null;
	}

	private void launchEditor()
	{
		Display.getDefault().asyncExec(() -> {
			IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
			IDFUtil.closeWelcomePage(activeww);

			try
			{
				IDE.openEditor(activeww.getActivePage(), new EimEditorInput(), ESPIDFManagerEditor.EDITOR_ID, true);
			}
			catch (Exception e)
			{
				Logger.log(e);
			}
		});
	}
}
