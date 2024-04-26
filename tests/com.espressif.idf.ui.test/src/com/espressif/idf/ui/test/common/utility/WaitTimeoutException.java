/*******************************************************************************
 * Copyright (c) 2008, 2016 Ketan Padegaonkar and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ketan Padegaonkar - initial API and implementation
 *     Marc-Andre Laperle - Adapted to Trace Compass from SWTBot's TimeoutException
 *******************************************************************************/
package com.espressif.idf.ui.test.common.utility;

public class WaitTimeoutException extends RuntimeException
{

	private static final long serialVersionUID = -2673174817824776871L;

	/**
	 * Constructs the exception with the given message.
	 *
	 * @param message the message.
	 */
	public WaitTimeoutException(String message)
	{
		super(message);
	}
}