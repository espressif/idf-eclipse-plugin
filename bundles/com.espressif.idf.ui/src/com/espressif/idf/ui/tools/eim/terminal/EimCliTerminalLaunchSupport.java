/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.eim.terminal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.widgets.Display;
import org.eclipse.terminal.view.core.ITerminalsConnectorConstants;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.tools.Messages;

/**
 * Opens EIM CLI in an Eclipse Terminal tab and runs a completion callback once when the process exits
 * (or if the terminal fails to open).
 *
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public final class EimCliTerminalLaunchSupport
{
	private static final AtomicBoolean completionFired = new AtomicBoolean();
	private static volatile Runnable pendingCompletion;

	private EimCliTerminalLaunchSupport()
	{
	}

	/**
	 * Arms the one-shot completion callback and opens a new terminal running EIM CLI.
	 *
	 * @param eimExecutablePath absolute path to the EIM executable
	 * @param onComplete runs on the SWT UI thread once after EIM exits or if the terminal cannot be opened
	 * @return future completing when the terminal tab has been opened (not when EIM exits)
	 */
	public static CompletableFuture<?> launch(String eimExecutablePath, Runnable onComplete)
	{
		if (StringUtil.isEmpty(eimExecutablePath) || onComplete == null)
		{
			Logger.log("EIM CLI terminal launch skipped: missing path or callback"); //$NON-NLS-1$
			if (onComplete != null)
			{
				Display.getDefault().asyncExec(onComplete);
			}
			return CompletableFuture.completedFuture(null);
		}

		completionFired.set(false);
		pendingCompletion = onComplete;

		Map<String, Object> properties = new HashMap<>();
		properties.put(EimCliTerminalLauncherDelegate.PROP_EIM_EXECUTABLE, eimExecutablePath);
		properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, EimCliTerminalLauncherDelegate.DELEGATE_ID);
		properties.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
				EimCliTerminalLauncherDelegate.CONNECTOR_ID);
		properties.put(ITerminalsConnectorConstants.PROP_TITLE, Messages.EimCliTerminalWizardTitle);
		properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		properties.put(ITerminalsConnectorConstants.PROP_ENCODING, java.nio.charset.StandardCharsets.UTF_8.name());

		CompletableFuture<?> opened = new EimCliTerminalLauncherDelegate().execute(properties);
		opened.whenComplete((v, ex) -> {
			if (ex != null)
			{
				if (ex instanceof Exception)
				{
					Logger.log("EIM CLI terminal failed to open: " + ex.getMessage(), (Exception) ex); //$NON-NLS-1$
				}
				else
				{
					Logger.log("EIM CLI terminal failed to open: " + ex); //$NON-NLS-1$
				}
				invokeCompletionIfArmed();
			}
		});
		return opened;
	}

	/**
	 * Invokes the armed completion callback at most once (from the EIM process wait thread or open failure).
	 */
	public static void invokeCompletionIfArmed()
	{
		Runnable r = pendingCompletion;
		if (r == null)
		{
			return;
		}
		if (!completionFired.compareAndSet(false, true))
		{
			return;
		}
		pendingCompletion = null;
		Display display = Display.getDefault();
		if (display == null)
		{
			r.run();
			return;
		}
		display.asyncExec(r);
	}
}
