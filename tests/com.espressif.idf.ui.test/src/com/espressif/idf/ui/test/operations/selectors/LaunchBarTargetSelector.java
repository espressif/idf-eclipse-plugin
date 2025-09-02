/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.test.operations.selectors;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;

import org.eclipse.launchbar.ui.controls.internal.CSelector;
import org.eclipse.launchbar.ui.controls.internal.LaunchBarWidgetIds;
import org.eclipse.launchbar.ui.controls.internal.TargetSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.SWTBotWidget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * Launchbar CDT helper class to select items from launch targets
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
@SWTBotWidget(clasz = CSelector.class, preferredName = "cselector")
public class LaunchBarTargetSelector extends AbstractSWTBotControl<CSelector>
{

	private static final int NUM_FOR_FILTER_POPUP = 7;

	public LaunchBarTargetSelector(TargetSelector targetSelector) throws WidgetNotFoundException
	{
		super(targetSelector);
	}

	public LaunchBarTargetSelector(SWTBot bot)
	{
		this(bot.widget(WidgetMatcherFactory.withTooltip("Launch Target: OK")));
	}

	public LaunchBarTargetSelector(SWTBot bot, boolean exec)
	{
		this(bot.widget(WidgetMatcherFactory.widgetOfType(TargetSelector.class)));
	}

	public SWTBot bot()
	{
		return new SWTBot(widget);
	}

	public void click(int x, int y)
	{
		notify(SWT.MouseEnter);
		notify(SWT.MouseMove);
		notify(SWT.Activate);
		notify(SWT.FocusIn);
		notify(SWT.MouseDown, createMouseEvent(x, y, 1, SWT.NONE, 1));
		notify(SWT.MouseUp, createMouseEvent(x, y, 1, SWT.BUTTON1, 1));
	}

    public void clickEdit()
	{
		bot().canvasWithId(LaunchBarWidgetIds.EDIT).click(); // $NON-NLS-1$
	}

	private void clickOnInternalWidget(int x, int y, Widget internalWidget)
	{
		notify(SWT.MouseDown, createMouseEvent(x, y, 1, SWT.NONE, 1), internalWidget);
		notify(SWT.MouseUp, createMouseEvent(x, y, 1, SWT.BUTTON1, 1), internalWidget);
	}

	@Override
	public LaunchBarTargetSelector click()
	{
		Point size = syncExec((Result<Point>) () -> widget.getSize());
		click(size.x / 2, size.y / 2);
		return this;
	}

	public LaunchBarTargetSelector select(String text)
	{
		click();
		Label itemToSelect = bot().shellWithId(LaunchBarWidgetIds.POPUP).bot().widget(withText(text));
		Point itemToSelectLocation = syncExec((Result<Point>) () -> itemToSelect.getLocation());
		clickOnInternalWidget(itemToSelectLocation.x, itemToSelectLocation.y, itemToSelect);
		return this;
	}

	public LaunchBarTargetSelector selectTarget(String text)
	{
		click();
		SWTBotShell swtBotShell = bot().shellWithId(LaunchBarWidgetIds.POPUP);
		ScrolledComposite scrolledComposite = swtBotShell.bot().widget(widgetOfType(ScrolledComposite.class));
		int numberOfItemsInScrolledComp = syncExec(
				() -> ((Composite) scrolledComposite.getChildren()[0]).getChildren().length);
		Label itemToSelect;

		// Set the text in the not visible text field
		// when the target list is too big, swtbot cannot select a target label, so we filter the list
		if (numberOfItemsInScrolledComp > NUM_FOR_FILTER_POPUP)
		{
			swtBotShell.bot().text().setText(text);
			itemToSelect = swtBotShell.bot().label(0).widget;
		}
		else
		{
			itemToSelect = swtBotShell.bot().widget(withText(text));
		}

		Point itemToSelectLocation = syncExec((Result<Point>) itemToSelect::getLocation);
		clickOnInternalWidget(itemToSelectLocation.x, itemToSelectLocation.y, itemToSelect);
		return this;
	}
}
