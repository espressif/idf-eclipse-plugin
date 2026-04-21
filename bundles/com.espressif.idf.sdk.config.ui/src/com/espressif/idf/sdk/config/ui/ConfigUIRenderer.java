package com.espressif.idf.sdk.config.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONObject;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.sdk.config.core.IJsonServerConfig;
import com.espressif.idf.sdk.config.core.KConfigMenuItem;
import com.espressif.idf.ui.dialogs.HelpPopupDialog;

public class ConfigUIRenderer
{

	private static final String ICONS_INFO_OBJ_GIF = "icons/help.gif"; //$NON-NLS-1$
	private static final String ICONS_SDK_RESET_ACTION_PNG = "icons/reset.png"; //$NON-NLS-1$

	private final Composite parent;
	private final JSONObject valuesMap;
	private final JSONObject visibleMap;
	private final JSONObject modifiedMap;
	private final JSONObject rangesMap;
	private final ConfigActionHandler actionHandler;
	private final boolean isResetSupported;
	private HelpPopupDialog infoDialog;

	private record RenderContext(KConfigMenuItem item, String configKey, Object configValue, Object modifiedValue,
			boolean isVisible, String helpInfo)
	{
		public RenderContext(KConfigMenuItem item, JSONObject visibleMap, JSONObject valuesMap, JSONObject modifiedMap)
		{
			this(item, item.getId(), valuesMap.get(item.getId()), modifiedMap.get(item.getId()),
					Boolean.TRUE.equals(visibleMap.get(item.getId())), item.getHelp());
		}
	}

	public ConfigUIRenderer(Composite parent, JSONObject valuesMap, JSONObject visibleMap, JSONObject modifiedMap,
			JSONObject rangesMap, boolean isResetSupported, ConfigActionHandler actionHandler)
	{
		this.parent = parent;
		this.valuesMap = valuesMap;
		this.visibleMap = visibleMap;
		this.modifiedMap = modifiedMap;
		this.rangesMap = rangesMap;
		this.isResetSupported = isResetSupported;
		this.actionHandler = actionHandler;
	}

	public void renderMenuItems(KConfigMenuItem selectedElement)
	{
		if (selectedElement == null || selectedElement.getChildren() == null)
			return;

		for (KConfigMenuItem item : selectedElement.getChildren())
		{
			var ctx = new RenderContext(item, visibleMap, valuesMap, modifiedMap);
			String type = item.getType();

			if (!ctx.isVisible() && !type.equals(IJsonServerConfig.MENU_TYPE))
			{
				continue;
			}

			switch (type)
			{
			case IJsonServerConfig.STRING_TYPE -> renderString(ctx);
			case IJsonServerConfig.HEX_TYPE -> renderHex(ctx);
			case IJsonServerConfig.INT_TYPE -> renderInt(ctx);
			case IJsonServerConfig.BOOL_TYPE -> renderBool(ctx);
			case IJsonServerConfig.CHOICE_TYPE -> renderChoice(ctx);
			case IJsonServerConfig.MENU_TYPE -> renderMenuItems(item);
			default -> Logger.log("Unhandled config type: " + type);
			}

			if (!type.equals(IJsonServerConfig.MENU_TYPE) && !type.equals(IJsonServerConfig.CHOICE_TYPE))
			{
				addTooltipImage(ctx);
				addResetButton(ctx);
			}

			if (item.hasChildren() && !type.equals(IJsonServerConfig.MENU_TYPE)
					&& !type.equals(IJsonServerConfig.CHOICE_TYPE) && !item.isMenuConfig())
			{
				renderMenuItems(item);
			}
		}
	}

	private void renderString(RenderContext ctx)
	{
		var labelName = new Label(parent, SWT.NONE);
		labelName.setText(ctx.item().getTitle());

		var textControl = new Text(parent, SWT.SINGLE | SWT.BORDER);
		var gridData = new GridData();
		gridData.widthHint = 250;
		textControl.setLayoutData(gridData);
		textControl.setToolTipText(ctx.helpInfo());

		if (ctx.configValue() != null)
		{
			textControl
					.setText(ctx.modifiedValue() != null ? (String) ctx.modifiedValue() : (String) ctx.configValue());
		}

		textControl.addModifyListener(e -> {
			String text = textControl.getText();
			actionHandler.onTextModified(ctx.configKey(), text.trim());
		});
	}

	private void renderHex(RenderContext ctx)
	{
		var labelName = new Label(parent, SWT.NONE);
		labelName.setText(ctx.item().getTitle().concat(" (hex)"));

		var textControl = new Text(parent, SWT.SINGLE | SWT.BORDER);
		var gridData = new GridData();
		gridData.widthHint = 250;
		textControl.setLayoutData(gridData);
		textControl.setToolTipText(ctx.helpInfo());

		if (ctx.configValue() != null)
		{
			long valToFormat = ctx.modifiedValue() != null ? (long) ctx.modifiedValue() : (long) ctx.configValue();
			textControl.setText("0x" + Long.toHexString(valToFormat).toUpperCase());
		}

		textControl.addModifyListener(e -> {
			String text = textControl.getText().toLowerCase();
			if (text.startsWith("0x") && text.length() > 2)
			{
				try
				{
					long hexVal = Long.parseLong(text.substring(2), 16);
					actionHandler.onTextModified(ctx.configKey(), hexVal);
				}
				catch (NumberFormatException ignored)
				{
				}
			}
		});
	}

