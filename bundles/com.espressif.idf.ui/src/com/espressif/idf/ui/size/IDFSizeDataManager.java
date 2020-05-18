/*******************************************************************************
 * Copyright 2018-2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.UIPlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeDataManager
{

	private String DATA = "data"; // DRAM .data
	private String BSS = "bss"; // DRAM .bss
	private String IRAM = "iram";
	private String DIRAM = "diram";
	private String FLASH_TEXT = "flash_text";
	private String FLASH_RODATA = "flash_rodata";
	private String OTHER = "other";
	private String TOTAL = "total";

	@SuppressWarnings("unchecked")
	public List<IDFSizeData> getDataList(IFile mapFile) throws Exception
	{
		String pythonExecutablePath = preconditionsCheck();

		List<String> arguments = getCommandArgsArchives(pythonExecutablePath, mapFile);
		String detailsJsonOp = getOutput(mapFile, arguments);
		if (!StringUtil.isEmpty(detailsJsonOp))
		{
			JSONObject archivesJsonObj = getJSON(detailsJsonOp);
			if (archivesJsonObj != null)
			{
				JSONObject symbolJsonObj = getSymbolDetails(pythonExecutablePath, mapFile);
				return convertToViewerModel(archivesJsonObj, symbolJsonObj);
			}
		}
		return Collections.EMPTY_LIST;

	}

	public JSONObject getIDFSizeOverview(IFile mapFile) throws Exception
	{
		String pythonExecutablePath = preconditionsCheck();
		List<String> commandArgs = getCommandArgs(pythonExecutablePath, mapFile);
		String detailsJsonOp = getOutput(mapFile, commandArgs);
		if (!StringUtil.isEmpty(detailsJsonOp))
		{
			return getJSON(detailsJsonOp);
		}
		return null;
	}

	protected String preconditionsCheck() throws Exception
	{
		// Check IDF_PYTHON_ENV_PATH
		String pythonExecutablePath = IDFUtil.getIDFPythonEnvPath();
		if (StringUtil.isEmpty(pythonExecutablePath))
		{
			throw new Exception("IDF_PYTHON_ENV_PATH path is not found in the Eclispe CDT build environment variables"); //$NON-NLS-1$
		}
		return pythonExecutablePath;
	}

	@SuppressWarnings("unchecked")
	private List<IDFSizeData> convertToViewerModel(JSONObject archivesJsonObj, JSONObject symbolJsonObj)
	{

		List<IDFSizeData> arrayList = new ArrayList<>();
		Set<String> keySet = archivesJsonObj.keySet();
		for (String key : keySet)
		{
			JSONObject object = (JSONObject) archivesJsonObj.get(key);

			IDFSizeData record = new IDFSizeData(key, (long) object.get(DATA), (long) object.get(BSS),
					(long) object.get(DIRAM), (long) object.get(IRAM), (long) object.get(FLASH_TEXT),
					(long) object.get(FLASH_RODATA), (long) object.get(OTHER), (long) object.get(TOTAL));
			arrayList.add(record);

			// update children
			if (symbolJsonObj != null)
			{
				Set<String> symbolsKeySet = symbolJsonObj.keySet();
				for (String symbolsKey : symbolsKeySet)
				{
					if (symbolsKey.startsWith(key))
					{
						String symbolName = symbolsKey.substring(key.length() + 1); // libnet80211.a:ieee80211_output.o
						JSONObject symbolObj = (JSONObject) symbolJsonObj.get(symbolsKey);
						IDFSizeData symbolRecord = new IDFSizeData(symbolName, (long) symbolObj.get(DATA),
								(long) symbolObj.get(BSS), (long) symbolObj.get(DIRAM), (long) symbolObj.get(IRAM),
								(long) symbolObj.get(FLASH_TEXT), (long) symbolObj.get(FLASH_RODATA),
								(long) symbolObj.get(OTHER), (long) symbolObj.get(TOTAL));
						record.getChildren().add(symbolRecord);
					}

				}
			}
		}

		return arrayList;
	}

	private JSONObject getSymbolDetails(String pythonExecutablePath, IFile mapFile)
	{
		List<String> arguments = getCommandArgsSymbolDetails(pythonExecutablePath, mapFile);
		String symbolsJsonOp = getOutput(mapFile, arguments);
		if (!StringUtil.isEmpty(symbolsJsonOp))
		{
			return getJSON(symbolsJsonOp);
		}
		return null;
	}

	protected String getOutput(IFile mapFile, List<String> arguments)
	{
		try
		{
			IStatus status = runProcess(mapFile, arguments);
			String message = status.getMessage();
			Logger.log(message);
			if (status.isOK() && !StringUtil.isEmpty(message))
			{
				IDFConsole idfConsole = new IDFConsole();
				idfConsole.getConsoleStream().print(message);
				return status.getMessage();
			}
		}
		catch (Exception e)
		{
			Logger.log(e);
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", e.getMessage()); //$NON-NLS-1$
		}
		return null;
	}

	protected IStatus runProcess(IFile file, List<String> arguments) throws Exception
	{

		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			return processRunner.runInBackground(arguments, Path.ROOT, System.getenv());
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
			return new Status(Status.ERROR, UIPlugin.PLUGIN_ID, e1.getMessage());
		}
	}

	protected List<String> getCommandArgsArchives(String pythonExecutablenPath, IFile file)
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(pythonExecutablenPath);
		arguments.add(IDFUtil.getIDFSizeScriptFile().getAbsolutePath());
		arguments.add(file.getLocation().toOSString());
		arguments.add("--archives");
		arguments.add("--json");

		return arguments;
	}

	protected List<String> getCommandArgsSymbolDetails(String pythonExecutablenPath, IFile file)
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(pythonExecutablenPath);
		arguments.add(IDFUtil.getIDFSizeScriptFile().getAbsolutePath());
		arguments.add(file.getLocation().toOSString());
		arguments.add("--file");
		arguments.add("--json");

		return arguments;
	}

	protected List<String> getCommandArgs(String pythonExecutablenPath, IFile file)
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(pythonExecutablenPath);
		arguments.add(IDFUtil.getIDFSizeScriptFile().getAbsolutePath());
		arguments.add(file.getLocation().toOSString());
		arguments.add("--json");

		return arguments;
	}

	protected JSONObject getJSON(String jsonOutput)
	{
		JSONObject jsonObj = null;
		try
		{
			jsonObj = (JSONObject) new JSONParser().parse(jsonOutput);
			return jsonObj;

		}
		catch (ParseException e)
		{
			Logger.log(e);
		}
		return jsonObj;
	}

}
