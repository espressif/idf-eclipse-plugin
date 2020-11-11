/*******************************************************************************
 * Copyright (c) 2013-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.espressif.idf.core.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/** Parses the output of cmake and reports errors and warnings as problem markers.
 *
 * @author Martin Weber
 */
public class CMakeErrorParser extends ConsoleOutputStream {

	public static final String CMAKE_PROBLEM_MARKER_ID = Activator.getId() + ".cmakeproblem"; //$NON-NLS-1$

	/**
	 * Taken from cmMessenger.cxx#printMessagePreamble: <code>
	 *
	 * <pre>
	if (t == cmake::FATAL_ERROR) {
	msg << "CMake Error";
	} else if (t == cmake::INTERNAL_ERROR) {
	msg << "CMake Internal Error (please report a bug)";
	} else if (t == cmake::LOG) {
	msg << "CMake Debug Log";
	} else if (t == cmake::DEPRECATION_ERROR) {
	msg << "CMake Deprecation Error";
	} else if (t == cmake::DEPRECATION_WARNING) {
	msg << "CMake Deprecation Warning";
	} else if (t == cmake::AUTHOR_WARNING) {
	msg << "CMake Warning (dev)";
	} else if (t == cmake::AUTHOR_ERROR) {
	msg << "CMake Error (dev)";
	} else {
	msg << "CMake Warning";
	 * </pre>
	 *
	 * <code><br>
	 * NOTE: We currently not handle output emitted by the cmake MESSAGE() command since the msg format
	 * is unknown (or needs more investigation).
	 *
	 */
	// message start markers...
	private static final String START_DERROR = "CMake Deprecation Error"; //$NON-NLS-1$
	private static final String START_DWARNING = "CMake Deprecation Warning"; //$NON-NLS-1$
	private static final String START_ERROR = "CMake Error"; //$NON-NLS-1$
	private static final String START_ERROR_DEV = "CMake Error (dev)"; //$NON-NLS-1$
	private static final String START_IERROR = "CMake Internal Error (please report a bug)"; //$NON-NLS-1$
	private static final String START_LOG = "CMake Debug Log"; //$NON-NLS-1$
	private static final String START_WARNING = "CMake Warning"; //$NON-NLS-1$
	private static final String START_WARNING_DEV = "CMake Warning (dev)"; //$NON-NLS-1$
	private static final String START_STATUS = "--"; //$NON-NLS-1$
	/** to terminate on the output of 'message("message test")' */
	private static final String START_MSG_SIMPLE = "\\R\\R"; //$NON-NLS-1$
	/** Start of a new error message, also ending the previous message. */
	private static final Pattern PTN_MSG_START;

	/** Name of the named-capturing group that holds a file name. */
	private static final String GP_FILE = "File"; //$NON-NLS-1$
	/** Name of the named-capturing group that holds a line number. */
	private static final String GP_LINE = "Lineno"; //$NON-NLS-1$

	static {
		String ptn = "^" + String.join("|", START_DERROR, START_DWARNING, Pattern.quote(START_ERROR_DEV), START_ERROR, //$NON-NLS-1$ //$NON-NLS-2$
				Pattern.quote(START_IERROR), START_LOG, Pattern.quote(START_WARNING_DEV), START_WARNING, START_STATUS,
				START_MSG_SIMPLE);
		PTN_MSG_START = Pattern.compile(ptn);
	}

	////////////////////////////////////////////////////////////////////
	// the source root of the project being built
	private final IContainer srcPath;
	private final OutputStream os;

	private final StringBuilder buffer;

	/** <code>false</code> as long as the buffer contains leading junk in front of a start-of-message */
	private boolean somSeen;

	/**
	 * @param srcFolder
	 *          the source root of the project being built
	 * @param outputStream
	 *          the OutputStream to write to or {@code null}
	 */
	public CMakeErrorParser(IContainer srcFolder, OutputStream outputStream) {
		this.srcPath = Objects.requireNonNull(srcFolder);
		this.os = outputStream;
		buffer = new StringBuilder(512);
	}

