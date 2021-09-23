/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

/**
 * Address decoder class calls the addr2line utility in toolchain path
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;

public class TracingCallerAddressDecoder
{
	private String elfFilePath;
	private IProject project;

	public TracingCallerAddressDecoder(String elfFilePath, IProject project)
	{
		this.elfFilePath = elfFilePath;
		this.project = project;
	}

	public Map<String, AddressInfoVO> decodeCallerAddresses(List<String> callerAddresses)
	{
		if (callerAddresses == null || callerAddresses.size() == 0)
		{
			return null;
		}
		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getXtensaToolchainExecutableAddr2LinePath(project));
		commands.add("-e");
		commands.add(elfFilePath);
		commands.addAll(callerAddresses);
		commands.add("-f");
		Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
		String decodedAddress = runCommand(commands, new Path(project.getLocation().toOSString()), envMap);
		return getListOfAddresses(decodedAddress, callerAddresses);
	}

	private Map<String, AddressInfoVO> getListOfAddresses(String decodedAddress, List<String> callerAddresses)
	{
		String[] addresses = decodedAddress.split(System.lineSeparator());
		Pattern removeAfterSpacePattern = Pattern.compile("^(\\S+)");
		Map<String, AddressInfoVO> callersMap = new HashMap<String, AddressInfoVO>();
		int callersIndex = 0;
		for (int i = 0; i < addresses.length; i += 2, callersIndex++)
		{
			if (callersMap.containsKey(callerAddresses.get(callersIndex)))
			{
				continue;
			}

			String functionName = addresses[i];
			int index = addresses[i + 1].indexOf(":", 2);
			String fullFilePath = addresses[i + 1].substring(0, index);
			IPath path = new Path(fullFilePath);
			IFile file = project.getFile(path);
			Matcher lineNumberMatcher = removeAfterSpacePattern
					.matcher(addresses[i + 1].substring(index + 1, addresses[i + 1].length()));
			int lineNumber = 1;
			if (lineNumberMatcher.find())
			{
				lineNumber = Integer.parseInt(lineNumberMatcher.group(0));
			}
			callersMap.put(callerAddresses.get(callersIndex),
					new AddressInfoVO(file, lineNumber, functionName, callerAddresses.get(callersIndex), fullFilePath));
		}

		return callersMap;
	}

	private String runCommand(List<String> arguments, Path workDir, Map<String, String> env)
	{
		String exportCmdOp = ""; //$NON-NLS-1$
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments, workDir, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Status can't be null", null).toString();
			}

			// process export command output
			exportCmdOp = status.getMessage();
			Logger.log(exportCmdOp);
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return exportCmdOp;
	}
}
