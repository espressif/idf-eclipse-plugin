/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.launch.strategies;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.launch.LaunchResult;
import com.espressif.idf.core.tools.launch.ProcessUtils;
import com.espressif.idf.core.tools.launch.ProcessWaiter;

/**
 * macOS EIM launcher strategy using AppleScript and execPath polling fallback
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public final class MacOsEimLauncherStrategy extends AbstractLoggingLauncherStrategy
{
	/**
	 * AppleScript tries to:
	 *  - open the app bundle
	 *  - poll System Events for a process with the same bundle id
	 *  - return unix id (PID)
	 *
	 * If PID is not returned (System Events permission, slow launch, etc),
	 * caller will fall back to execPath polling.
	 */
	private static final String MACOS_LAUNCH_AND_PID_APPLESCRIPT = """
			set appPath to system attribute "APP_PATH"
			set bundleId to system attribute "BUNDLE_ID"

			do shell script "open -a " & quoted form of appPath

			tell application "System Events"
				repeat 300 times
					set matches to (application processes whose bundle identifier is bundleId)
					if (count of matches) > 0 then
						return unix id of (item 1 of matches)
					end if
					delay 0.1
				end repeat
			end tell

			error "PID not found (app may not have launched)"
			"""; //$NON-NLS-1$

	public MacOsEimLauncherStrategy(Display display, MessageConsoleStream standardConsoleStream,
			MessageConsoleStream errorConsoleStream)
	{
		super(display, standardConsoleStream, errorConsoleStream);
	}

	@Override
	public LaunchResult launch(String eimPath) throws IOException
	{
		String appBundlePath = deriveAppBundlePath(eimPath);
		String execPath = deriveExecPath(eimPath, appBundlePath);
		String bundleId = readBundleId(appBundlePath);

		ProcessBuilder pb = new ProcessBuilder("osascript", "-"); //$NON-NLS-1$ //$NON-NLS-2$
		pb.redirectErrorStream(true);
		pb.environment().put("APP_PATH", appBundlePath); //$NON-NLS-1$
		pb.environment().put("BUNDLE_ID", bundleId); //$NON-NLS-1$

		Process p = pb.start();
		try (OutputStream stdin = p.getOutputStream())
		{
			stdin.write(MACOS_LAUNCH_AND_PID_APPLESCRIPT.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		}

		String out;
		int exit;
		try
		{
			exit = p.waitFor();
			out = ProcessUtils.readAll(p.getInputStream());
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while waiting for osascript", e); //$NON-NLS-1$
		}

		Logger.log("APP_PATH=" + appBundlePath); //$NON-NLS-1$
		Logger.log("BUNDLE_ID=" + bundleId); //$NON-NLS-1$
		Logger.log("Launcher output was:\n" + out); //$NON-NLS-1$

		// If osascript failed or returned no pid, we fall back to execPath polling.
		Long pid = ProcessUtils.parseFirstLongLine(out);
		if (exit == 0 && pid != null)
		{
			return LaunchResult.ofPid(pid.longValue(), execPath, out);
		}

		// Fallback (still "successfully launched" from user perspective):
		// - we already attempted "open -a"
		// - we can wait for closure using pgrep -f execPath
		return LaunchResult.ofNoPid(execPath,
				"osascript exit=" + exit + "\n" + out); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public IStatus waitForExit(LaunchResult launchResult, IProgressMonitor monitor)
	{
		if (launchResult.pid().isPresent())
		{
			return ProcessWaiter.waitForExitByPid(launchResult.pid(), monitor);
		}

		// Fallback wait (pgrep -f execPath)
		return ProcessWaiter.waitForExitByExecPath(launchResult.execPath(), monitor);
	}

	private String deriveAppBundlePath(String eimPath)
	{
		Path p = Paths.get(eimPath).toAbsolutePath().normalize();

		while (p != null)
		{
			String name = p.getFileName() != null ? p.getFileName().toString() : ""; //$NON-NLS-1$
			if (name.endsWith(".app")) //$NON-NLS-1$
			{
				return p.toString();
			}
			p = p.getParent();
		}

		throw new IllegalArgumentException("Cannot derive .app bundle path from: " + eimPath); //$NON-NLS-1$
	}

	private String deriveExecPath(String eimPath, String appBundlePath)
	{
		// If caller passed the internal binary path, keep it.
		if (eimPath != null && eimPath.contains(".app/Contents/MacOS/")) //$NON-NLS-1$
		{
			return eimPath;
		}

		// Otherwise derive default binary location for waiting.
		return Paths.get(appBundlePath, "Contents", "MacOS", "eim").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private String readBundleId(String appBundlePath) throws IOException
	{
		// Prefer PlistBuddy because it reads Info.plist directly (no LaunchServices dependency).
		String infoPlist = Paths.get(appBundlePath, "Contents", "Info.plist").toString(); //$NON-NLS-1$ //$NON-NLS-2$
		Process p = new ProcessBuilder("/usr/libexec/PlistBuddy", "-c", "Print :CFBundleIdentifier", infoPlist) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.redirectErrorStream(true).start();

		String out;
		int exit;
		try
		{
			exit = p.waitFor();
			out = ProcessUtils.readAll(p.getInputStream());
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while reading Info.plist", e); //$NON-NLS-1$
		}

		if (exit != 0 || out == null || out.isBlank())
		{
			throw new IOException("Failed to read CFBundleIdentifier. Output was:\n" + out); //$NON-NLS-1$
		}

		return out.trim();
	}
}
