package com.espressif.idf.ui.handlers;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.LaunchBarListener;

public class RenameIdfProjectParticipant extends RenameParticipant
{

	private IProject project;

	class UpdateBuildFolderChange extends Change
	{

		private String oldBuildFolderPath;
		private String newBuildPath;
		private IProject newProject;
		private IFolder newBuildFolder;

		@Override
		public String getName()
		{
			return String.format(Messages.RenameIdfProjectParticipant_RenameBuildFolderPathChangeName,
					oldBuildFolderPath, newBuildPath);
		}

		@Override
		public RefactoringStatus isValid(IProgressMonitor pm)
		{
			return RefactoringStatus.create(Status.OK_STATUS);
		}

		@Override
		public void initializeValidationData(IProgressMonitor pm)
		{
			try
			{
				oldBuildFolderPath = IDFUtil.getBuildDir(project);
				String newProjectName = getArguments().getNewName();
				newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(newProjectName);
				newBuildFolder = newProject.getFolder(IDFConstants.BUILD_FOLDER);
				newBuildPath = newBuildFolder.getFullPath().toOSString();
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
		}

		@Override
		public Change perform(IProgressMonitor pm) throws CoreException
		{
			IDFUtil.setBuildDir(newProject, newBuildFolder.getLocation().toOSString());
			return null;
		}

		@Override
		public Object getModifiedElement()
		{
			return project;
		}
	}

	protected boolean initialize(Object element)
	{
		if (element instanceof IProject projectElement)
		{
			this.project = projectElement;
		}
		return true;
	}

	public String getName()
	{
		return null;
	}

	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException
	{
		return null;
	}

	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
	{
		// workaround to save active launch target when renaming the project
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		ILaunchTarget activeLaunchTarget = launchBarManager.getActiveLaunchTarget();
		LaunchBarListener.setIgnoreTargetChange(true);
		Display.getDefault().syncExec(() -> Display.getDefault().getActiveShell().addDisposeListener(disposeEvent -> {
			try
			{
				LaunchBarListener.setIgnoreTargetChange(false);
				launchBarManager.setActiveLaunchTarget(activeLaunchTarget);
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}

		}));

		return new UpdateBuildFolderChange();
	}

}
