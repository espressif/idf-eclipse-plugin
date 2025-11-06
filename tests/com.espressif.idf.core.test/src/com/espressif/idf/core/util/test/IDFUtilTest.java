package com.espressif.idf.core.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.toolchain.ESPToolChainManager;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.SDKConfigJsonReader;

public class IDFUtilTest
{
	@TempDir
	File tempDir;

	@Test
	public void testGetIdfSysviewTraceScriptFile_ShouldReturnValidScriptFile()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{

			File result = IDFUtil.getIDFSysviewTraceScriptFile();
			String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, //$NON-NLS-1$
					IDFConstants.IDF_APP_TRACE_FOLDER, IDFConstants.IDF_SYSVIEW_TRACE_SCRIPT).toString();

			assertEquals(expectedPath, result.getPath());
		}
	}

	@Test
	public void testGetIDFPythonScriptFile_ShouldReturnValidScriptFile()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{

			File result = IDFUtil.getIDFPythonScriptFile();
			String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_PYTHON_SCRIPT) //$NON-NLS-1$
					.toString();

			assertEquals(expectedPath, result.getPath());
		}
	}

	@Test
	public void testGetIDFPythonScriptFile_WithArgument_ShouldReturnValidScriptFile()
	{
		String idfPath = "esp_idf_path"; //$NON-NLS-1$

		File result = IDFUtil.getIDFPythonScriptFile(idfPath);
		String expectedPath = Paths.get(idfPath, IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_PYTHON_SCRIPT).toString();

		assertEquals(expectedPath, result.getPath());
	}

	@Test
	public void testGetIDFMonitorPythonScriptFile_ShouldReturnValidScriptFile()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{

			File result = IDFUtil.getIDFMonitorPythonScriptFile();
			String expectedPath = Paths
					.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_MONITOR_PYTHON_SCRIPT).toString(); //$NON-NLS-1$

			assertEquals(expectedPath, result.getPath());
		}
	}

	@Test
	public void testGetIDFToolsScriptFile_ShouldReturnValidScriptFile()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{

			File result = IDFUtil.getIDFToolsScriptFile();
			String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_TOOLS_SCRIPT) //$NON-NLS-1$
					.toString();

			assertEquals(expectedPath, result.getPath());
		}
	}

	@Test
	public void testGetIDFToolsScriptFile_WithArgument_ShouldReturnValidScriptFile()
	{
		String inputPath = "custom_idf_path"; //$NON-NLS-1$
		File result = IDFUtil.getIDFToolsScriptFile(inputPath);

		String expectedPath = Paths.get("custom_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_TOOLS_SCRIPT) //$NON-NLS-1$
				.toString();

		assertEquals(expectedPath, result.getPath());
	}

	@Test
	public void testGetIDFMonitorScriptFile_ShouldReturnValidScriptFile()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{
			File result = IDFUtil.getIDFMonitorScriptFile();
			String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_MONITOR_SCRIPT) //$NON-NLS-1$
					.toString();

			assertEquals(expectedPath, result.getPath());
		}
	}

	@Test
	public void testGetIDFSizeScriptFile_ShouldReturnValidScriptFile()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{
			File result = IDFUtil.getIDFSizeScriptFile();
			String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_SIZE_SCRIPT) //$NON-NLS-1$
					.toString();

			assertEquals(expectedPath, result.getPath());
		}
	}

	@Test
	public void testGetIDFToolsJsonFileForInstallation_ShouldReturnValidScriptFile()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{
			File result = IDFUtil.getIDFToolsJsonFileForInstallation();
			String expectedPath = Paths.get("esp_idf_path", IDFConstants.TOOLS_FOLDER, IDFConstants.IDF_TOOLS_JSON) //$NON-NLS-1$
					.toString();

			assertEquals(expectedPath, result.getPath());
		}
	}

	@Test
	public void testGetIDFPath_ShouldReturnIDFPathSpecifiedInIDFEnvironmentVariables()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{
			String result = IDFUtil.getIDFPath();
			String expected = "esp_idf_path"; //$NON-NLS-1$
			assertEquals(expected, result);
		}
	}

	@AfterEach
	public void resetCachedFlag() throws Exception
	{
		Field f = IDFUtil.class.getDeclaredField("idfSupportsSpaces"); //$NON-NLS-1$
		f.setAccessible(true);
		f.set(null, null); // reset static cached value
	}

	@Test
	public void testCheckIfIdfSupportsSpaces_VersionAbove5_ShouldReturnTrue()
	{
		try (MockedStatic<IDFUtil> mocked = mockStatic(IDFUtil.class, CALLS_REAL_METHODS))
		{
			mocked.when(IDFUtil::getEspIdfVersion).thenReturn("5.1.0"); //$NON-NLS-1$

			boolean result = IDFUtil.checkIfIdfSupportsSpaces();

			assertTrue(result);
		}
	}

	@Test
	public void testCheckIfIdfSupportsSpaces_VersionBelow5_ShouldReturnFalse()
	{
		try (MockedStatic<IDFUtil> mocked = mockStatic(IDFUtil.class, CALLS_REAL_METHODS))
		{
			mocked.when(IDFUtil::getEspIdfVersion).thenReturn("4.4.3"); //$NON-NLS-1$

			boolean result = IDFUtil.checkIfIdfSupportsSpaces();

			assertFalse(result);
		}
	}

	@Test
	public void testCheckIfIdfSupportsSpaces_CachesResultAfterFirstCall() throws Exception
	{
		try (MockedStatic<IDFUtil> mocked = mockStatic(IDFUtil.class, CALLS_REAL_METHODS))
		{
			mocked.when(IDFUtil::getEspIdfVersion).thenReturn("5.0.0"); //$NON-NLS-1$

			boolean firstCall = IDFUtil.checkIfIdfSupportsSpaces();
			assert (firstCall);

			mocked.when(IDFUtil::getEspIdfVersion).thenReturn("4.0.0"); // should not affect result //$NON-NLS-1$
			boolean secondCall = IDFUtil.checkIfIdfSupportsSpaces();
			assertTrue(secondCall); // result is cached
		}
	}

	@Test
	public void testGetLineSeparatorValue_ShouldReturnLineSeparator()
	{
		String expected = System.getProperty("line.separator"); //$NON-NLS-1$
		String result = IDFUtil.getLineSeparatorValue();
		assertEquals(expected, result);
	}

	@Test
	public void testGetIDFExtraPaths_WhenIDFPathIsSet_ShouldReturnExpectedPaths()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{
			String result = IDFUtil.getIDFExtraPaths();

			String expected = new Path("esp_idf_path").append("components/esptool_py/esptool").append(":") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.append("esp_idf_path").append("components/espcoredump").append(":").append("esp_idf_path") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					.append("components/partition_table").append(":").append("esp_idf_path") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.append("components/app_update") //$NON-NLS-1$
					.toString();

			assertEquals(expected, result);
		}
	}

	@Test
	public void testGetIDFExtraPaths_WhenIDFPathIsEmpty_ShouldReturnEmptyString()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn(""))) //$NON-NLS-1$
		{
			String result = IDFUtil.getIDFExtraPaths();
			assertEquals("", result); //$NON-NLS-1$
		}
	}

	@Test
	public void testGetOpenOCDLocation_ShouldReturnOpenOCDScriptsLocationFromIdfEnvVar()
	{
		String openocdScriptsLoc = "openocd_path" + File.separator + "share" + File.separator + "openocd" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ File.separator + "scripts"; //$NON-NLS-1$

		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS))
						.thenReturn(openocdScriptsLoc)))
		{
			String result = IDFUtil.getOpenOCDLocation();

			String expected = openocdScriptsLoc
					.replace(File.separator + "share" + File.separator + "openocd" + File.separator + "scripts", "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ File.separator + "bin"; //$NON-NLS-1$

			assertEquals(expected, result);
		}
	}

	@Test
	void testGetXtensaToolchainExecutablePathByTarget_DebuggerFound_ShouldReturnPath()
	{
		File mockFile = mock(File.class);
		when(mockFile.getAbsolutePath()).thenReturn("/mock/debugger/xtensa-gdb"); //$NON-NLS-1$

		try (MockedConstruction<ESPToolChainManager> mocked = mockConstruction(ESPToolChainManager.class,
				(mock, context) -> when(mock.findDebugger("esp32")).thenReturn(mockFile))) //$NON-NLS-1$
		{
			String result = IDFUtil.getXtensaToolchainExecutablePathByTarget("esp32"); //$NON-NLS-1$
			assertEquals("/mock/debugger/xtensa-gdb", result); //$NON-NLS-1$
		}
	}

	@Test
	void testGetXtensaToolchainExecutablePathByTarget_DebuggerNotFound_ShouldReturnNull()
	{
		try (MockedConstruction<ESPToolChainManager> mocked = mockConstruction(ESPToolChainManager.class,
				(mock, context) -> when(mock.findDebugger("esp32")).thenReturn(null))) //$NON-NLS-1$
		{
			String result = IDFUtil.getXtensaToolchainExecutablePathByTarget("esp32"); //$NON-NLS-1$
			assertNull(result);
		}
	}

	@Test
	void testGetToolchainExePathForActiveTarget_ShouldReturnPath() throws CoreException
	{
		ILaunchBarManager mockManager = mock(ILaunchBarManager.class);
		ILaunchTarget mockTarget = mock(ILaunchTarget.class);
		File mockFile = mock(File.class);

		when(mockTarget.getAttribute(eq(LaunchBarTargetConstants.TARGET), any())).thenReturn("esp32"); //$NON-NLS-1$
		when(mockFile.getAbsolutePath()).thenReturn("/mock/toolchain/xtensa-gcc"); //$NON-NLS-1$

		try (MockedStatic<IDFCorePlugin> mockedPlugin = mockStatic(IDFCorePlugin.class);
				MockedConstruction<ESPToolChainManager> mockedToolchain = mockConstruction(ESPToolChainManager.class,
						(mock, context) -> when(mock.findCompiler("esp32")).thenReturn(mockFile))) //$NON-NLS-1$
		{
			mockedPlugin.when(() -> IDFCorePlugin.getService(ILaunchBarManager.class)).thenReturn(mockManager);
			when(mockManager.getActiveLaunchTarget()).thenReturn(mockTarget);

			String result = IDFUtil.getToolchainExePathForActiveTarget();

			assertEquals("/mock/toolchain/xtensa-gcc", result); //$NON-NLS-1$
		}
	}

	@Test
	void testGetToolchainExePathForActiveTarget_NullLaunchTarget_ShouldReturnNull() throws CoreException
	{
		ILaunchBarManager mockManager = mock(ILaunchBarManager.class);

		try (MockedStatic<IDFCorePlugin> mockedPlugin = mockStatic(IDFCorePlugin.class))
		{
			mockedPlugin.when(() -> IDFCorePlugin.getService(ILaunchBarManager.class)).thenReturn(mockManager);
			when(mockManager.getActiveLaunchTarget()).thenReturn(null);

			String result = IDFUtil.getToolchainExePathForActiveTarget();

			assertNull(result);
		}
	}

	@Test
	void testGetXtensaToolchainExecutableAddr2LinePath_shouldReturnMatchingPath() throws IOException
	{
		IProject mockProject = mock(IProject.class);
		String target = "esp32"; //$NON-NLS-1$
		File matchingDir = new File(tempDir, "toolchain"); //$NON-NLS-1$
		matchingDir.mkdirs();
		File matchingFile = new File(matchingDir, "xtensa-" + target + "-elf-addr2line"); //$NON-NLS-1$ //$NON-NLS-2$
		matchingFile.createNewFile();

		try (MockedConstruction<SDKConfigJsonReader> mockedReader = mockConstruction(SDKConfigJsonReader.class,
				(reader, context) -> when(reader.getValue("IDF_TARGET")).thenReturn(target)); //$NON-NLS-1$

				MockedConstruction<IDFEnvironmentVariables> mockedEnv = mockConstruction(IDFEnvironmentVariables.class,
						(env, context) -> {
							IEnvironmentVariable pathVar = mock(IEnvironmentVariable.class);
							when(pathVar.getValue()).thenReturn(matchingDir.getAbsolutePath());
							when(env.getEnv("PATH")).thenReturn(pathVar); //$NON-NLS-1$
						}))
		{

			String result = IDFUtil.getXtensaToolchainExecutableAddr2LinePath(mockProject);
			assertEquals(matchingFile.getAbsolutePath(), result);
		}
	}

	@Test
	public void testGetEspToolScriptFile_ShouldReturnValidScriptFile()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{

			File result = IDFUtil.getEspToolScriptFile();
			String expectedPath = Paths.get("esp_idf_path", IDFConstants.COMPONENTS_FOLDER, //$NON-NLS-1$
					IDFConstants.ESP_TOOL_FOLDER_PY, IDFConstants.ESP_TOOL_FOLDER, IDFConstants.ESP_TOOL_SCRIPT)
					.toString();

			assertEquals(expectedPath, result.getPath());
		}
	}

	@Test
	public void testGetEspCoreDumpScriptFile_ShouldReturnValidScriptFile()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{

			File result = IDFUtil.getEspCoreDumpScriptFile();
			String expectedPath = Paths.get("esp_idf_path", IDFConstants.COMPONENTS_FOLDER, //$NON-NLS-1$
					IDFConstants.ESP_CORE_DUMP_FOLDER, IDFConstants.ESP_CORE_DUMP_SCRIPT).toString();

			assertEquals(expectedPath, result.getPath());
		}
	}

	@Test
	public void testGetNvsGeneratorScriptPath_ShouldReturnValidScriptPath()
	{
		try (MockedConstruction<IDFEnvironmentVariables> mocked = mockConstruction(IDFEnvironmentVariables.class,
				(mock, context) -> when(mock.getEnvValue(IDFEnvironmentVariables.IDF_PATH)).thenReturn("esp_idf_path"))) //$NON-NLS-1$
		{

			String result = IDFUtil.getNvsGeneratorScriptPath();

			String expectedPath = Stream
					.of("esp_idf_path", "components", "nvs_flash", "nvs_partition_generator", "nvs_partition_gen.py") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					.collect(Collectors.joining(String.valueOf(IPath.SEPARATOR)));

			assertEquals(expectedPath, result);
		}
	}

}
