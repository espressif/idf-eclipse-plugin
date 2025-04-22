package com.espressif.idf.ui.tools;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.IToolsInstallationWizardConstants;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerEditor;

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
				
				try
				{
					File inputFile = new File(toolSetConfigFilePath());
					if (!inputFile.exists())
					{
						inputFile.createNewFile();
					}

					IFile iFile = ResourcesPlugin.getWorkspace().getRoot()
							.getFile(new Path(inputFile.getAbsolutePath()));
					IDE.openEditor(activeww.getActivePage(), new FileEditorInput(iFile), ESPIDFManagerEditor.EDITOR_ID);
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
			}
		});
	}

	private String toolSetConfigFilePath()
	{
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(path.toOSString());
		Logger.log("Workspace path: " + stringBuilder.toString());
		stringBuilder.append(File.separatorChar);
		stringBuilder.append(IToolsInstallationWizardConstants.TOOL_SET_CONFIG_FILE);
		Logger.log("Tool Set configuraion file Path: " + stringBuilder.toString());
		return stringBuilder.toString();
	}
}
