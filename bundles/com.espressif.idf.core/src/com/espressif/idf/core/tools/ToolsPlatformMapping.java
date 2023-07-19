/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.util.Arrays;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.logging.Logger;

/**
 * Mapping /tools/tools.json os to Eclipse platform os & arch
 * 
 * @author kondal kolipaka
 *
 */
public enum ToolsPlatformMapping
{
	// @formatter:off
	
    WIN32("win32", Platform.OS_WIN32,Platform.ARCH_X86), //$NON-NLS-1$
    WIN64("win64", Platform.OS_WIN32, Platform.ARCH_X86_64), //$NON-NLS-1$
    MACOS("macos", Platform.OS_MACOSX, Platform.ARCH_X86_64), //$NON-NLS-1$
    MACOSARM64("macos-arm64", Platform.OS_MACOSX, Platform.ARCH_AARCH64), //$NON-NLS-1$
    LINUXAMD64("linux-amd64", Platform.OS_LINUX, Platform.ARCH_X86_64), //$NON-NLS-1$
    LINUXARM64("linux-arm64", Platform.OS_LINUX, "arm64"), //$NON-NLS-1$ //$NON-NLS-2$
    LINUXARMEL("linux-armel", Platform.OS_LINUX, "armel"), //$NON-NLS-1$ //$NON-NLS-2$
    LINUXARMHF("linux-armhf", Platform.OS_LINUX, "armhf"), //$NON-NLS-1$ //$NON-NLS-2$
    LINUXI686("linux-i686", Platform.OS_LINUX, "i686"); //$NON-NLS-1$ //$NON-NLS-2$
	
	// @formatter:on

	private final String toolsOS;
	private final String os;
	private final String arch;

	ToolsPlatformMapping(String toolsOS, String os, String arch)
	{
		this.toolsOS = toolsOS;
		this.os = os;
		this.arch = arch;
	}

	public String getToolsOS()
	{
		return toolsOS;
	}

	public String getOS()
	{
		return os;
	}

	public String getArch()
	{
		return arch;
	}

	public static boolean isSupported(String toolsOS)
	{
		final String os = Platform.getOS();
		final String arch = Platform.getOSArch();
		Logger.log("toolsOS:" + toolsOS + " os:" + os + " arch:" + arch, true);

		return Arrays.stream(ToolsPlatformMapping.values()).filter(
				entry -> entry.getToolsOS().equals(toolsOS) && entry.getArch().equals(arch) && entry.getOS().equals(os))
				.count() > 0;
	}

}
