/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import org.eclipse.core.expressions.PropertyTester;

import com.espressif.idf.core.util.IDFUtil;

public class UpdateEspIdfMasterPropertyTester extends PropertyTester
{

	private static boolean isMasterBranch;

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
	{
		// Check if the master version of ESP-IDF is being used. If false, do not show update command
		return isMasterBranch;
	}

	public static void setEspIdfMasterPropertyTester()
	{
		isMasterBranch = IDFUtil.getEspIdfVersion().contains("dev"); //$NON-NLS-1$
	}

}
