package com.espressif.idf.core;

/**
 * Class to contain all the variables that can be resolved by the IDFDynamicVariableResolver.java
 * @author Ali Azam Rana
 *
 */
public enum IDFDynamicVariables
{
	/**
	 * idf.py resolver variable
	 */
	IDF_PY,
	
	/**
	 * idf path variable added here to avoid redundancy in case of change in name
	 */
	IDF_PATH,
	
	/**
	 * idf virtual python executable path variable based on the env
	 */
	IDF_PYTHON_ENV_PATH
}