	private void renderInt(RenderContext ctx)
	{
		var labelName = new Label(parent, SWT.NONE);
		labelName.setText(ctx.item().getTitle());

		var textControl = new Text(parent, SWT.SINGLE | SWT.BORDER);
		var gridData = new GridData();
		gridData.widthHint = 250;
		textControl.setLayoutData(gridData);
		textControl.setToolTipText(ctx.helpInfo());

		if (ctx.configValue() != null)
		{
			textControl.setText(ctx.modifiedValue() != null ? String.valueOf(ctx.modifiedValue())
					: String.valueOf(ctx.configValue()));
		}

		textControl.addModifyListener(e -> actionHandler.onTextModified(ctx.configKey(), textControl.getText().trim()));
	}

	private void renderBool(RenderContext ctx)
	{
		var button = new Button(parent, SWT.CHECK);
		button.setText(ctx.item().getTitle());
		button.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, false, 2, 1));
		button.setToolTipText(ctx.helpInfo());

		if (ctx.configValue() != null)
		{
			button.setSelection((boolean) ctx.configValue());
		}

		button.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				var jsonObj = new JSONObject();
				jsonObj.put(ctx.configKey(), button.getSelection());
				actionHandler.onCommandExecuted(jsonObj);
			}
		});
	}

	private void renderChoice(RenderContext ctx)
	{
		var labelName = new Label(parent, SWT.NONE);
		labelName.setText(ctx.item().getTitle());

		var choiceCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		var gridData = new GridData();
		gridData.widthHint = 250;
		choiceCombo.setLayoutData(gridData);

		int index = 0;
		for (KConfigMenuItem child : ctx.item().getChildren())
		{
			String localConfigKey = child.getId();
			if (Boolean.TRUE.equals(visibleMap.get(localConfigKey)))
			{
				choiceCombo.add(child.getTitle());
				choiceCombo.setData(child.getTitle(), localConfigKey);

				if (Boolean.TRUE.equals(valuesMap.get(localConfigKey)))
				{
					choiceCombo.select(index);
				}
				index++;
			}
		}

		choiceCombo.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String key = (String) choiceCombo.getData(choiceCombo.getText());
				if (key != null)
				{
					var jsonObj = new JSONObject();
					jsonObj.put(key, true);
					actionHandler.onCommandExecuted(jsonObj);
				}
			}
		});

		addTooltipImage(ctx);
		addResetButton(ctx);
	}

	private void addTooltipImage(RenderContext ctx)
	{
		var labelName = new Label(parent, SWT.NONE);
		labelName.setImage(SDKConfigUIPlugin.getImage(ICONS_INFO_OBJ_GIF));
		labelName.setToolTipText("Help");

		labelName.addListener(SWT.MouseUp, event -> {
			String message = """
					%s

					%s
					""".formatted(ctx.item().getTitle(), ctx.helpInfo()); //$NON-NLS-1$

			Object range = rangesMap.get(ctx.configKey());
			if (range != null)
			{
				message += """

						Range Information:
						%s
						""".formatted(range.toString()); //$NON-NLS-1$
			}

			var activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if (infoDialog != null)
				infoDialog.close();

			infoDialog = new HelpPopupDialog(activeShell, "Help > " + ctx.configKey(), message);
			infoDialog.open();
		});
	}

	private void addResetButton(RenderContext ctx)
	{
		if (!isResetSupported)
		{
			new Label(parent, SWT.NONE).setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			return;
		}

		var resetIconComposite = new Composite(parent, SWT.NONE);
		resetIconComposite.setLayout(new FillLayout());
		resetIconComposite.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

		var resetIcon = new Label(resetIconComposite, SWT.NONE);
		resetIcon.setImage(SDKConfigUIPlugin.getImage(ICONS_SDK_RESET_ACTION_PNG));
		resetIcon.setToolTipText("Reset '" + ctx.item().getTitle() + "' to default value");

		resetIcon.addListener(SWT.MouseUp, event -> {
			if (ctx.configKey() != null)
			{
				actionHandler.onResetRequested(ctx.configKey());
			}
		});
	}

	public void renderFullMenu(KConfigMenuItem selectedElement)
	{
		addResetMenuButton(selectedElement);
		renderMenuItems(selectedElement);
	}

	private void addResetMenuButton(KConfigMenuItem selectedElement)
	{
		if (!isResetSupported)
		{
			return;
		}

		var resetGroupButton = new Button(parent, SWT.PUSH);
		resetGroupButton.setText("Reset Menu Defaults");
		resetGroupButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 4, 1));
		resetGroupButton.setToolTipText("Reset all settings in this menu");

		resetGroupButton.addSelectionListener(new SelectionAdapter()

		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				boolean confirm = MessageDialog.openConfirm(parent.getShell(), "Reset Menu Configurations",
						"This action will reset all configurations under '" + selectedElement.getTitle()
								+ "' to their default values. Continue?");

				if (confirm)
				{
					actionHandler.onMenuResetRequested(selectedElement);
				}
			}
		});
	}
}
