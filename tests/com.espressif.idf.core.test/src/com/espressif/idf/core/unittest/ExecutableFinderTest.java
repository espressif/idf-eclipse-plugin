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
	private static ExecutableFinder executableFinder;

	private static final String FOUNDABLE_EXE_FILE_STRING = "foundable.exe"; //$NON-NLS-1$
	private static final String FOUNDABLE_TEXT_FILE_STRING = "foundable.txt"; //$NON-NLS-1$
	private static final String NON_FOUNDABLE_EXE_FILE_STRING = "non_foundable.exe"; //$NON-NLS-1$
	private static final String NON_FOUNDABLE_TEXT_FILE_STRING = "non_foundable.txt"; //$NON-NLS-1$
	private static final String PATHEXT = "PATHEXT"; //$NON-NLS-1$

	@BeforeAll
	public static void setUp() throws IOException
	{
		foundableExecutableFile = new File(foundableTempDir, FOUNDABLE_EXE_FILE_STRING);
		foundableExecutableFile.createNewFile();
		foundableExecutableFile.setExecutable(true);

		foundableNonExecutableFile = new File(foundableTempDir, FOUNDABLE_TEXT_FILE_STRING);
		foundableNonExecutableFile.createNewFile();

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
				return System.getenv(PATHEXT);
			}
		};

		executableFinder = new SystemExecutableFinder(systemWrapper);
	}

	@Test
	void testFindReturnsExpectedResultonFoundableExecutable()
	{	

		String foundExecutable = executableFinder.find(FOUNDABLE_EXE_FILE_STRING, true)
				.toString();
		assertEquals(foundExecutable, foundExecutable.toString());
	}

	@Test
	void testFindReturnsNullOnNonFoundableExecutable()
	{
		IPath nonFoundableExecutable = executableFinder.find(NON_FOUNDABLE_EXE_FILE_STRING,
				true);
		assertNull(nonFoundableExecutable);
	}

	@Test
	void testFindReturnsNullOnFoundableNonExecutable()
	{
		IPath foundNonExecutable = executableFinder.find(FOUNDABLE_TEXT_FILE_STRING, true);
		assertNull(foundNonExecutable);
	}

	@Test
	void testFindReturnsNullOnNonFoundableNonExecutable()
	{
		IPath nonFoudableNonExecutable = executableFinder.find(NON_FOUNDABLE_TEXT_FILE_STRING,
				true);
		assertNull(nonFoudableNonExecutable);
	}

}
