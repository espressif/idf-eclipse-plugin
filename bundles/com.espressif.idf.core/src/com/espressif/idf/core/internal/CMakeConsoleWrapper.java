/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package com.espressif.idf.core.internal;

import java.util.Objects;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Martin Weber
 *
 */
public class CMakeConsoleWrapper implements IConsole {
	private final IConsole delegate;
	private final ConsoleOutputStream out;
	private final ConsoleOutputStream err;

	/**
	 * @param srcFolder
	 *          the source root of the project being built
	 * @param delegate
	 * 			the console to wrap
	 */
	public CMakeConsoleWrapper(IContainer srcFolder, IConsole delegate) throws CoreException {
		Objects.requireNonNull(srcFolder);
		this.delegate = Objects.requireNonNull(delegate);
		// NOTE: we need one parser for each stream, since the output streams are not synchronized
		// when the process is started via o.e.c.core.CommandLauncher, causing loss of
		// the internal parser state
		out = new CMakeErrorParser(srcFolder, delegate.getOutputStream());
		err = new CMakeErrorParser(srcFolder, delegate.getErrorStream());
	}

	@Override
	public void start(IProject project) {
		delegate.start(project);
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return delegate.getInfoStream();
	}

	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return out;
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return err;
	}
}
