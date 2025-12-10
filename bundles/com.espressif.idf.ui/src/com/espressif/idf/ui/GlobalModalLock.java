/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Display;

public class GlobalModalLock
{
	private static final Semaphore lock = new Semaphore(1);

	private GlobalModalLock()
	{
	}

	public static <T> void showModal(Supplier<T> dialogSupplier, Consumer<T> callback)
	{
		new Thread(() -> {
			try
			{
				lock.acquire();
				Display.getDefault().syncExec(() -> {
					try
					{
						T result = dialogSupplier.get();
						callback.accept(result);
					} finally
					{
						lock.release();
					}
				});
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}, "GlobalModalLock-DialogThread").start(); //$NON-NLS-1$
	}
}
