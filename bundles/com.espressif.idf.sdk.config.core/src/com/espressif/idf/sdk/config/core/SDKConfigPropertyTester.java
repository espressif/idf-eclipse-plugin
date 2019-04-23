/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;

import com.espressif.idf.core.IDFConstants;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class SDKConfigPropertyTester extends PropertyTester
{

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
	{
		if (receiver instanceof IFile && ((IFile) receiver).getName().equals(IDFConstants.SDKCONFIG_FILE_NAME))
		{
			return true;
		}
		return false;
	}

}
