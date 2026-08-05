/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.telemetry.TelemetryPreferences;
import com.espressif.idf.core.telemetry.TelemetryService;

/**
 * Tells the user once per installation that anonymous usage statistics are reported, and offers to switch the
 * reporting off right away.
 *
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TelemetryNotice
{
	private static final String LEARN_MORE_HREF = "learnMore"; //$NON-NLS-1$
	private static final String DISABLE_HREF = "disable"; //$NON-NLS-1$
	private static final String DOCUMENTATION_URL = "https://docs.espressif.com/projects/espressif-ide/en/latest/telemetry.html"; //$NON-NLS-1$

	private static final long CLOSE_DELAY_MS = TimeUnit.SECONDS.toMillis(30);
	private static final int WIDTH_HINT = 320;

	private TelemetryNotice()
	{
	}

	/**
	 * Shows the notice when this installation never showed it and usage statistics are actually reported. Safe to call
	 * from any thread.
	 */
	public static void showIfNeeded()
	{
		if (TelemetryPreferences.isNoticeShown() || !TelemetryService.getInstance().isEnabled()
				|| !PlatformUI.isWorkbenchRunning())
		{
			return;
		}

		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display.isDisposed())
		{
			return;
		}
		TelemetryPreferences.setNoticeShown();
		display.asyncExec(() -> open(display));
	}

	private static void open(Display display)
	{
		if (display.isDisposed())
		{
			return;
		}
		NotificationPopup.forDisplay(display).title(Messages.TelemetryNotice_Title, true)
				.content(TelemetryNotice::createContent).delay(CLOSE_DELAY_MS).open();
	}

	private static Control createContent(Composite parent)
	{
		Link link = new Link(parent, SWT.WRAP);
		link.setText(Messages.TelemetryNotice_Message);
		GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, true);
		layoutData.widthHint = WIDTH_HINT;
		link.setLayoutData(layoutData);
		link.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				handleSelection(event.text, link);
			}
		});
		return link;
	}

	private static void handleSelection(String href, Link link)
	{
		if (DISABLE_HREF.equals(href))
		{
			TelemetryPreferences.setEnabled(false);
		}
		else if (LEARN_MORE_HREF.equals(href))
		{
			openDocumentation();
		}
		link.getShell().close();
	}

	private static void openDocumentation()
	{
		try
		{
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
					.openURL(URI.create(DOCUMENTATION_URL).toURL());
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}
}
