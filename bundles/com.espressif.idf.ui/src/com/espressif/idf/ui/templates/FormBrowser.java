/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.templates;

import java.net.MalformedURLException;
import java.net.URL;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.logging.Logger;

public class FormBrowser
{
	FormToolkit toolkit;
	Composite container;
	Browser formText;
	String text;
	int style;

	public FormBrowser(int style)
	{
		this.style = style;
	}

	public void createControl(Composite parent)
	{
		toolkit = new FormToolkit(parent.getDisplay());
		int borderStyle = toolkit.getBorderStyle() == SWT.BORDER ? SWT.NULL : SWT.BORDER;
		container = new Composite(parent, borderStyle);
		FillLayout flayout = new FillLayout();
		flayout.marginWidth = 1;
		flayout.marginHeight = 1;
		container.setLayout(flayout);
		formText = new Browser(container, SWT.NONE);
		LocationListener locationListener = new LocationListener()
		{

			@Override
			public void changing(LocationEvent event)
			{
				try
				{
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(event.location));
					event.doit = false;
				}
				catch (
						PartInitException
						| MalformedURLException e)
				{
					// no need to report
				}
				formText.setText(text);
			}

			@Override
			public void changed(LocationEvent event)
			{

			}

		};
		formText.addLocationListener(locationListener);

		if (borderStyle == SWT.NULL)
		{
			formText.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
			toolkit.paintBordersFor(container);
		}
		FormText ftext = toolkit.createFormText(formText, false);
		formText.setBackground(toolkit.getColors().getBackground());
		formText.setForeground(toolkit.getColors().getForeground());
		ftext.marginWidth = 2;
		ftext.marginHeight = 2;
		ftext.setHyperlinkSettings(toolkit.getHyperlinkGroup());
		formText.addDisposeListener(e -> {
			if (toolkit != null)
			{
				toolkit.dispose();
				toolkit = null;
			}
		});
		if (text != null)
			formText.setText(text);
	}

	public Control getControl()
	{
		return container;
	}

	public void setText(String text)
	{
		if (formText != null)
			try
			{
				this.text = convertMarkdownToHtml(text);
				formText.setText(this.text);
			}
			catch (Exception e)
			{
				Status status = new Status(IStatus.ERROR, IDFCorePlugin.PLUGIN_ID,
						"Error parsing the template description", e); //$NON-NLS-1$
				IDFCorePlugin.getPlugin().getLog().log(status);
			}
	}

	private String convertMarkdownToHtml(String markdownText)
	{
		try
		{
			Parser parser = Parser.builder().build();
			Node document = parser.parse(markdownText);
			HtmlRenderer renderer = HtmlRenderer.builder().build();
			return renderer.render(document);
		}
		catch (Exception e)
		{
			Logger.log(e);
			return markdownText;
		}

	}
}
