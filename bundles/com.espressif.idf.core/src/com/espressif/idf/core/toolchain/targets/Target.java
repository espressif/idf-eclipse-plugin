/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain.targets;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Known ESP-IDF targets. Add new ones here when they graduate from "preview".
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public enum Target implements TargetSpec
{
	ESP32("esp32", Architecture.XTENSA), //$NON-NLS-1$
	ESP32S2("esp32s2", Architecture.XTENSA), //$NON-NLS-1$
	ESP32S3("esp32s3", Architecture.XTENSA), //$NON-NLS-1$
	ESP32C2("esp32c2", Architecture.RISCV32), //$NON-NLS-1$
	ESP32C3("esp32c3", Architecture.RISCV32), //$NON-NLS-1$
	ESP32C6("esp32c6", Architecture.RISCV32), //$NON-NLS-1$
	ESP32H2("esp32h2", Architecture.RISCV32), //$NON-NLS-1$
	ESP32P4("esp32p4", Architecture.RISCV32);//$NON-NLS-1$

	private static final Map<String, Target> BY_NAME = Stream.of(values())
			.collect(Collectors.toUnmodifiableMap(Target::idfName, t -> t));

	private final String idfName;
	private final Architecture arch;

	Target(String idfName, Architecture arch)
	{
		this.idfName = idfName;
		this.arch = arch;
	}

	@Override
	public String idfName()
	{
		return idfName;
	}

	@Override
	public Architecture arch()
	{
		return arch;
	}

	/**
	 * @return enum constant if known, otherwise null.
	 */
	public static Target tryParse(String s)
	{
		if (s == null)
			return null;
		return BY_NAME.get(s.toLowerCase(Locale.ROOT));
	}
}
