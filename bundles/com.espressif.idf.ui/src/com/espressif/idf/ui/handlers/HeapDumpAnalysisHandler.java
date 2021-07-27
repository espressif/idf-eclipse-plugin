/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.FileUtil;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.tracing.TracingAnalysisEditor;
import com.espressif.idf.ui.update.AbstractToolsHandler;

/**
 * Handler class for handling the context menu action
 * @author Ali Azam Rana
 *
 */
public class HeapDumpAnalysisHandler extends AbstractToolsHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		// get the selected dumpFile
		IResource dumpFile = EclipseHandler.getSelectedResource((IEvaluationContext) event.getApplicationContext());
		IProject selectedProject = dumpFile.getProject();
		IFile elfSymbolsFile = selectedProject.getFolder("build").getFile(selectedProject.getName().concat(".elf"));
		
		
		activateIDFConsoleView();
		List<String> commands = new ArrayList<String>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFSysviewTraceScriptFile().getAbsolutePath());
		commands.add("-j");
		commands.add("-b");
		commands.add(elfSymbolsFile.getRawLocation().toOSString());
		commands.add("file://".concat(dumpFile.getRawLocation().toString()));
		console.println("Commands Prepared");
		for(String command : commands)
		{
			console.println(command);
		}
		Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
		Path pathToProject = new Path(selectedProject.getLocation().toString());
		String jsonOutput = runCommand(commands, pathToProject, envMap);
		FileUtil.writeFile(selectedProject, "build/dump.json", jsonOutput, false);
		console.print(jsonOutput);
		
		launchEditor(selectedProject.getFile("build/dump.json"));
		return null;
	}
	
	private void launchEditor(IFile jsonDumpFile)
	{
		FileEditorInput editorInput = new FileEditorInput(jsonDumpFile);
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
				try
				{
					IDE.openEditor(activeww.getActivePage(), editorInput, TracingAnalysisEditor.EDITOR_ID);
				}
				catch (PartInitException e)
				{
					Logger.log(e);
				}
			}
		});
	}

	private String runCommand(List<String> arguments, Path workDir, Map<String, String> env)
	{
		String exportCmdOp = ""; //$NON-NLS-1$
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments,
					workDir, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Status can't be null", null).toString();
			}

			// process export command output
			exportCmdOp = status.getMessage();
			Logger.log(exportCmdOp);
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return exportCmdOp;
	}

	@Override
	protected void execute()
	{
	}
}
