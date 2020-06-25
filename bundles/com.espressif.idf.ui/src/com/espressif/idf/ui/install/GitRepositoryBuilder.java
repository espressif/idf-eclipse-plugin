/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.install;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jgit.api.Git;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class GitRepositoryBuilder
{

	private File repositoryDirectory;
	private String activeBranch;
	private String uri;
	private IProgressMonitor monitor;

	/**
	 * Location of the repository, or where the repository should be cloned.
	 *
	 * @param directory the directory
	 */
	public void repositoryDirectory(final File directory)
	{
		this.repositoryDirectory = directory;
	}

	/**
	 * Active branch to checkout and use.
	 *
	 * @param value the value
	 */
	public void activeBranch(final String value)
	{
		this.activeBranch = value;
	}

	public void repositoryClone() throws Exception
	{

		Collection<String> branchesToClone = new ArrayList<>();
		branchesToClone.add(getBranchPath(this.activeBranch));

		GitProgressMonitor gitProgressMonitor = new GitProgressMonitor(monitor);

		// @formatter:off
		Git git = Git.cloneRepository()
				  .setProgressMonitor(gitProgressMonitor)
				  .setCloneSubmodules(true)
				  .setURI(this.uri)
				  .setDirectory(this.repositoryDirectory)
				  .setBranchesToClone(branchesToClone)
				  .setBranch(getBranchPath(this.activeBranch))
				  .call();
		
		// @formatter:on
		Logger.log(String.format("git clone result: %s", git.toString()));

		// To release the lock on the file system resource
		git.getRepository().close();

		Logger.log(MessageFormat.format("ESP-IDF {0} cloning completed!", this.activeBranch)); //$NON-NLS-1$
	}

	private String getBranchPath(final String branchName)
	{
		return "refs/heads/" + branchName; //$NON-NLS-1$
	}

	public void repositoryURI(String uri)
	{
		this.uri = uri;
	}

	public void setProgressMonitor(IProgressMonitor monitor)
	{
		this.monitor = monitor;
	}
}
