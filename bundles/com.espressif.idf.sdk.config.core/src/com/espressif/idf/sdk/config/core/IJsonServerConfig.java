package com.espressif.idf.sdk.config.core;

public interface IJsonServerConfig
{

	// json output constants
	String VALUES = "values";
	String VISIBLE = "visible";
	String RANGES = "ranges";
	String VERSION = "version";

	// data types
	String HEX_TYPE = "hex";
	String STRING_TYPE = "string";
	String BOOL_TYPE = "bool";
	String INT_TYPE = "int";
	String CHOICE_TYPE = "choice";

	// Operations
	String SET = "set";
	String SAVE = "save";
	String LOAD = "load";
}
