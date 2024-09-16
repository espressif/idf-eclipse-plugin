/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
@SuppressWarnings("restriction")
public class IDFEnvironmentVariables
{
	/**
	 * ESPRESSIF IDF_PATH environment variable identifier
	 * 
	 */
	public static final String IDF_PATH = "IDF_PATH"; //$NON-NLS-1$

	public static final String IDF_PYTHON_ENV_PATH = "IDF_PYTHON_ENV_PATH"; //$NON-NLS-1$

	public static final String PATH = "PATH"; //$NON-NLS-1$

	public static final String OPENOCD_SCRIPTS = "OPENOCD_SCRIPTS"; //$NON-NLS-1$

	public static final String IDF_COMPONENT_MANAGER = "IDF_COMPONENT_MANAGER"; //$NON-NLS-1$

	public static final String ESP_IDF_VERSION = "ESP_IDF_VERSION"; //$NON-NLS-1$

	public static final String GIT_PATH = "GIT_PATH"; //$NON-NLS-1$

	public static final String PYTHON_EXE_PATH = "PYTHON_EXE_PATH"; //$NON-NLS-1$

	public static final String IDF_MAINTAINER = "IDF_MAINTAINER"; //$NON-NLS-1$

	public static final String IDF_CCACHE_ENABLE = "IDF_CCACHE_ENABLE"; //$NON-NLS-1$

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

	public void removeEnvVariable(String variableName)
	{
		IContributedEnvironment contributedEnvironment = getEnvironment();
		contributedEnvironment.removeVariable(variableName, null);
	}

	public void removeAllEnvVariables()
	{
		Map<String, String> currentVarMap = getEnvMap();
		for (Entry<String, String> varEntry : currentVarMap.entrySet())
		{
			removeEnvVariable(varEntry.getKey());
		}
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

	public void addEnvVariable(String name, String value)
	{
		Logger.log(MessageFormat.format("Updating environment variables with key:{0} value:{1}", name, value)); //$NON-NLS-1$
		IContributedEnvironment contributedEnvironment = getEnvironment();
		contributedEnvironment.addVariable(name, value, IEnvironmentVariable.ENVVAR_REPLACE, null, null);

		// Without this environment variables won't be persisted
		EnvironmentVariableManager.fUserSupplier.storeWorkspaceEnvironment(true);
	}

	public void prependEnvVariableValue(String variableName, String value)
	{
		Logger.log(MessageFormat.format("Prepending environment variables with key:{0} to value:{1}", variableName, //$NON-NLS-1$
				value));
		EnvironmentVariableManager.fUserSupplier.createOverrideVariable(variableName, value,
				IBuildEnvironmentVariable.ENVVAR_PREPEND,
				EnvironmentVariableManager.getDefault().getDefaultDelimiter());
	}

	/**
	 * @return CDT build environment variables map
	 */
	public Map<String, String> getSystemEnvMap()
	{
		IEnvironmentVariableManager buildEnvironmentManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable[] variables = buildEnvironmentManager.getVariables((ICConfigurationDescription) null,
				true);
		Map<String, String> envMap = IDFUtil.getSystemEnv();
		if (variables != null)
		{
			for (IEnvironmentVariable iEnvironmentVariable : variables)
			{
				String key = iEnvironmentVariable.getName();
				if (key.equals(IDFCorePreferenceConstants.IDF_TOOLS_PATH))
				{
					continue;
				}
				String value = iEnvironmentVariable.getValue();
				envMap.put(key, value);
			}
		}

		return envMap;
	}

	/**
	 * @return CDT build environment variables map
	 */
	public Map<String, String> getEnvMap()
	{
		IEnvironmentVariableManager buildEnvironmentManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable[] vars = buildEnvironmentManager.getContributedEnvironment().getVariables(null);
		Map<String, String> envMap = new HashMap<>();
		if (vars != null)
		{
			for (IEnvironmentVariable iEnvironmentVariable : vars)
			{
				String key = iEnvironmentVariable.getName();
				String value = iEnvironmentVariable.getValue();
				envMap.put(key, value);
			}
		}

		return envMap;
	}
}
