/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ali Azam Rana - Added some extension required for IDF Plugin
 *******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd.dsf.process.monitors;

import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.debug.core.model.IBinaryStreamMonitor;
import org.eclipse.debug.core.model.IBinaryStreamsProxy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.internal.core.InputStreamMonitor;

import com.espressif.idf.debug.gdbjtag.openocd.dsf.process.StreamListener;

/**
 * This class is a derivation of original {@link org.eclipse.debug.internal.core.StreamsProxy}
 * The reason is we want one stream listener for all 
 * stream monitors as we are filtering out everything ourselves
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
public class StreamsProxy implements IBinaryStreamsProxy {
	/**
	 * The monitor for the output stream (connected to standard out of the process)
	 */
	private CustomOutputStreamMonitor fOutputMonitor;
	/**
	 * The monitor for the error stream (connected to standard error of the process)
	 */
	private CustomOutputStreamMonitor fErrorMonitor;
	/**
	 * The monitor for the input stream (connected to standard in of the process)
	 */
	private InputStreamMonitor fInputMonitor;
	/**
	 * Records the open/closed state of communications with
	 * the underlying streams.  Note: fClosed is initialized to
	 * <code>false</code> by default.
	 */
	private boolean fClosed;

	/**
	 * Creates a <code>StreamsProxy</code> on the streams of the given system
	 * process.
	 *
	 * @param process system process to create a streams proxy on
	 * @param charset the process's charset or <code>null</code> if default
	 * @param processLabel The name for the process label
	 */
	public StreamsProxy(IProcess iProcess, Process process, Charset charset, String processLabel, String outputFile, boolean append) {
		if (process == null) {
			return;
		}
		fOutputMonitor = new CustomOutputStreamMonitor(process.getInputStream(), charset);
		fErrorMonitor = new CustomOutputStreamMonitor(process.getErrorStream(), charset);
		// Our own addition to make sure that we utilize only one listener for all streams
		StreamListener streamListener = new StreamListener(iProcess, fErrorMonitor, fOutputMonitor, charset, outputFile, append);
		fOutputMonitor.addListener(streamListener);
		fErrorMonitor.addListener(streamListener);
		fInputMonitor = new InputStreamMonitor(process.getOutputStream(), charset);
		fOutputMonitor.startMonitoring(processLabel);
		fErrorMonitor.startMonitoring(processLabel);
		fInputMonitor.startMonitoring();
	}

	/**
	 * Causes the proxy to close all communications between it and the
	 * underlying streams after all remaining data in the streams is read.
	 */
	public void close() {
		if (!isClosed(true)) {
			fOutputMonitor.close();
			fErrorMonitor.close();
			fInputMonitor.close();
		}
	}

	/**
	 * Returns whether the proxy is currently closed.  This method
	 * synchronizes access to the <code>fClosed</code> flag.
	 *
	 * @param setClosed If <code>true</code> this method will also set the
	 * <code>fClosed</code> flag to true.  Otherwise, the <code>fClosed</code>
	 * flag is not modified.
	 * @return Returns whether the stream proxy was already closed.
	 */
	private synchronized boolean isClosed(boolean setClosed) {
		boolean closed = fClosed;
		if (setClosed) {
			fClosed = true;
		}
		return closed;
	}

	/**
	 * Causes the proxy to close all
	 * communications between it and the
	 * underlying streams immediately.
	 * Data remaining in the streams is lost.
	 */
	public void kill() {
		synchronized (this) {
			fClosed= true;
		}
		fOutputMonitor.kill();
		fErrorMonitor.kill();
		fInputMonitor.close();
	}

	@Override
	public IStreamMonitor getErrorStreamMonitor() {
		return fErrorMonitor;
	}

	@Override
	public IStreamMonitor getOutputStreamMonitor() {
		return fOutputMonitor;
	}

	@Override
	public void write(String input) throws IOException {
		if (!isClosed(false)) {
			fInputMonitor.write(input);
		} else {
			throw new IOException();
		}
	}

	@Override
	public void closeInputStream() throws IOException {
		if (!isClosed(false)) {
			fInputMonitor.closeInputStream();
		} else {
			throw new IOException();
		}

	}

	@Override
	public IBinaryStreamMonitor getBinaryErrorStreamMonitor() {
		return fErrorMonitor;
	}

	@Override
	public IBinaryStreamMonitor getBinaryOutputStreamMonitor() {
		return fOutputMonitor;
	}

	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		if (!isClosed(false)) {
			fInputMonitor.write(data, offset, length);
		} else {
			throw new IOException();
		}
	}
}
