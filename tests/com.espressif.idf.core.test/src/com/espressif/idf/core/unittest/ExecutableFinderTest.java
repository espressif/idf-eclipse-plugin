/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.ExecutableFinder;
import com.espressif.idf.core.SystemExecutableFinder;
import com.espressif.idf.core.SystemWrapper;

class ExecutableFinderTest
{

	@TempDir
	private static File foundableTempDir;

	private static File foundableExecutableFile;
	private static File foundableNonExecutableFile;

	private static SystemWrapper systemWrapper;
	private static TestableSystemExecutableFinderWindows windowsExecutableFinder;
	private static SystemWrapper emptyPathSystemWrapper;
	private static SystemWrapper emptyPathExtSystemWrapper;
	private static TestableSystemExecutableFinderUnix unixExecutableFinder;

	private static File unixFoundableExecutableFile;

	private static final String EXE_STRING = ".EXE"; //$NON-NLS-1$
	private static final String TXT_STRING = ".TXT"; //$NON-NLS-1$
	private static final String FOUNDABLE_EXE_FILE_STRING = "foundableExe"; //$NON-NLS-1$
	private static final String FOUNDABLE_TEXT_FILE_STRING = "foundableTxt"; //$NON-NLS-1$
	private static final String NON_FOUNDABLE_EXE_FILE_STRING = "non_foundableExe"; //$NON-NLS-1$
	private static final String NON_FOUNDABLE_TEXT_FILE_STRING = "non_foundableTxt"; //$NON-NLS-1$

	private static class TestableSystemExecutableFinderWindows extends SystemExecutableFinder
	{
		public TestableSystemExecutableFinderWindows(SystemWrapper systemWrapper)
		{
			super(systemWrapper);
		}

		@Override
		protected boolean isPlatformWindows()
		{
			return true;
		}
	}

	private static class TestableSystemExecutableFinderUnix extends SystemExecutableFinder
	{
		public TestableSystemExecutableFinderUnix(SystemWrapper systemWrapper)
		{
			super(systemWrapper);
		}

		@Override
		protected boolean isPlatformWindows()
		{
			return false;
		}
	}

	@BeforeAll
	public static void setUp() throws IOException
	{
		foundableExecutableFile = new File(foundableTempDir, FOUNDABLE_EXE_FILE_STRING + EXE_STRING);
		foundableExecutableFile.createNewFile();
		foundableExecutableFile.setExecutable(true);
		unixFoundableExecutableFile = new File(foundableTempDir, FOUNDABLE_EXE_FILE_STRING);
		unixFoundableExecutableFile.createNewFile();
		unixFoundableExecutableFile.setExecutable(true);

		foundableNonExecutableFile = new File(foundableTempDir, FOUNDABLE_TEXT_FILE_STRING + TXT_STRING);
		foundableNonExecutableFile.createNewFile();
		foundableNonExecutableFile.setExecutable(false);

		systemWrapper = new SystemWrapper()
		{
			@Override
			public String getPathEnv()
			{
				return foundableTempDir.toString();
			}

			@Override
			public String getEnvExecutables()
			{
				return EXE_STRING;
			}
		};

		emptyPathSystemWrapper = new SystemWrapper()
		{

			@Override
			public String getPathEnv()
			{
				return null;
			}

			@Override
			public String getEnvExecutables()
			{
				return EXE_STRING;
			}
		};

		emptyPathExtSystemWrapper = new SystemWrapper()
		{

			@Override
			public String getPathEnv()
			{

				return foundableTempDir.toString();
			}

			@Override
			public String getEnvExecutables()
			{
				return null;
			}
		};

		windowsExecutableFinder = new TestableSystemExecutableFinderWindows(systemWrapper);
		unixExecutableFinder = new TestableSystemExecutableFinderUnix(systemWrapper);
	}

	@Test
	void testWindowsFindReturnsExpectedResultOnFoundableExecutable()
	{
		IPath foundExecutable = windowsExecutableFinder.find(FOUNDABLE_EXE_FILE_STRING, true);
		assertEquals(foundableExecutableFile.toString(), foundExecutable.toOSString());
	}

