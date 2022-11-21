package com.espressif.idf.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.TelemetryViewer;
import com.espressif.idf.ui.telemetry.util.CsvDataParser;
import com.espressif.idf.ui.telemetry.util.DataParser;
import com.espressif.idf.ui.telemetry.util.RegexParser;

public class TelemetryDialog extends TitleAreaDialog
{

	private Text cacheLimitText;
	private Text graphNameText;
	private Text yAxisText;
	private Combo dataFormatCombo;
	private Label regexLbl;
	private Text regextText;
	private Label csvColumnNumberLbl;
	private Combo csvColumnNumberText;
	private Label csvTotalColumnsNumberlbl;
	private Text csvTotalColumnsNumberText;
	private Label csvSeparatorLbl;
	private Text csvSeparatorText;

	private static int graphCounter = 0;

	public TelemetryDialog(Shell parentShell)
	{
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	public void create()
	{
		super.create();
		setMessage("Provide options for a new telemetry graph");
		setTitle("Telemetry Graph Settings");
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);
		createContainer(area);
		return super.createDialogArea(parent);
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Telemetry Graph Settings");
	}

	private void createContainer(Composite area)
	{
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(6, false);
		container.setLayout(layout);

		Label cacheLimitLbl = new Label(container, SWT.NONE);
		cacheLimitLbl.setText("Cache limit:");
		cacheLimitText = new Text(container, SWT.BORDER);
		cacheLimitText.setText("100");

		Label graphNameLbl = new Label(container, SWT.NONE);
		graphNameLbl.setText("Graph name:");
		graphNameText = new Text(container, SWT.BORDER);
		graphNameText.setText("Line Chart");

		Label yAxisLbl = new Label(container, SWT.NONE);
		yAxisLbl.setText("Y axis name:");
		yAxisText = new Text(container, SWT.BORDER);
		yAxisText.setText("Amplitude");

		Label dataFormatLbl = new Label(container, SWT.NONE);
		dataFormatLbl.setText("Data format:");
		dataFormatCombo = new Combo(container, SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 5;
		dataFormatCombo.setLayoutData(gridData);
		String items[] = { "csv", "regex" }; //$NON-NLS-1$ //$NON-NLS-2$
		dataFormatCombo.setItems(items);
		dataFormatCombo.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (dataFormatCombo.getSelectionIndex() == 0)
				{
					disposeRegexWidgets();
					if (csvColumnNumberLbl == null || csvColumnNumberLbl.isDisposed())
					{
						csvTotalColumnsNumberlbl = new Label(container, SWT.NONE);
						csvTotalColumnsNumberlbl.setText("Total columns:");
						csvTotalColumnsNumberText = new Text(container, SWT.BORDER);
						csvTotalColumnsNumberText.setText("3");
						csvTotalColumnsNumberText.addModifyListener(new ModifyListener()
						{

							@Override
							public void modifyText(ModifyEvent e)
							{
								try
								{
									if (Integer.valueOf(csvTotalColumnsNumberText.getText()) > 0)
									{
										csvColumnNumberText
												.setItems(getColumnItems(csvTotalColumnsNumberText.getText()));
										csvColumnNumberText.select(0);
										csvColumnNumberText.update();
										return;
									}
								}
								catch (NumberFormatException exc)
								{
									Logger.log(exc);
								}

							}
						});
						csvTotalColumnsNumberText.addSelectionListener(new SelectionAdapter()
						{
							@Override
							public void widgetDefaultSelected(SelectionEvent e)
							{
								if (Integer.valueOf(csvColumnNumberText.getText()) > 0)
								{
									csvColumnNumberText.setItems(getColumnItems(csvTotalColumnsNumberText.getText()));
									csvColumnNumberText.select(0);
								}
							}
						});

						csvColumnNumberLbl = new Label(container, SWT.NONE);
						csvColumnNumberLbl.setText("Column number:");
						csvColumnNumberText = new Combo(container, SWT.READ_ONLY);
						csvColumnNumberText.setItems(getColumnItems(csvTotalColumnsNumberText.getText()));
						csvColumnNumberText.select(0);

						csvSeparatorLbl = new Label(container, SWT.NONE);
						csvSeparatorLbl.setText("Separator:");
						csvSeparatorText = new Text(container, SWT.BORDER);
						csvSeparatorText.setText(",");

					}

					getShell().layout(true, false);
					final Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
					getShell().setSize(newSize);
				}

				if (dataFormatCombo.getSelectionIndex() == 1)
				{
					disposeCsvWidgets();
					if (regexLbl == null || regexLbl.isDisposed())
					{
						regexLbl = new Label(container, SWT.NONE);
						regexLbl.setText("Regex:");
						regextText = new Text(container, SWT.BORDER);
						GridData gridData = new GridData();
						gridData.horizontalSpan = 5;
						regextText.setLayoutData(gridData);
					}
					getShell().layout(true, false);
					final Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
					getShell().setSize(newSize);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				// TODO Auto-generated method stub

			}
		});
	}

	private void disposeWidget(Control widget)
	{
		if (widget != null && !widget.isDisposed())
		{
			widget.dispose();
		}
	}

	private void disposeRegexWidgets()
	{
		disposeWidget(regexLbl);
		disposeWidget(regextText);
	}

	private void disposeCsvWidgets()
	{
		disposeWidget(csvColumnNumberLbl);
		disposeWidget(csvColumnNumberText);
		disposeWidget(csvSeparatorLbl);
		disposeWidget(csvSeparatorText);
		disposeWidget(csvTotalColumnsNumberlbl);
		disposeWidget(csvTotalColumnsNumberText);
	}

	private String[] getColumnItems(String columnQuantity)
	{
		int maxValue = Integer.parseInt(columnQuantity);
		String[] items = new String[maxValue];
		for (int i = 0; i < maxValue; i++)
		{
			items[i] = String.valueOf(i);
		}
		return items;
	}

	@Override
	protected void buttonPressed(int buttonId)
	{
		if (buttonId == IDialogConstants.OK_ID)
		{
			try
			{
				TelemetryViewer view = (TelemetryViewer) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView("com.espressif.idf.ui.telemetry", "graph" + graphCounter, 3); //$NON-NLS-1$ //$NON-NLS-2$
				graphCounter += 1;
				int cacheLimit = Integer.parseInt(cacheLimitText.getText());
				String graphName = graphNameText.getText();
				String yAxisName = yAxisText.getText();

				cacheLimit = cacheLimit == 0 ? 100 : cacheLimit;
				graphName = graphName.isBlank() ? "Line Chart" : graphName; //$NON-NLS-1$
				yAxisName = yAxisName.isBlank() ? "Amplitude" : yAxisName; //$NON-NLS-1$
				DataParser dataParser = dataFormatCombo.getSelectionIndex() == 0
						? new CsvDataParser(Integer.valueOf(csvColumnNumberText.getText()),
								Integer.valueOf(csvTotalColumnsNumberText.getText()), csvSeparatorText.getText())
						: new RegexParser(regextText.getText());
				view.createGraph(cacheLimit, graphName, yAxisName, dataParser);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
		super.buttonPressed(buttonId);
	}

}
