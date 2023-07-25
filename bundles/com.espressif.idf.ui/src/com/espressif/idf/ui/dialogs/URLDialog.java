/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.dialogs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.core.logging.Logger;

/**
 * URL Dialog class that can show multiple external web URLs in the text and 
 * make them click able and launches the default program to open them
 * @author Ali Azam Rana
 *
 */
public class URLDialog extends Dialog
{
	private static final String URL_REGEX = "\\b((http|https)://)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"; //$NON-NLS-1$

	private String text;
	private String title;
	
	public URLDialog(Shell parentShell, String title, String text)
	{
		super(parentShell);
		this.text = text;
		this.title = title;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite container = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
		container.setLayout(new GridLayout());
		Link link = new Link(container, SWT.NONE);
		link.setText(getTextForLinkControl());
		link.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
		link.addListener(SWT.Selection, e -> Program.launch(e.text));
		return container;
	}
	
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(title);
	}
	
	private String getTextForLinkControl()
	{
		StringBuilder stringBuilder = new StringBuilder();
		Pattern pattern = Pattern.compile(URL_REGEX);
		Matcher matcher = pattern.matcher(text);
		int startIndex = 0, endIndex = text.length();
		while (matcher.find())
		{
			String url = matcher.group();
			Logger.log("URL Found: " + url);
			endIndex = text.indexOf(url);
			stringBuilder.append(text.substring(startIndex, endIndex));
			stringBuilder.append("<a>"); //$NON-NLS-1$
			stringBuilder.append(url);
			stringBuilder.append("</a>"); //$NON-NLS-1$
			startIndex = endIndex + url.length();
			endIndex = text.length();
		}
		if (stringBuilder.toString().isEmpty())
		{
			stringBuilder.append(text);
		}
		return stringBuilder.toString();
	}
}
