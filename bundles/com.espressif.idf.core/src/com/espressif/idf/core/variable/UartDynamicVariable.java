package com.espressif.idf.core.variable;

public enum UartDynamicVariable
{
	SERIAL_PORT("serial_port"), //$NON-NLS-1$
	FLASH_COMMAND("flash_command"); //$NON-NLS-1$

	private String value;

	UartDynamicVariable(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}
}
