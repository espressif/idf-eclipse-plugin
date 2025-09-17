/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.exceptions;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.Messages;

/**
 * Exception to be thrown when the EIM version found
 * does not match the expected version
 * @author Ali Azam Rana 
 * */
public class EimVersionMismatchException extends Exception
{
	private static final long serialVersionUID = 4471390598613711666L;
	private final String expectedVersion;
	private final String foundVersion;

	public EimVersionMismatchException(String expectedVersion, String foundVersion)
	{
		super(String.format(Messages.EimVersionMismatchExceptionMessage, expectedVersion, foundVersion));
		Logger.log(String.format("Invalid eim_idf.json version. Expected: %s, but found: %s.", expectedVersion, foundVersion)); //$NON-NLS-1$
		this.expectedVersion = expectedVersion;
		this.foundVersion = foundVersion;
	}

	public String getExpectedVersion()
	{
		return expectedVersion;
	}

	public String getFoundVersion()
	{
		return foundVersion;
	}
	
	public String msgTitle()
	{
		return Messages.EimVersionMismatchExceptionMessageTitle;
	}
}
