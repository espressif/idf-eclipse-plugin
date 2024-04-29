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

/**
 * A condition meant to be tested periodically. If it fails after a certain
 * timeout, a failure message is provided.
 */
public interface IWaitCondition {
    /**
     * Tests if the condition has been met.
     *
     * @return <code>true</code> if the condition is satisfied, <code>false</code> otherwise.
     * @throws Exception if the test encounters an error while processing the check.
     */
    boolean test() throws Exception;

    /**
     * Gets the failure message when a test fails (returns <code>false</code>).
     *
     * @return the failure message to show in case the test fails.
     */
    String getFailureMessage();
}