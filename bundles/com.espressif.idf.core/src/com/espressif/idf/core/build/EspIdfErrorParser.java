package com.espressif.idf.core.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.IConsoleParser;

import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.PopupDialog;
import com.espressif.idf.core.util.HintsUtil;

public class EspIdfErrorParser implements IConsoleParser
{

	private List<ReHintPair> reHintsList = HintsUtil.getReHintsList(new File(HintsUtil.getHintsYmlPath()));
	private List<ReHintPair> allMatchesList = new ArrayList<>();
	private List<String> errorList = new ArrayList<>();

	public boolean processLine(String paramString)
	{
		for (ReHintPair reHintEntry : reHintsList)
		{
			boolean isRegexMatchesWithField = Pattern.compile(reHintEntry.getRe()).matcher(paramString).find();
			if (isRegexMatchesWithField)
			{
				allMatchesList.add(reHintEntry);
				errorList.add(paramString);
			}
		}
		if (allMatchesList.isEmpty())
		{
			for (ReHintPair reHintEntry : reHintsList)
			{
				if (reHintEntry.getRe().contains(paramString))
				{
					allMatchesList.add(reHintEntry);
					errorList.add(paramString);
				}
			}
		}
		return false;
	}

	public void shutdown()
	{
		StringBuilder comBuilder = new StringBuilder();
		for (int i = 0; i < errorList.size(); i++)
		{
			comBuilder.append(errorList.get(i) + "-" + reHintsList.get(i).getHint() + "\n");
		}
		OpenDialogListenerSupport.getSupport().firePropertyChange(PopupDialog.AVAILABLE_HINTS.name(),
				PopupDialog.AVAILABLE_HINTS.name(),
				comBuilder.toString());
		allMatchesList.clear();
	}


}
