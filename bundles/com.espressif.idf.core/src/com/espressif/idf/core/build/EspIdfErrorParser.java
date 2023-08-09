/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.IConsoleParser;

import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.PopupDialog;
import com.espressif.idf.core.util.HintsUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * The EspIdfErrorParser class implements the IConsoleParser interface to parse console output for ESP-IDF errors and
 * extract relevant hints. It processes each line of the console output and checks against a list of regular expression
 * patterns to identify errors and their corresponding hints.
 *
 * This class maintains a list of regular expression hint pairs (ReHintPair) that are used to match and identify errors,
 * and associate them with appropriate hints. It accumulates matched pairs in the 'allMatchesList' for further
 * processing.
 *
 * The processLine method is responsible for processing each input line by iterating through the list of regular
 * expression hint pairs. It uses regular expressions to determine if the line matches any error pattern. If a match is
 * found, the corresponding hint is associated with the error message and added to the 'allMatchesList'.
 *
 * If no matches are found using regular expressions, the processLine method performs a substring-based check against
 * the error patterns to capture potential matches. If any matches are identified, the corresponding hints are again
 * associated with the errors and added to the 'allMatchesList'.
 *
 * The shutdown method is used to trigger the completion of parsing. It notifies listeners registred in the UI plugin
 * that the list of available hints has changed, providing the accumulated error hint pairs to any interested parties.
 * The 'allMatchesList' is then cleared to prepare for the next parsing session.
 *
 * Note: This class assumes the availability of HintsUtil and ReHintPair classes.
 *
 * @author Denys Almazov (denys.almazov@espressif.com)
 *
 */
public class EspIdfErrorParser implements IConsoleParser
{

	private List<ReHintPair> reHintsList = HintsUtil.getReHintsList(new File(HintsUtil.getHintsYmlPath()));
	private List<ReHintPair> allMatchesList = new ArrayList<>();

	public boolean processLine(String paramString)
	{
		for (ReHintPair reHintEntry : reHintsList)
		{
			boolean isRegexMatchesWithField = Pattern.compile(reHintEntry.getRe()).matcher(paramString).find();
			if (isRegexMatchesWithField)
			{
				allMatchesList.add(new ReHintPair(paramString, reHintEntry.getHint()));
			}
		}
		if (allMatchesList.isEmpty())
		{
			for (ReHintPair reHintEntry : reHintsList)
			{
				if (reHintEntry.getRe().contains(paramString))
				{
					allMatchesList.add(new ReHintPair(paramString, reHintEntry.getHint()));
				}
			}
		}
		return false;
	}

	public void shutdown()
	{
		OpenDialogListenerSupport.getSupport().firePropertyChange(PopupDialog.AVAILABLE_HINTS.name(),
					StringUtil.EMPTY, new ArrayList<>(allMatchesList));
		allMatchesList.clear();
	}
}
