/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.launch.serial.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.espressif.idf.core.IDFDynamicVariables;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.util.IDFUtil;

/**
 * Variable resolver for our added dynamic variables
 * @author Ali Azam Rana
 *
 */
public class IDFDynamicVariableResolver implements IDynamicVariableResolver {

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();

		IDFDynamicVariables idfVariable = IDFDynamicVariables.valueOf(variable.getName());

		switch (idfVariable) {
		case IDF_PATH:
			return idfEnvironmentVariables.getEnvValue(IDFDynamicVariables.IDF_PATH.name());

		case IDF_PYTHON_ENV_PATH:
			return IDFUtil.getIDFPythonEnvPath();

		case IDF_PY:
			return IDFUtil.getIDFPythonScriptFile().getAbsolutePath();

		default:
			return null;
		}
	}
}
