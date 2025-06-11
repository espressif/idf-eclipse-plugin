package com.espressif.idf.core.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.util.IDFUtil;

public class IDFUtilTest
{

	@Test
	public void testGetIdfSysviewTraceScriptFile_ShouldReturnValidScriptFile()
	{
		IDFUtil.idfEnvVarPathProvider = env -> "esp_idf_path"; //$NON-NLS-1$

		File result = IDFUtil.getIDFSysviewTraceScriptFile();

		String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, //$NON-NLS-1$
				IDFConstants.IDF_APP_TRACE_FOLDER, IDFConstants.IDF_SYSVIEW_TRACE_SCRIPT).toString();

		assertEquals(expectedPath, result.getPath());
	}

	@Test
	public void testGetIDFToolsScriptFile_WithArgument_ShouldReturnValidScriptFile()
	{
		String inputPath = "custom_idf_path"; //$NON-NLS-1$
		File result = IDFUtil.getIDFToolsScriptFile(inputPath);

		String expectedPath = Paths.get("custom_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_TOOLS_SCRIPT)
				.toString();

		assertEquals(expectedPath, result.getPath());
	}

	@Test
	public void testGetIDFMonitorScriptFile_ShouldReturnValidScriptFile()
	{
		IDFUtil.idfEnvVarPathProvider = env -> "esp_idf_path"; //$NON-NLS-1$

		File result = IDFUtil.getIDFMonitorScriptFile();
		String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_MONITOR_SCRIPT)
				.toString();

		assertEquals(expectedPath, result.getPath());
	}

	@Test
	public void testGetIDFSizeScriptFile_ShouldReturnValidScriptFile()
	{
		IDFUtil.idfEnvVarPathProvider = env -> "esp_idf_path"; //$NON-NLS-1$

		File result = IDFUtil.getIDFSizeScriptFile();
		String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_SIZE_SCRIPT)
				.toString();

		assertEquals(expectedPath, result.getPath());
	}

	@Test
	public void testGetIDFToolsJsonFileForInstallation_ShouldReturnValidScriptFile()
	{
		IDFUtil.idfEnvVarPathProvider = env -> "esp_idf_path"; //$NON-NLS-1$

		File result = IDFUtil.getIDFToolsJsonFileForInstallation();
		String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_TOOLS_JSON)
				.toString();

		assertEquals(expectedPath, result.getPath());
	}

	@Test
	public void testGetIDFPath_ShouldReturnIDFPathSpecifiedInIDFEnvironmentVariables()
	{
		IDFUtil.idfEnvVarPathProvider = env -> "esp_idf_path"; //$NON-NLS-1$

		String result = IDFUtil.getIDFPath();
		String expected = "esp_idf_path";
		assertEquals(expected, result);
	}

	@Test
	public void testCheckIfIdfSupportsSpaces_VersionAbove5_ShouldReturnTrue()
	{
		try (MockedStatic<IDFUtil> mocked = mockStatic(IDFUtil.class, CALLS_REAL_METHODS))
		{
			mocked.when(IDFUtil::getEspIdfVersion).thenReturn("5.1.0");

			boolean result = IDFUtil.checkIfIdfSupportsSpaces();

			assertTrue(result);
		}
	}

	@Test
	public void testCheckIfIdfSupportsSpaces_VersionBelow5_ShouldReturnFalse()
	{
		try (MockedStatic<IDFUtil> mocked = mockStatic(IDFUtil.class, CALLS_REAL_METHODS))
		{
			mocked.when(IDFUtil::getEspIdfVersion).thenReturn("4.4.3");

			boolean result = IDFUtil.checkIfIdfSupportsSpaces();

			assertFalse(result);
		}
	}

	@Test
	public void testCheckIfIdfSupportsSpaces_CachesResultAfterFirstCall() throws Exception
	{
		try (MockedStatic<IDFUtil> mocked = mockStatic(IDFUtil.class, CALLS_REAL_METHODS))
		{
			mocked.when(IDFUtil::getEspIdfVersion).thenReturn("5.0.0");

			boolean firstCall = IDFUtil.checkIfIdfSupportsSpaces();
			assert (firstCall);

			mocked.when(IDFUtil::getEspIdfVersion).thenReturn("4.0.0"); // should not affect result
			boolean secondCall = IDFUtil.checkIfIdfSupportsSpaces();
			assertTrue(secondCall); // result is cached
		}
	}

}