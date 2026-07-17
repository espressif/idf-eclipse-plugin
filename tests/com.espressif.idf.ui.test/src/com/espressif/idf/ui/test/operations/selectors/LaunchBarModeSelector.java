/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.test.operations.selectors;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;

import org.eclipse.launchbar.ui.controls.internal.CSelector;
import org.eclipse.launchbar.ui.controls.internal.LaunchBarWidgetIds;
import org.eclipse.launchbar.ui.controls.internal.ModeSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.SWTBotWidget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;

/**
 * Helper to interact with the CDT Launch Bar mode selector (Run / Debug).
 *
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@SWTBotWidget(clasz = CSelector.class, preferredName = "cselector")
public class LaunchBarModeSelector extends AbstractSWTBotControl<CSelector>
{
	public LaunchBarModeSelector(ModeSelector modeSelector) throws WidgetNotFoundException
	{
		super(modeSelector);
	}

	public LaunchBarModeSelector(SWTBot bot)
	{
		this(bot.widget(WidgetMatcherFactory.withTooltip("Launch Mode")));
	}

	public LaunchBarModeSelector(SWTBot bot, boolean unused)
	{
		this(bot.widget(WidgetMatcherFactory.widgetOfType(ModeSelector.class)));
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

	@Override
	public LaunchBarModeSelector click()
	{
		Point size = syncExec((Result<Point>) () -> widget.getSize());
		click(size.x / 2, size.y / 2);
		return this;
	}

	private void clickOnInternalWidget(int x, int y, Widget internalWidget)
	{
		notify(SWT.MouseDown, createMouseEvent(x, y, 1, SWT.NONE, 1), internalWidget);
		notify(SWT.MouseUp, createMouseEvent(x, y, 1, SWT.BUTTON1, 1), internalWidget);
	}

	public LaunchBarModeSelector select(String text)
	{
		click();
		Label itemToSelect = bot().shellWithId(LaunchBarWidgetIds.POPUP).bot().widget(withText(text));
		Point itemToSelectLocation = syncExec((Result<Point>) () -> itemToSelect.getLocation());
		clickOnInternalWidget(itemToSelectLocation.x, itemToSelectLocation.y, itemToSelect);
		return this;
	}
}
