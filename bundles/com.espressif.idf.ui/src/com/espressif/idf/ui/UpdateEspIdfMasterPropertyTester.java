/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jgit.api.Git;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

public class UpdateEspIdfMasterPropertyTester extends PropertyTester
{
	private final String MASTER_BRANCH = "master"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
	{
		// Check if the master version of ESP-IDF is being used. If false, do not show update command
		String branchName = StringUtil.EMPTY;
		try
		{
			Git git = Git.open(new File(IDFUtil.getIDFPath()));
			branchName = git.getRepository().getBranch();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		return branchName.contentEquals(MASTER_BRANCH);
	}
}
