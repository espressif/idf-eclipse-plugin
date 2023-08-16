package com.espressif.idf.ui.completion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.core.util.StringUtil;

public class NlsInformationControl extends AbstractInformationControl
{
	private Composite controlComposite;
	
	private StyledText styledText;
	
	private String intent;
	private String context;
	private String description;
	private String functionHeader;
	private String functionBody;

	private String input;

	public NlsInformationControl(Shell parentShell)
	{
		super(parentShell, false);
		create();
		
	}

	@Override
	public boolean hasContents()
	{
		return true;
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(true);
		// bug fixed for info area getting hidden when even clicking on it
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	protected void createContent(Composite parent)
	{
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 1;
		controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(gridLayout);
		
		styledText = new StyledText(controlComposite, SWT.READ_ONLY | SWT.WRAP);
		
		GridData gd_styledText = new GridData(SWT.FILL, SWT.TOP, true, false);
		styledText.setLayoutData(gd_styledText);
		Label label = new Label(controlComposite, SWT.NONE);
		styledText.setBackground(label.getBackground());
		
		
	}

	public void setInput(String input, String functionInfo)
	{
		this.input = input;
		setInternalTexts();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Intent: ");
		if (!StringUtil.isEmpty(intent))
		{
			stringBuilder.append(intent);
		}
		
		stringBuilder.append("\nContext: ");
		if (!StringUtil.isEmpty(context))
		{
			stringBuilder.append(context);
		}
		
		stringBuilder.append("\nDescription: ");
		if (!StringUtil.isEmpty(description))
		{
			stringBuilder.append(description);
		}
		
		functionHeader = functionInfo.split(System.lineSeparator())[0];
		functionBody = functionInfo;
		
		
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append(functionBody);
		
		styledText.setText(stringBuilder.toString());
		FontData[] fontData = styledText.getFont().getFontData();
        for (int i = 0; i < fontData.length; i++) {
            fontData[i].setStyle(SWT.BOLD);
        }
        Font boldFont = new Font(getShell().getDisplay(), fontData);
        
        StyleRange styleRange = new StyleRange();
        styleRange.start = 0;
		styleRange.length = "Intent:".length();
		styleRange.font = boldFont;
		styledText.setStyleRange(styleRange);
		
		StyleRange styleRange2 = new StyleRange();
		styleRange2.start = "Intent:".length() + intent.length() + 1;
		styleRange2.length = "Context:".length();
		styleRange2.font = boldFont;
		styledText.setStyleRange(styleRange2);
		
		StyleRange styleRange3 = new StyleRange();
		styleRange3.start = "Intent:".length() + intent.length() + "\nContext:".length() + context.length() + 2;
		styleRange3.length = "Description:".length();
		styleRange3.font = boldFont;
		styledText.setStyleRange(styleRange3);
	}
	
	
	public void setFunctionInfo(String funcInfo)
	{
		functionHeader = funcInfo.split(System.lineSeparator())[0];
		functionBody = funcInfo;
		
		styledText.append(System.lineSeparator());
		
		styledText.append(functionHeader);
	}

	private void setInternalTexts()
	{
		Pattern intentPattern = Pattern.compile("\"intent\":\\s*\"([^\"]+)\"");
		Pattern contextPattern = Pattern.compile("\"context\":\\s*\"([^\"]+)\"");
		Pattern descriptionPattern = Pattern.compile("\"description\":\\s*\"([^\"]+)\"");

		intent = extractValue(intentPattern, input);
		if (StringUtil.isEmpty(intent))
		{
			intentPattern = Pattern.compile("intent:\\s*\"([^\"]+)\"");
			intent = extractValue(intentPattern, input);
		}

		context = extractValue(contextPattern, input);
		if (StringUtil.isEmpty(context))
		{
			contextPattern = Pattern.compile("context:\\s*\"([^\"]+)\"");
			context = extractValue(contextPattern, input);
		}

		description = extractValue(descriptionPattern, input);
		if (StringUtil.isEmpty(description))
		{
			descriptionPattern = Pattern.compile("description:\\s*\"([^\"]+)\"");
			description = extractValue(descriptionPattern, input);
		}
	}

	private String extractValue(Pattern pattern, String input)
	{
		Matcher matcher = pattern.matcher(input);
		if (matcher.find())
		{
			return matcher.group(1);
		}
		return null;
	}

}