	private void processBuffer(boolean isEOF) {
		Matcher matcher = PTN_MSG_START.matcher(""); //$NON-NLS-1$
		for (;;) {
			matcher.reset(buffer);
			if (matcher.find()) {
				// a new Start Of Message is present in the buffer
				if (!somSeen) {
					// this is the first Start Of Message seen in the output, discard leading junk
					buffer.delete(0, matcher.start());
					somSeen = true;
					return;
				}
				String classification = matcher.group();
				int start = matcher.end();
				// get start of next message
				if (matcher.find() || isEOF) {
					int end = isEOF ? buffer.length() : matcher.start();
					String fullMessage = buffer.substring(0, end);
					// System.err.println("-###" + fullMessage.trim() + "\n###-");
					String content = buffer.substring(start, end);
					// buffer contains a complete message
					processMessage(classification, content, fullMessage);
					buffer.delete(0, end);
				} else {
					break;
				}
			} else {
				// nothing found in buffer
				return;
			}
		}
	}

	/**
	 * @param classification
	 *          message classification string
	 * @param content
	 *          message content, which is parsed according to the classification
	 * @param fullMessage
	 *          the complete message, including the classification
	 */
	private void processMessage(String classification, String content, String fullMessage) {
		MarkerCreator creator;
		switch (classification) {
		case START_DERROR:
			creator = new McDeprError(srcPath);
			break;
		case START_DWARNING:
			creator = new McDeprWarning(srcPath);
			break;
		case START_ERROR:
			creator = new McError(srcPath);
			break;
		case START_ERROR_DEV:
			creator = new McErrorDev(srcPath);
			break;
		case START_IERROR:
			creator = new McInternalError(srcPath);
			break;
		case START_WARNING:
			creator = new McWarning(srcPath);
			break;
		case START_WARNING_DEV:
			creator = new McWarningDev(srcPath);
			break;
		default:
			return; // ignore message
		}

		try {
			creator.createMarker(fullMessage, content);
		} catch (CoreException e) {
			Activator.getPlugin().getLog()
					.log(new Status(IStatus.WARNING, Activator.getId(), "CMake output error parsing failed", e)); //$NON-NLS-1$
		}
	}

	@Override
	public void write(int c) throws IOException {
		if (os != null)
			os.write(c);
		buffer.append(new String(new byte[] { (byte) c }));
		processBuffer(false);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (os != null)
			os.write(b, off, len);
		buffer.append(new String(b, off, len));
		processBuffer(false);
	}

	@Override
	public void flush() throws IOException {
		if (os != null)
			os.flush();
	}

	@Override
	public void close() throws IOException {
		if (os != null)
			os.close();
		// process remaining bytes
		processBuffer(true);
	}

	////////////////////////////////////////////////////////////////////
	// inner classes
	////////////////////////////////////////////////////////////////////
	/**
	 * Marker creator base class. Extracts the source-file name and line-number of errors from the output stream.
	 *
	 * @author Martin Weber
	 */
	private static abstract class MarkerCreator {

		/** patterns used to extract file-name and line number information */
		private static final Pattern[] PTN_LOCATION;

