/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;

public class NvsPartitionGenerator
{
	private NvsPartitionGenerator()
	{
	}

	public static String generateNvsPartititon(boolean isEncrypted, Optional<String> encryptionKeyPathOpt, String size,
			IFile nvsCsvFile)
	{
		String resultFileName = nvsCsvFile.getName().replace("csv", "bin"); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (IDFUtil.getIDFPath() != null && IDFUtil.getIDFPythonEnvPath() != null)
		{
			List<String> commands = new ArrayList<>();
			commands.add(IDFUtil.getIDFPythonEnvPath());
			commands.add(IDFUtil.getNvsGeneratorScriptPath());
			commands.add(isEncrypted ? "encrypt" : "generate"); //$NON-NLS-1$ //$NON-NLS-2$ );
			commands.add(nvsCsvFile.getName());
			commands.add(resultFileName);
			commands.add(size);
			if (isEncrypted)
				commands.add(encryptionKeyPathOpt.map(t -> "--inputKey " + t).orElse("--keygen")); //$NON-NLS-1$ //$NON-NLS-2$

			Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();
			return runCommand(commands, envMap, new Path(nvsCsvFile.getProject().getLocationURI().getPath()));
		}
		return StringUtil.EMPTY;
	}

	private static String runCommand(List<String> arguments, Map<String, String> env, Path path)
	{
		String exportCmdOp = StringUtil.EMPTY;
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments, path, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
				return exportCmdOp;
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
