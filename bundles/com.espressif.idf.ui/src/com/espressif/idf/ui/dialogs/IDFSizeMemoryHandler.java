package com.espressif.idf.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;

public class IDFSizeMemoryHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		// get the selected project
		IResource project = getSelectedProject(IPageLayout.ID_PROJECT_EXPLORER);
		if (project == null)
		{
			project = getSelectedResource((IEvaluationContext) event.getApplicationContext());
		}

		if (project == null)
		{
			return project;
		}

		// get the active server instance for the project
		if (project instanceof IProject)
		{
			try
			{
				IStatus status = runIdfSizeCommand((IProject) project);
				Logger.log(status.getMessage());

				if (status.isOK())
				{
					String message = status.getMessage();
					if (!StringUtil.isEmpty(message))
					{
						IDFConsole idfConsole = new IDFConsole();
						idfConsole.getConsoleStream().print(message);
					}
				}
			}
			catch (Exception e)
			{
				Logger.log(e);
			}
		}

		return null;
	}

	protected IStatus runIdfSizeCommand(IProject project) throws Exception
	{
		// Check IDF_PYTHON_ENV_PATH
		String pythonExecutablenPath = IDFUtil.getIDFPythonEnvPath();
		if (StringUtil.isEmpty(pythonExecutablenPath))
		{
			throw new Exception("IDF_PYTHON_ENV_PATH path is not found in the Eclispe CDT build environment variables");
		}

		// Check /project/build/projectname.map
		IPath mapPath = project.getLocation().append("build").append(project.getName() + ".map");
		if (!mapPath.toFile().exists())
		{
			String msg = NLS.bind("Couldn't find {0} map file in the project", mapPath.toOSString());
			throw new Exception(msg);
		}

		List<String> arguments = new ArrayList<String>();
		arguments.add(pythonExecutablenPath);
		arguments.add(IDFUtil.getIDFSizeScriptFile().getAbsolutePath());
		arguments.add(mapPath.toOSString());
		arguments.add("--json");

		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			return processRunner.runInBackground(arguments, Path.ROOT, System.getenv());
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return null;
	}
	
	protected IStatus runIdfSizeArchivesCommand(IProject project) throws Exception
	{
		// Check IDF_PYTHON_ENV_PATH
		String pythonExecutablenPath = IDFUtil.getIDFPythonEnvPath();
		if (StringUtil.isEmpty(pythonExecutablenPath))
		{
			throw new Exception("IDF_PYTHON_ENV_PATH path is not found in the Eclispe CDT build environment variables");
		}

		// Check /project/build/projectname.map
		IPath mapPath = project.getLocation().append("build").append(project.getName() + ".map");
		if (!mapPath.toFile().exists())
		{
			String msg = NLS.bind("Couldn't find {0} map file in the project", mapPath.toOSString());
			throw new Exception(msg);
		}

		List<String> arguments = new ArrayList<String>();
		arguments.add(pythonExecutablenPath);
		arguments.add(IDFUtil.getIDFSizeScriptFile().getAbsolutePath());
		arguments.add(mapPath.toOSString());
		arguments.add("--archives");
		arguments.add("--json");

		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			return processRunner.runInBackground(arguments, Path.ROOT, System.getenv());
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return null;
	}

	public static IResource getSelectedResource(IEvaluationContext evaluationContext)
	{
		if (evaluationContext == null)
		{
			return null;
		}

		Object variable = evaluationContext.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (variable instanceof IStructuredSelection)
		{
			Object selectedObject = ((IStructuredSelection) variable).getFirstElement();
			if (selectedObject instanceof IAdaptable)
			{
				IResource resource = ((IAdaptable) selectedObject).getAdapter(IResource.class);
				if (resource != null)
				{
					return resource;
				}
			}
		}

		return null;
	}

	private static IProject getSelectedProject(String viewID)
	{
		ISelectionService service = getActiveWorkbenchWindow().getSelectionService();
		IStructuredSelection structured = (IStructuredSelection) service.getSelection(viewID);
		if (structured instanceof IStructuredSelection)
		{
			Object selectedObject = structured.getFirstElement();
			if (selectedObject instanceof IAdaptable)
			{
				IResource resource = ((IAdaptable) selectedObject).getAdapter(IResource.class);
				if (resource != null)
				{
					return resource.getProject();
				}
			}
		}
		return null;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow()
	{
		try
		{
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		catch (IllegalStateException e)
		{
			// Workbench has not been created yet
			return null;
		}
	}
}
