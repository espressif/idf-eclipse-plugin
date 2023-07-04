/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.SubmoduleInitCommand;
import org.eclipse.jgit.api.SubmoduleUpdateCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.install.GitProgressMonitor;
import com.espressif.idf.ui.update.InstallToolsHandler;

public class UpdateEspIdfHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		Job job = new Job(Messages.UpdateEspIdfCommand_JobMsg)
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				GitProgressMonitor gitProgressMonitor = new GitProgressMonitor(monitor);

				try (Git git = Git.open(new File(IDFUtil.getIDFPath())))
				{
					git.pull().setProgressMonitor(gitProgressMonitor).call();
					SubmoduleInitCommand initCommand = git.submoduleInit();
					SubmoduleUpdateCommand updateCommand = git.submoduleUpdate();
					addRecursivePaths(git.getRepository(), initCommand, updateCommand);
					initCommand.call();
					updateCommand.setFetch(true);
					updateCommand.setProgressMonitor(gitProgressMonitor).call();
					return Status.OK_STATUS;
				}
				catch (
						IOException
						| GitAPIException e)
				{
					Logger.log(e);
					return Status.error(e.getLocalizedMessage());
				}
			}

			private void addRecursivePaths(Repository repo, SubmoduleInitCommand initCommand,
					SubmoduleUpdateCommand updateCommand) throws IOException
			{
				if (repo == null)
					return;
				try (SubmoduleWalk generator = SubmoduleWalk.forIndex(repo))
				{
					while (generator.next())
					{
						// Add current submodule path
						initCommand.addPath(generator.getPath());
						updateCommand.addPath(generator.getPath());

						// Recursively add paths for each submodule repository
						addRecursivePaths(generator.getRepository(), initCommand, updateCommand);
					}
				}
				finally
				{
					repo.close();
				}
			}
		};


		MutexRule rule = new MutexRule();
		job.setUser(true);

		openProgressView();
		Job installToolsJob = new Job(Messages.UpdateEspIdfCommand_InstallToolsJobMsg)
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				suggestInstallTools();
				return Status.OK_STATUS;
			}

			private void suggestInstallTools()
			{
				Display.getDefault().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
								Messages.UpdateEspIdfCommand_InstallToolsJobMsg,
								Messages.UpdateEspIdfCommand_SuggestToOpenInstallToolsWizard);
						if (isYes)
						{
							InstallToolsHandler installToolsHandler = new InstallToolsHandler();
							try
							{
								installToolsHandler.setCommandId("com.espressif.idf.ui.command.install"); //$NON-NLS-1$
								installToolsHandler.execute(null);
							}
							catch (ExecutionException e)
							{
								Logger.log(e);
							}
						}
					}
				});

			}

		};
		job.setRule(rule);
		installToolsJob.setRule(rule);
		job.schedule();
		installToolsJob.schedule();
		return null;
	}

	private void openProgressView()
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$
		}
		catch (PartInitException e)
		{
			Logger.log(e);
		}

	}

	public class MutexRule implements ISchedulingRule
	{
		@Override
		public boolean isConflicting(ISchedulingRule rule)
		{
			return rule == this;
		}

		@Override
		public boolean contains(ISchedulingRule rule)
		{
			return rule == this;
		}
	}

}
