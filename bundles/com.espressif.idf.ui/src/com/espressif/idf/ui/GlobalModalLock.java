/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.logging.Logger;

public class GlobalModalLock
{
	private static final Semaphore lock = new Semaphore(1);
	private static final Set<Object> queuedDialogs = ConcurrentHashMap.newKeySet();

	private GlobalModalLock()
	{
	}

	public static <T> void showModal(Supplier<T> dialogSupplier, Consumer<T> callback)
	{
		showModal(null, dialogSupplier, callback);
	}

	/**
	 * Shows a modal dialog, waiting until any dialog already in the queue has closed.
	 * <p>
	 * When {@code queueKey} is already queued or visible, the request is ignored so the same dialog is not stacked.
	 *
	 * @param queueKey
	 *            identity of this dialog; {@code null} always enqueues
	 * @param dialogSupplier
	 *            opens the dialog and returns its result
	 * @param callback
	 *            receives the dialog result on the UI thread
	 */
	public static <T> void showModal(Object queueKey, Supplier<T> dialogSupplier, Consumer<T> callback)
	{
		if (queueKey != null && !queuedDialogs.add(queueKey))
		{
			Logger.log("Skipping duplicate modal dialog: " + queueKey); //$NON-NLS-1$
			return;
		}

		new Thread(() -> {
			boolean acquired = false;
			try
			{
				lock.acquire();
				acquired = true;
				Display.getDefault().syncExec(() -> {
					T result = dialogSupplier.get();
					callback.accept(result);
				});
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			finally
			{
				if (acquired)
				{
					lock.release();
				}
				if (queueKey != null)
				{
					queuedDialogs.remove(queueKey);
				}
			}
		}, "GlobalModalLock-DialogThread").start(); //$NON-NLS-1$
	}
}
