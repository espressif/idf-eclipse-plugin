/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;

/**
 * SWTBot tests for the ESP-IDF Manager editor shell.
 *
 * @author Andrii Filippov
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EspIdfManagerTest
{
	@BeforeClass
	public static void beforeClass() throws Exception
	{
		Fixture.loadEnv();
	}

	@Test
	public void test01_givenEspressifEnvIsConfiguredWhenOpeningEspIdfManagerFromMenuThenEditorShowsInstalledVersionsTable()
			throws Exception
	{
		Fixture.givenEspressifEnvIsConfigured();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.thenEditorShowsInstalledVersionsTable();
	}

	@Test
	public void test02_givenEspIdfManagerIsOpenWhenEnvSetupCompletedThenActiveVersionIsListed() throws Exception
	{
		Fixture.givenEspressifEnvIsConfigured();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.thenActiveEspIdfVersionIsListed();
	}

	@Test
	public void test03_givenActiveEspIdfWhenRefreshEnvironmentIsClickedThenToolsSetupCompletesInConsole() throws Exception
	{
		Fixture.givenEspressifEnvIsConfigured();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenRefreshEnvironmentIsClicked();
		Fixture.thenToolsSetupCompletesInConsole();
	}

	@Test
	public void test04_givenActiveEspIdfWhenBuildEnvironmentPreferencesAreOpenedThenIdfVariablesMatchActiveVersion()
			throws Exception
	{
		Fixture.givenEspressifEnvIsConfigured();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenBuildEnvironmentPreferencesAreOpened();
		Fixture.thenIdfEnvironmentVariablesMatchActiveVersion();
	}

	@After
	public void afterEach()
	{
		Fixture.closePreferencesDialogIfOpen();
		Fixture.closeEspIdfManagerIfOpen();
	}

	private static class Fixture
	{
		private static final String EDITOR_TITLE = "ESP-IDF Manager"; //$NON-NLS-1$
		private static final String MANAGE_VERSIONS_BUTTON = "Manage ESP-IDF Versions"; //$NON-NLS-1$
		private static final String ACTIVATE_SELECTED_BUTTON = "Activate Selected"; //$NON-NLS-1$
		private static final String REFRESH_ENVIRONMENT_BUTTON = "Refresh Environment"; //$NON-NLS-1$
		private static final String ACTIVE_STATUS_MARKER = "Active"; //$NON-NLS-1$
		private static final String VERSION_DETECTION_FAILED = "Detection Failed"; //$NON-NLS-1$
		private static final String TOOLS_SETUP_COMPLETE = "Tools Setup complete"; //$NON-NLS-1$
		private static final String SETTING_UP_IDE_ENVIRONMENT = "Setting up IDE environment"; //$NON-NLS-1$
		private static final String TOOLS_CONSOLE_NAME = "Espressif IDF Tools Console"; //$NON-NLS-1$
		private static final String IDF_PATH_VARIABLE = "IDF_PATH"; //$NON-NLS-1$
		private static final String IDF_PYTHON_ENV_PATH_VARIABLE = "IDF_PYTHON_ENV_PATH"; //$NON-NLS-1$
		private static final String PYTHON_EXE_PATH_VARIABLE = "PYTHON_EXE_PATH"; //$NON-NLS-1$
		private static final String IDF_TOOLS_PATH_VARIABLE = "IDF_TOOLS_PATH"; //$NON-NLS-1$
		private static final String ESP_IDF_VERSION_VARIABLE = "ESP_IDF_VERSION"; //$NON-NLS-1$
		private static final String ESP_IDF_EIM_ID_VARIABLE = "ESP_IDF_EIM_ID"; //$NON-NLS-1$
		private static final String IDF_TOOLS_PATH_PROPERTY = "default.env.idf.tools.path"; //$NON-NLS-1$
		private static final long TABLE_LOAD_TIMEOUT_MS = 300_000L;
		private static final long CONSOLE_WAIT_TIMEOUT_MS = 600_000L;
		private static final long REFRESH_START_TIMEOUT_MS = 60_000L;

		private static SWTWorkbenchBot bot;
		private static int toolsSetupCompleteCountBeforeAction;
		private static String activeInstallationLocation;
		private static String activeInstallationPathVersionToken;
		private static String activeInstallationTableVersion;

		static void loadEnv() throws Exception
		{
			SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US"; //$NON-NLS-1$
			SWTBotPreferences.SCREENSHOTS_DIR = "screenshots/EspIdfManagerEditor/"; //$NON-NLS-1$
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
		}

		static void givenEspressifEnvIsConfigured()
		{
			bot.shell().activate();
		}

		static void whenEspIdfManagerIsOpenedFromMenu()
		{
			closeEspIdfManagerIfOpen();
			bot.shell().activate();
			bot.menu("Espressif").menu(EDITOR_TITLE).click(); //$NON-NLS-1$
			TestWidgetWaitUtility.waitForCTabToAppear(bot, EDITOR_TITLE, TABLE_LOAD_TIMEOUT_MS);
			bot.editorByTitle(EDITOR_TITLE).show();
			waitForInstalledVersionsTableToLoad();
			readActiveInstallationDetails(bot.editorByTitle(EDITOR_TITLE).bot().table());
		}

		static void thenEditorShowsInstalledVersionsTable()
		{
			SWTBotEditor editor = bot.editorByTitle(EDITOR_TITLE);
			editor.bot().button(MANAGE_VERSIONS_BUTTON);
			editor.bot().button(ACTIVATE_SELECTED_BUTTON);
			editor.bot().button(REFRESH_ENVIRONMENT_BUTTON);

			SWTBotTable table = editor.bot().table();
			assertTrue("Expected at least one installed ESP-IDF version", table.rowCount() > 0); //$NON-NLS-1$
			assertTrue("Expected four table columns", table.columnCount() >= 4); //$NON-NLS-1$
		}

		static void thenActiveEspIdfVersionIsListed()
		{
			SWTBotTable table = bot.editorByTitle(EDITOR_TITLE).bot().table();
			assertTrue("Expected an active ESP-IDF installation after env setup", hasActiveInstallation(table)); //$NON-NLS-1$
			assertTrue("Expected Refresh Environment to be enabled for an active installation", //$NON-NLS-1$
					bot.editorByTitle(EDITOR_TITLE).bot().button(REFRESH_ENVIRONMENT_BUTTON).isEnabled());
		}

		static void whenRefreshEnvironmentIsClicked()
		{
			SWTBotEditor editor = bot.editorByTitle(EDITOR_TITLE);
			editor.show();
			editor.setFocus();

			SWTBotView toolsConsole = ProjectTestOperations.viewConsole(TOOLS_CONSOLE_NAME, bot);
			toolsConsole.show();
			toolsConsole.setFocus();
			toolsSetupCompleteCountBeforeAction = countOccurrences(getConsoleText(toolsConsole),
					TOOLS_SETUP_COMPLETE);

			SWTBotButton refreshButton = editor.bot().button(REFRESH_ENVIRONMENT_BUTTON);
			assertTrue("Refresh Environment must be enabled before clicking it", refreshButton.isEnabled()); //$NON-NLS-1$
			refreshButton.click();

			waitForToolsSetupToStart(editor, toolsConsole, REFRESH_ENVIRONMENT_BUTTON);
		}

		static void thenToolsSetupCompletesInConsole()
		{
			SWTBotView toolsConsole = ProjectTestOperations.viewConsole(TOOLS_CONSOLE_NAME, bot);
			toolsConsole.show();
			toolsConsole.setFocus();

			final int expectedMinimumCount = toolsSetupCompleteCountBeforeAction + 1;
			toolsConsole.bot().waitUntil(new DefaultCondition()
			{
				@Override
				public boolean test() throws Exception
				{
					String consoleText = getConsoleText(toolsConsole);
					return countOccurrences(consoleText, TOOLS_SETUP_COMPLETE) >= expectedMinimumCount
							&& consoleText.contains(SETTING_UP_IDE_ENVIRONMENT);
				}

				@Override
				public String getFailureMessage()
				{
					return "Expected a new tools setup run to finish in console"; //$NON-NLS-1$
				}
			}, CONSOLE_WAIT_TIMEOUT_MS, 3000);

			SWTBotEditor editor = bot.editorByTitle(EDITOR_TITLE);
			editor.bot().waitUntil(new DefaultCondition()
			{
				@Override
				public boolean test() throws Exception
				{
					return editor.bot().button(REFRESH_ENVIRONMENT_BUTTON).isEnabled();
				}

				@Override
				public String getFailureMessage()
				{
					return "Refresh Environment did not become enabled after tools setup finished"; //$NON-NLS-1$
				}
			}, CONSOLE_WAIT_TIMEOUT_MS, 3000);
		}

		static void whenBuildEnvironmentPreferencesAreOpened()
		{
			assertNotNull("Active ESP-IDF installation location must be known before opening preferences", //$NON-NLS-1$
					activeInstallationLocation);
			assertNotNull("Active ESP-IDF path version token must be known before opening preferences", //$NON-NLS-1$
					activeInstallationPathVersionToken);
			assertNotNull("Active ESP-IDF version must be known before opening preferences", //$NON-NLS-1$
					activeInstallationTableVersion);

			bot.menu("Window").menu("Preferences...").click(); //$NON-NLS-1$ //$NON-NLS-2$
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Preferences", 10_000L); //$NON-NLS-1$

			SWTBotShell preferencesShell = bot.shell("Preferences"); //$NON-NLS-1$
			preferencesShell.bot().tree().getTreeItem("C/C++").select(); //$NON-NLS-1$
			preferencesShell.bot().tree().getTreeItem("C/C++").expand(); //$NON-NLS-1$
			preferencesShell.bot().tree().getTreeItem("C/C++").getNode("Build").select(); //$NON-NLS-1$ //$NON-NLS-2$
			preferencesShell.bot().tree().getTreeItem("C/C++").getNode("Build").expand(); //$NON-NLS-1$ //$NON-NLS-2$
			preferencesShell.bot().tree().getTreeItem("C/C++").getNode("Build").getNode("Environment").select(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		static void thenIdfEnvironmentVariablesMatchActiveVersion() throws IOException
		{
			SWTBotShell preferencesShell = bot.shell("Preferences"); //$NON-NLS-1$
			SWTBotTable environmentTable = preferencesShell.bot().table();

			String idfPath = getEnvironmentVariableValue(environmentTable, IDF_PATH_VARIABLE);
			String idfPythonEnvPath = getEnvironmentVariableValue(environmentTable, IDF_PYTHON_ENV_PATH_VARIABLE);
			String pythonExePath = getEnvironmentVariableValue(environmentTable, PYTHON_EXE_PATH_VARIABLE);
			String idfToolsPath = getEnvironmentVariableValue(environmentTable, IDF_TOOLS_PATH_VARIABLE);
			String espIdfVersion = getEnvironmentVariableValue(environmentTable, ESP_IDF_VERSION_VARIABLE);
			String espIdfEimId = getEnvironmentVariableValue(environmentTable, ESP_IDF_EIM_ID_VARIABLE);
			String expectedToolsPath = DefaultPropertyFetcher.getStringPropertyValue(IDF_TOOLS_PATH_PROPERTY,
					getDefaultToolsPath());
			String expectedPythonEnvPath = buildExpectedPythonEnvPath(expectedToolsPath,
					activeInstallationPathVersionToken);
			String expectedPythonExePath = buildExpectedPythonExePath(expectedToolsPath,
					activeInstallationPathVersionToken);

			assertNotNull("IDF_PATH must be set in C/C++ Build Environment", idfPath); //$NON-NLS-1$
			assertNotNull("IDF_PYTHON_ENV_PATH must be set in C/C++ Build Environment", idfPythonEnvPath); //$NON-NLS-1$
			assertNotNull("PYTHON_EXE_PATH must be set in C/C++ Build Environment", pythonExePath); //$NON-NLS-1$
			assertNotNull("IDF_TOOLS_PATH must be set in C/C++ Build Environment", idfToolsPath); //$NON-NLS-1$
			assertNotNull("ESP_IDF_VERSION must be set in C/C++ Build Environment", espIdfVersion); //$NON-NLS-1$
			assertNotNull("ESP_IDF_EIM_ID must be set in C/C++ Build Environment", espIdfEimId); //$NON-NLS-1$
			assertNotNull("Active ESP-IDF version folder must be present in installation path", //$NON-NLS-1$
					activeInstallationPathVersionToken);
			assertNotNull("Active ESP-IDF version must be present in manager table", activeInstallationTableVersion); //$NON-NLS-1$

			assertTrue("ESP_IDF_EIM_ID must not be empty", !espIdfEimId.isBlank()); //$NON-NLS-1$
			assertEquals("ESP_IDF_VERSION must match active row in ESP-IDF Manager", activeInstallationTableVersion, //$NON-NLS-1$
					espIdfVersion);

			assertPathContainsVersionToken("IDF_PATH", idfPath); //$NON-NLS-1$
			assertPathContainsVersionToken("IDF_PYTHON_ENV_PATH", idfPythonEnvPath); //$NON-NLS-1$
			assertPathContainsVersionToken("PYTHON_EXE_PATH", pythonExePath); //$NON-NLS-1$

			assertPathEquals("IDF_PATH must point to the active ESP-IDF installation", activeInstallationLocation, //$NON-NLS-1$
					idfPath);
			assertPathEquals("IDF_TOOLS_PATH must match configured tools directory", expectedToolsPath, idfToolsPath); //$NON-NLS-1$
			assertPathEquals("IDF_PYTHON_ENV_PATH must use the active ESP-IDF python virtual environment", //$NON-NLS-1$
					expectedPythonEnvPath, idfPythonEnvPath);
			assertPathEquals("PYTHON_EXE_PATH must use the active ESP-IDF python executable", expectedPythonExePath, //$NON-NLS-1$
					pythonExePath);

			closePreferencesDialog();
		}

		static void closeEspIdfManagerIfOpen()
		{
			try
			{
				bot.cTabItem(EDITOR_TITLE).close();
			}
			catch (WidgetNotFoundException ignored)
			{
			}
		}

		static void closePreferencesDialogIfOpen()
		{
			try
			{
				bot.shell("Preferences"); //$NON-NLS-1$
				closePreferencesDialog();
			}
			catch (WidgetNotFoundException ignored)
			{
			}
		}

		private static void waitForInstalledVersionsTableToLoad()
		{
			SWTBotEditor editor = bot.editorByTitle(EDITOR_TITLE);
			editor.bot().waitUntil(new DefaultCondition()
			{
				@Override
				public boolean test() throws Exception
				{
					SWTBotTable table = editor.bot().table();
					if (table.rowCount() == 0)
					{
						return false;
					}
					for (int row = 0; row < table.rowCount(); row++)
					{
						String version = table.cell(row, 1);
						if (!version.isEmpty() && !VERSION_DETECTION_FAILED.equals(version))
						{
							return true;
						}
					}
					return false;
				}

				@Override
				public String getFailureMessage()
				{
					return "Installed ESP-IDF versions table did not finish loading"; //$NON-NLS-1$
				}
			}, TABLE_LOAD_TIMEOUT_MS, 2000);
		}

		private static boolean hasActiveInstallation(SWTBotTable table)
		{
			for (int row = 0; row < table.rowCount(); row++)
			{
				String status = table.cell(row, 0);
				String location = table.cell(row, 3);
				if (status.contains(ACTIVE_STATUS_MARKER) && !location.isEmpty())
				{
					return true;
				}
			}
			return false;
		}

		private static void waitForToolsSetupToStart(SWTBotEditor editor, SWTBotView toolsConsole, String actionButtonLabel)
		{
			editor.bot().waitUntil(new DefaultCondition()
			{
				@Override
				public boolean test() throws Exception
				{
					if (!editor.bot().button(actionButtonLabel).isEnabled())
					{
						return true;
					}

					String consoleText = getConsoleText(toolsConsole);
					return consoleText.contains(SETTING_UP_IDE_ENVIRONMENT)
							|| countOccurrences(consoleText, TOOLS_SETUP_COMPLETE) > toolsSetupCompleteCountBeforeAction;
				}

				@Override
				public String getFailureMessage()
				{
					return "Tools setup did not start after clicking " + actionButtonLabel; //$NON-NLS-1$
				}
			}, REFRESH_START_TIMEOUT_MS, 500);
		}

		private static String getConsoleText(SWTBotView consoleView)
		{
			consoleView.show();
			consoleView.setFocus();
			return consoleView.bot().styledText().getText();
		}

		private static int countOccurrences(String text, String needle)
		{
			int count = 0;
			int index = 0;
			while ((index = text.indexOf(needle, index)) != -1)
			{
				count++;
				index += needle.length();
			}
			return count;
		}

		private static void readActiveInstallationDetails(SWTBotTable table)
		{
			activeInstallationLocation = null;
			activeInstallationPathVersionToken = null;
			activeInstallationTableVersion = null;

			for (int row = 0; row < table.rowCount(); row++)
			{
				if (table.cell(row, 0).contains(ACTIVE_STATUS_MARKER))
				{
					activeInstallationLocation = table.cell(row, 3);
					activeInstallationPathVersionToken = extractVersionTokenFromPath(activeInstallationLocation);
					activeInstallationTableVersion = table.cell(row, 1);
					return;
				}
			}
		}

		private static String extractVersionTokenFromPath(String path)
		{
			if (path == null || path.isEmpty())
			{
				return null;
			}

			for (String segment : path.replace('/', '\\').split("\\\\")) //$NON-NLS-1$
			{
				if (segment.matches("v\\d+(?:\\.\\d+)*")) //$NON-NLS-1$
				{
					return segment;
				}
			}
			return null;
		}

		private static String getEnvironmentVariableValue(SWTBotTable environmentTable, String variableName)
		{
			for (int row = 0; row < environmentTable.rowCount(); row++)
			{
				if (variableName.equals(environmentTable.cell(row, 0)))
				{
					return environmentTable.cell(row, 1);
				}
			}
			return null;
		}

		private static void closePreferencesDialog()
		{
			SWTBotShell preferencesShell = bot.shell("Preferences"); //$NON-NLS-1$
			preferencesShell.bot().button("Cancel").click(); //$NON-NLS-1$
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Preferences", 10_000L); //$NON-NLS-1$
		}

		private static String getDefaultToolsPath()
		{
			if (Platform.getOS().equals(Platform.OS_WIN32))
			{
				return "C:\\Espressif\\tools"; //$NON-NLS-1$
			}
			return Paths.get(System.getProperty("user.home"), ".espressif", "tools").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		private static String buildExpectedPythonEnvPath(String toolsPath, String versionToken)
		{
			return Paths.get(toolsPath, "python", versionToken, "venv").toString(); //$NON-NLS-1$ //$NON-NLS-2$
		}

		private static String buildExpectedPythonExePath(String toolsPath, String versionToken)
		{
			if (Platform.getOS().equals(Platform.OS_WIN32))
			{
				return Paths.get(toolsPath, "python", versionToken, "venv", "Scripts", "python.exe").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			return Paths.get(toolsPath, "python", versionToken, "venv", "bin", "python").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		private static void assertPathEquals(String message, String expected, String actual)
		{
			String normalizedExpected = normalizePath(expected);
			String normalizedActual = normalizePath(actual);
			if (Platform.getOS().equals(Platform.OS_WIN32))
			{
				assertEquals(message + " (expected: " + normalizedExpected + ", actual: " + normalizedActual + ")", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						normalizedExpected.toLowerCase(), normalizedActual.toLowerCase());
				return;
			}
			assertEquals(message + " (expected: " + normalizedExpected + ", actual: " + normalizedActual + ")", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					normalizedExpected, normalizedActual);
		}

		private static void assertPathContainsVersionToken(String variableName, String pathValue)
		{
			assertTrue(variableName + " must contain active ESP-IDF version folder " + activeInstallationPathVersionToken //$NON-NLS-1$
					+ " but was: " + pathValue, pathValue.contains(activeInstallationPathVersionToken)); //$NON-NLS-1$
		}

		private static String normalizePath(String path)
		{
			if (path == null)
			{
				return null;
			}
			return Paths.get(path.replace('\\', '/')).normalize().toString();
		}
	}
}
