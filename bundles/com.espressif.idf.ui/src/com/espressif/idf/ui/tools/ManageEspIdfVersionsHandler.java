package com.espressif.idf.ui.tools;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.vo.EimJson;
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
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
				IDFUtil.closeWelcomePage(activeww);

				EimJson eimJson = new EimJson();

				try
				{
					EimIdfConfiguratinParser eimIdfConfiguratinParser = new EimIdfConfiguratinParser();
					eimJson = eimIdfConfiguratinParser.getEimJson(true);
				}
				catch (IOException e)
				{
					Logger.log(e);
					// Proceed with an empty EimJson object and show the information to the user in the ESP-IDF Manager view
				}

				try
				{
					IDE.openEditor(activeww.getActivePage(), new EimEditorInput(eimJson),
							ESPIDFManagerEditor.EDITOR_ID, true);
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
			}
		});
	}
}
