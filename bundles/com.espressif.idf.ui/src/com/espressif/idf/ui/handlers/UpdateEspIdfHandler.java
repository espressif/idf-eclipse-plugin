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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.install.GitProgressMonitor;

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

				Git git = null;
				try
				{
					git = Git.open(new File(IDFUtil.getIDFPath()));
					git.pull().setProgressMonitor(gitProgressMonitor).call();
					git.submoduleInit().call();
					git.submoduleUpdate().setProgressMonitor(gitProgressMonitor).call();

					return Status.OK_STATUS;
				}
				catch (
						IOException
						| GitAPIException e)
				{
					Logger.log(e);
					return Status.error(e.getLocalizedMessage());
				} finally
				{
					if (git != null)
					{
						git.close();
					}
				}
			}
		};

		job.setUser(true);
		job.schedule();
		openProgressView();
		return job;
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
}
