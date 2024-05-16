/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;

import com.espressif.idf.core.logging.Logger;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

/**
 * Win32 native api class to use because of reparse tags. This is only meant for windows.
 * 
 * @author Ali Azam Rana
 *
 */
public class WinNativeFileTagOperations
{
	private static final int MICROSOFT_REPARSE_TAG_BIT = 0x80000000;

	@Structure.FieldOrder({ "dwFileAttributes", "ftCreationTime", "ftLastAccessTime", "ftLastWriteTime",
			"nFileSizeHigh", "nFileSizeLow", "dwReserved0", "dwReserved1", "cFileName", "cAlternateFileName" })
	public static class WIN32_FIND_DATA extends Structure
	{
		public WinDef.DWORD dwFileAttributes;
		public WinBase.FILETIME ftCreationTime;
		public WinBase.FILETIME ftLastAccessTime;
		public WinBase.FILETIME ftLastWriteTime;
		public WinDef.DWORD nFileSizeHigh;
		public WinDef.DWORD nFileSizeLow;
		public WinDef.DWORD dwReserved0;
		public WinDef.DWORD dwReserved1;
		public byte[] cFileName = new byte[WinDef.MAX_PATH];
		public byte[] cAlternateFileName = new byte[14];

		public WIN32_FIND_DATA()
		{
			super();
		}

		public WIN32_FIND_DATA(Pointer memory)
		{
			super(memory);
			read();
		}
	}

	public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32
	{
		Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS); //$NON-NLS-1$

		WinNT.HANDLE FindFirstFile(String lpFileName,
				com.espressif.idf.core.util.WinNativeFileTagOperations.WIN32_FIND_DATA findFileData);

		boolean FindClose(WinNT.HANDLE hFindFile);
	}

	public static int getReparseTag(String filePath)
	{
		WIN32_FIND_DATA findFileData = new WIN32_FIND_DATA();
		WinNT.HANDLE hFind = Kernel32.INSTANCE.FindFirstFile(filePath, findFileData);

		if (WinBase.INVALID_HANDLE_VALUE.equals(hFind))
		{
			Logger.log("FindFirstFile failed: " + Native.getLastError()); //$NON-NLS-1$
		}

		try
		{
			if ((findFileData.dwFileAttributes.intValue() & WinNT.FILE_ATTRIBUTE_REPARSE_POINT) != 0)
			{
				int reparseTag = findFileData.dwReserved0.intValue();
				Logger.log("The file has a reparse point. Reparse Tag: 0x" + Integer.toHexString(reparseTag)); //$NON-NLS-1$
				return reparseTag;
			}
			else
			{
				Logger.log("The file does not have a reparse point."); //$NON-NLS-1$
			}
		} finally
		{
			Kernel32.INSTANCE.FindClose(hFind);
		}
		return -1;
	}

	public static boolean isReparseTagMicrosoft(int tag)
	{
		return (tag & MICROSOFT_REPARSE_TAG_BIT) != 0;
	}

	public static boolean fileExists(File file)
	{
		String filePath = file.getAbsolutePath();
		WIN32_FIND_DATA findFileData = new WIN32_FIND_DATA();
		WinNT.HANDLE hFind = Kernel32.INSTANCE.FindFirstFile(filePath, findFileData);

		if (WinBase.INVALID_HANDLE_VALUE.equals(hFind))
		{
			// File does not exist
			return false;
		}
		else
		{
			// File exists
			Kernel32.INSTANCE.FindClose(hFind);
			return true;
		}
	}

	public static boolean isDirectory(String filePath)
	{
		WIN32_FIND_DATA findFileData = new WIN32_FIND_DATA();
		WinNT.HANDLE hFind = Kernel32.INSTANCE.FindFirstFile(filePath, findFileData);

		if (WinBase.INVALID_HANDLE_VALUE.equals(hFind))
		{
			Logger.log("FindFirstFile failed: " + Native.getLastError()); //$NON-NLS-1$
			return false;
		}

		try
		{
			return (findFileData.dwFileAttributes.intValue() & WinNT.FILE_ATTRIBUTE_DIRECTORY) != 0;
		} finally
		{
			Kernel32.INSTANCE.FindClose(hFind);
		}
	}

}