		static {
			PTN_LOCATION = new Pattern[] {
					Pattern.compile("(?m)^ at (?<" + GP_FILE + ">.+):(?<" + GP_LINE + ">\\d+).*$"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Pattern.compile("(?s)^: Error in cmake code at.(?<" + GP_FILE + ">.+):(?<" + GP_LINE + ">\\d+).*$"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Pattern.compile("(?m)^ in (?<" + GP_FILE + ">.+):(?<" + GP_LINE + ">\\d+).*$"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Pattern.compile("(?m)^:\\s.+$"), }; //$NON-NLS-1$
		}

		// the source root of the project being built
		protected final IContainer srcPath;

		/**
		 * @param srcPath
		 *          the source root of the project being built
		 */
		public MarkerCreator(IContainer srcPath) {
			this.srcPath = srcPath;
		}

		/**
		 * Gets the message classification that this object handles.
		 */
		abstract String getClassification();

		/**
		 * @return the severity of the problem, see {@link IMarker} for acceptable severity values
		 */
		abstract int getSeverity();

		/**
		 * Creates the {@link IMarker marker object} that reflects the message.
		 *
		 * @param fullMessage
		 *          the complete message, including the classification
		 * @param content
		 *          the message, without the classification
		 * @throws CoreException
		 */
		public void createMarker(String fullMessage, String content) throws CoreException {
			for (Pattern ptn : PTN_LOCATION) {
				final Matcher matcher = ptn.matcher(content);
				if (matcher.find()) {
					// normally project source root relative but may be absolute FS path
					String filename = null;
					try {
						filename = matcher.group(GP_FILE);
					} catch (IllegalArgumentException expected) {
					}
					IMarker marker = createBasicMarker(filename, getSeverity(), fullMessage.trim());
					try {
						String lineno = matcher.group(GP_LINE);
						Integer lineNumber = Integer.parseInt(lineno);
						marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
					} catch (IllegalArgumentException expected) {
					}
					break;
				}
			}
		}

		/**
		 * Creates a basic problem marker which should be enhanced with more problem information (e.g. severity, file name,
		 * line number exact message).
		 *
		 * @param fileName
		 *          the file where the problem occurred, relative to the source-root or <code>null</code> to denote just the
		 *          current project being build
		 * @param severity
		 *          the severity of the problem, see {@link IMarker} for acceptable severity values
		 * @param fullMessage
		 *          the complete message, including the classification
		 * @throws CoreException
		 */
		protected final IMarker createBasicMarker(String fileName, int severity, String fullMessage)
				throws CoreException {
			IMarker marker;
			if (fileName == null) {
				marker = srcPath.createMarker(CMAKE_PROBLEM_MARKER_ID);
			} else {
				// NOTE normally, cmake reports the file name relative to source root.
				// BUT some messages give an absolute file-system path which is problematic when the build
				// runs in a docker container
				// So we do some heuristics here...
				IPath path = new Path(fileName);
				try {
					// normal case: file is rel. to source root
					marker = srcPath.getFile(path).createMarker(CMAKE_PROBLEM_MARKER_ID);
				} catch (CoreException ign) {
					// try abs. path
					IPath srcLocation = srcPath.getLocation();
					if (srcLocation.isPrefixOf(path)) {
						// can resolve the cmake file
						int segmentsToRemove = srcLocation.segmentCount();
						path = path.removeFirstSegments(segmentsToRemove);
						marker = srcPath.getFile(path).createMarker(CMAKE_PROBLEM_MARKER_ID);
					} else {
						// possibly a build in docker container. we would reach this if the source-dir path inside
						// the container is NOT the same as the one in the host/IDE.
						// for now, just add the markers to the source dir and lets users file issues:-)
						marker = srcPath.createMarker(CMAKE_PROBLEM_MARKER_ID);
						Activator.getPlugin().getLog().log(new Status(IStatus.INFO, Activator.getId(),
								String.format("=Could not map %s to a workspace resource. Did the build run in a container?", fileName)));
						// Extra case: IDE runs on Linux, build runs on Windows, or vice versa...
					}
				}
			}
			marker.setAttribute(IMarker.MESSAGE, fullMessage);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.LOCATION, CMakeErrorParser.class.getName());
			return marker;
		}
	} // MarkerCreator

	////////////////////////////////////////////////////////////////////
	private static class McDeprError extends MarkerCreator {
		public McDeprError(IContainer srcPath) {
			super(srcPath);
		}

		@Override
		String getClassification() {
			return START_DERROR;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_ERROR;
		}
	}

	private static class McDeprWarning extends MarkerCreator {
		public McDeprWarning(IContainer srcPath) {
			super(srcPath);
		}

		@Override
		String getClassification() {
			return START_DWARNING;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_WARNING;
		}
	}

	private static class McError extends MarkerCreator {
		public McError(IContainer srcPath) {
			super(srcPath);
		}

		@Override
		String getClassification() {
			return START_ERROR;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_ERROR;
		}
	}

	private static class McErrorDev extends MarkerCreator {
		public McErrorDev(IContainer srcPath) {
			super(srcPath);
		}

		@Override
		String getClassification() {
			return START_ERROR_DEV;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_ERROR;
		}
	}

	private static class McInternalError extends MarkerCreator {
		public McInternalError(IContainer srcPath) {
			super(srcPath);
		}

		@Override
		String getClassification() {
			return START_IERROR;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_ERROR;
		}
	}

	private static class McWarning extends MarkerCreator {
		public McWarning(IContainer srcPath) {
			super(srcPath);
		}

		@Override
		String getClassification() {
			return START_WARNING;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_WARNING;
		}
	}

	private static class McWarningDev extends MarkerCreator {
		public McWarningDev(IContainer srcPath) {
			super(srcPath);
		}

		@Override
		String getClassification() {
			return START_WARNING_DEV;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_WARNING;
		}
	}
}