	@Test
	void testWindowsFindReturnsNullOnNonFoundableExecutable()
	{
		IPath nonFoundableExecutable = windowsExecutableFinder.find(NON_FOUNDABLE_EXE_FILE_STRING, true);
		assertNull(nonFoundableExecutable);
	}

	@Test
	void testWindowsFindReturnsNullOnNull()
	{
		IPath nullPath = windowsExecutableFinder.find(null, true);
		assertNull(nullPath);
	}

	@Test
	void testWindowsFindReturnsNullWithEmpyPath()
	{
		ExecutableFinder executableFinder = new TestableSystemExecutableFinderWindows(emptyPathSystemWrapper);
		IPath foundNonExecutable = executableFinder.find(FOUNDABLE_TEXT_FILE_STRING, true);
		assertNull(foundNonExecutable);
	}

	@Test
	void testWindowsFindReturnsNullWithEmpyPathExt()
	{
		ExecutableFinder executableFinder = new TestableSystemExecutableFinderWindows(emptyPathExtSystemWrapper);
		IPath foundNonExecutable = executableFinder.find(FOUNDABLE_TEXT_FILE_STRING, true);
		assertNull(foundNonExecutable);
	}

	@Test
	void testWindowsFindReturnsNullOnFoundableNonExecutable()
	{
		IPath foundNonExecutable = windowsExecutableFinder.find(FOUNDABLE_TEXT_FILE_STRING, true);
		assertNull(foundNonExecutable);
	}

	@Test
	void testWindowsFindReturnsNullOnNonFoundableNonExecutable()
	{
		IPath nonFoudableNonExecutable = windowsExecutableFinder.find(NON_FOUNDABLE_TEXT_FILE_STRING, true);
		assertNull(nonFoudableNonExecutable);
	}

	@Test
	void testUnixFindReturnsExpectedResultOnFoundableExecutable()
	{

		IPath foundExecutable = unixExecutableFinder.find(FOUNDABLE_EXE_FILE_STRING, true);
		assertEquals(unixFoundableExecutableFile.toString(), foundExecutable.toOSString());
	}

	@Test
	void testUnixFindReturnsNullOnNonFoundableExecutable()
	{
		IPath nonFoundableExecutable = unixExecutableFinder.find(NON_FOUNDABLE_EXE_FILE_STRING, true);
		assertNull(nonFoundableExecutable);
	}

	@Test
	void testUnixFindReturnsNullOnNull()
	{
		IPath nullPath = unixExecutableFinder.find(null, true);
		assertNull(nullPath);
	}

	@Test
	void testUnixFindReturnsNullWithEmpyPath()
	{
		ExecutableFinder executableFinder = new TestableSystemExecutableFinderUnix(emptyPathSystemWrapper);
		IPath foundNonExecutable = executableFinder.find(FOUNDABLE_TEXT_FILE_STRING, true);
		assertNull(foundNonExecutable);
	}

	@Test
	void testUnixFindReturnsExpectedResultWithEmpyPathExt()
	{
		ExecutableFinder executableFinder = new TestableSystemExecutableFinderUnix(emptyPathExtSystemWrapper);
		IPath foundNonExecutable = executableFinder.find(FOUNDABLE_EXE_FILE_STRING, true);
		assertEquals(unixFoundableExecutableFile.toString(), foundNonExecutable.toOSString());
	}

	@Test
	void testUnixFindReturnsNullOnFoundableNonExecutable()
	{
		IPath foundNonExecutable = unixExecutableFinder.find(FOUNDABLE_TEXT_FILE_STRING, true);
		assertNull(foundNonExecutable);
	}

	@Test
	void testUnixFindReturnsNullOnNonFoundableNonExecutable()
	{
		IPath nonFoudableNonExecutable = unixExecutableFinder.find(NON_FOUNDABLE_TEXT_FILE_STRING, true);
		assertNull(nonFoudableNonExecutable);
	}

}
