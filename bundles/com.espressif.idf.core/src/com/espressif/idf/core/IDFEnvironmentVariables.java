/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;

import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFEnvironmentVariables
{
	/**
	 * ESPRESSIF IDF_PATH environment variable identifier
	 * 
	 */
	public static String IDF_PATH = "IDF_PATH";

	/**
	 * @param variableName Environment variable Name
	 * @return IEnvironmentVariable
	 */
	public IEnvironmentVariable getEnv(String variableName)
	{
		IEnvironmentVariableManager buildEnvironmentManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contributedEnvironment = buildEnvironmentManager.getContributedEnvironment();
		IEnvironmentVariable variable = contributedEnvironment.getVariable(variableName, null);
		return variable;
	}

	/**
	 * @param variableName Environment variable Name
	 * @return Environment value associated the given name
	 */
	public String getEnvValue(String variableName)
	{
		IEnvironmentVariable variable = getEnv(variableName);
		String envValue = StringUtil.EMPTY;
		if (variable != null)
		{
			envValue = variable.getValue();
		}

		return envValue;
	}

}
