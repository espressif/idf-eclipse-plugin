/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.swt.messages.Messages;

public final class LaunchTabControls
{
	private LaunchTabControls()
	{
	}

	public static void applyEmptyDefaultHint(Text text)
	{
		if (text != null)
		{
			text.setMessage(Messages.keepEmptyForDefault);
		}
	}

	public static void applyEmptyDefaultHint(TextWithButton field)
	{
		if (field != null)
		{
			field.setMessage(Messages.keepEmptyForDefault);
		}
	}

	public static Button addRestoreDefaultsButton(Composite parent, Runnable restoreAction)
	{
		Button button = new Button(parent, SWT.PUSH);
		button.setText(Messages.styledTextRestoreDefaultsLinkMsg);
		button.setToolTipText(Messages.restoreDefaultsButtonToolTip);
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
		if (parent.getLayout() instanceof GridLayout gridLayout)
		{
			gd.horizontalSpan = Math.max(1, gridLayout.numColumns);
		}
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				restoreAction.run();
			}
		});
		return button;
	}

	public static Button addRestoreDefaultsButtonToTab(Control tabControl, Runnable restoreAction)
	{
		Composite parent = findButtonParent(tabControl);
		if (parent == null)
		{
			return null;
		}
		return addRestoreDefaultsButton(parent, restoreAction);
	}

	private static Composite findButtonParent(Control control)
	{
		if (control instanceof ScrolledComposite scrolled && scrolled.getContent() instanceof Composite content)
		{
			return content;
		}
		if (control instanceof Composite composite)
		{
			return composite;
		}
		return null;
	}
}
