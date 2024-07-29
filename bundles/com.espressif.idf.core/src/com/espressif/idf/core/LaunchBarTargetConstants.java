/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

/**
 * This class defines constants used to access attributes in the launch bar target. These constants are used to uniquely
 * identify settings such as the board, flash voltage, serial port, and target within the launch configuration.
 */
public final class LaunchBarTargetConstants
{
	public static final String PREFIX = "com.espressif.idf.launch.serial.core"; //$NON-NLS-1$
	public static final String BOARD = PREFIX + ".board"; //$NON-NLS-1$
	public static final String FLASH_VOLTAGE = PREFIX + ".flash_voltage"; //$NON-NLS-1$
	public static final String SERIAL_PORT = PREFIX + ".serialPort"; //$NON-NLS-1$
	public static final String TARGET = PREFIX + ".idfTarget"; //$NON-NLS-1$

	private LaunchBarTargetConstants()
	{
	}
}
