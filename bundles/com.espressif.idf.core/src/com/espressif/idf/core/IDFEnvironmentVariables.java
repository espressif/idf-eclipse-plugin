/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;

import com.espressif.idf.core.logging.Logger;
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
	public static String IDF_PATH = "IDF_PATH"; //$NON-NLS-1$
	
	public static String IDF_PYTHON_ENV_PATH = "IDF_PYTHON_ENV_PATH"; //$NON-NLS-1$
	
	public static String PATH = "PATH"; //$NON-NLS-1$
	
	public static String OPENOCD_SCRIPTS = "OPENOCD_SCRIPTS"; //$NON-NLS-1$
	
	/**
	 * @param variableName Environment variable Name
	 * @return IEnvironmentVariable
	 */
	public IEnvironmentVariable getEnv(String variableName)
	{
		IContributedEnvironment contributedEnvironment = getEnvironment();
		IEnvironmentVariable variable = contributedEnvironment.getVariable(variableName, null);
		return variable;
	}

	protected IContributedEnvironment getEnvironment()
	{
		IEnvironmentVariableManager buildEnvironmentManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contributedEnvironment = buildEnvironmentManager.getContributedEnvironment();
		return contributedEnvironment;
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
	
	@SuppressWarnings("restriction")
	public void addEnvVariable(String name, String value)
	{
		Logger.log(MessageFormat.format("Updating environment variables with key:{0} value:{1}", name, value)); //$NON-NLS-1$
		IContributedEnvironment contributedEnvironment = getEnvironment();
		contributedEnvironment.addVariable(name, value, IEnvironmentVariable.ENVVAR_REPLACE, null, null);
		
		//Without this environment variables won't be persisted
		EnvironmentVariableManager.fUserSupplier.storeWorkspaceEnvironment(true);
	}
	
	/**
	 * @return CDT build environment variables map
	 */
	public Map<String, String> getEnvMap()
	{
		IEnvironmentVariableManager buildEnvironmentManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable[] variables = buildEnvironmentManager.getVariables((ICConfigurationDescription) null,
				true);
		Map<String, String> envMap = new HashMap<>();
		if (variables != null)
		{
			for (IEnvironmentVariable iEnvironmentVariable : variables)
			{
				String key = iEnvironmentVariable.getName();
				String value = iEnvironmentVariable.getValue();
				envMap.put(key, value);
			}
		}

		return envMap;
	}

}
