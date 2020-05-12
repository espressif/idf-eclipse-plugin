/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.help;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class LaunchEspIdfDocHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
		try
		{
			IWebBrowser browser = support.getExternalBrowser();
			if (browser != null)
			{
				URL docsUrl = getDocsUrl(getIDFVersion());
				Logger.log(docsUrl.getPath());
				browser.openURL(docsUrl);
			}
		}
		catch (
				PartInitException
				| IOException e)
		{
			Logger.log(e);
		}
		return null;
	}

	protected String getIDFVersion()
	{
		String LATEST = "latest";//$NON-NLS-1$

		String idfPath = IDFUtil.getIDFPath();
		if (!StringUtil.isEmpty(idfPath))
		{
			String branch = gitBranchName(idfPath);
			if (branch != null)
			{
				if (branch.equals("master")) //$NON-NLS-1$
				{
					return LATEST;
				}
				else if (branch.contains("release")) //$NON-NLS-1$
				{
					return branch.replace("/", "-"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

		}

		return LATEST;
	}

	protected String gitBranchName(String gitPath)
	{
		try
		{
			Git git = init(gitPath);
			if (git != null)
			{
				return git.getRepository().getBranch();
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		return null;
	}

	protected Git init(String gitPath) throws IOException
	{
		File gitFolder = new File(gitPath);
		File gitDB = new File(gitFolder, ".git"); //$NON-NLS-1$
		if (gitDB.exists() && gitDB.isDirectory())
		{
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			Repository repository = builder.setGitDir(gitDB).readEnvironment().findGitDir().build();
			return new Git(repository);
		}
		return null;
	}

	protected URL getDocsUrl(String versionArg) throws MalformedURLException
	{
		return new URL("https://docs.espressif.com/projects/esp-idf/en/" + versionArg + "/index.html"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
