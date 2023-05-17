package com.espressif.idf.core;

/**
 * Class to contain all the variables that can be resolved by the IDFDynamicVariableResolver.java
 * @author Ali Azam Rana
 *
 */
public interface IDFDynamicVariables
{
	/**
	 * idf.py resolver variable
	 */
	String IDF_PY = "IDF_PY"; //$NON-NLS-1$
	
	/**
	 * idf path variable added here to avoid redundancy in case of change in name
	 */
	String IDF_PATH = IDFEnvironmentVariables.IDF_PATH;
	
	/**
	 * idf virtual python executable path variable based on the env
	 */
	String IDF_PYTHON_ENV_PATH = IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH;
}
