/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.dsf;

import java.util.stream.Stream;

public enum IdfHardwareWatchpoints
{

	ESP32(2), ESP32S2(2), ESP32S3(2), ESP32C2(2), ESP32C3(8), ESP32C6(4), ESP32H2(8);

	private int hardwareWatchpointNum;
	private static final int DEFAULT_WATCHPOINT_NUM = 2;

	public static int getWatchpointNumForTarget(String idfTarget)
	{
		String idfTargetUprCase = idfTarget.toUpperCase();
		boolean isValueExists = Stream.of(values()).anyMatch(x -> x.name().contentEquals(idfTargetUprCase));
		return isValueExists ? valueOf(idfTargetUprCase).hardwareWatchpointNum : DEFAULT_WATCHPOINT_NUM;
	}

	IdfHardwareWatchpoints(int num)
	{
		this.hardwareWatchpointNum = num;
	}

}
