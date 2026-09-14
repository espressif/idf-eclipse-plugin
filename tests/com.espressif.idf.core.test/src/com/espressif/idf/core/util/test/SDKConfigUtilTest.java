package com.espressif.idf.core.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.util.SDKConfigUtil;

class SDKConfigUtilTest
{
	@TempDir
	File tempDirectory;

	@Test
	void getConfigMenuFilePathUsesExplicitBuildDirectory() throws Exception
	{
		String expected = new File(new File(tempDirectory, IDFConstants.CONFIG_FOLDER),
				IDFConstants.KCONFIG_MENUS_JSON).getAbsolutePath();

		String actual = new SDKConfigUtil().getConfigMenuFilePath(tempDirectory.getAbsolutePath());

		assertEquals(expected, actual);
	}

	@Test
	void getConfigMenuFilePathRejectsMissingBuildDirectory()
	{
		File missingDirectory = new File(tempDirectory, "missing"); //$NON-NLS-1$

		Exception exception = assertThrows(Exception.class,
				() -> new SDKConfigUtil().getConfigMenuFilePath(missingDirectory.getAbsolutePath()));

		assertEquals("Build directory is not found: " + missingDirectory.getAbsolutePath(), exception.getMessage()); //$NON-NLS-1$
	}
}
