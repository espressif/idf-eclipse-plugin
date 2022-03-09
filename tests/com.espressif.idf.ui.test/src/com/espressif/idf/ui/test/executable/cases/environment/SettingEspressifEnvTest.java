package com.espressif.idf.ui.test.executable.cases.environment;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.ui.test.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;

@RunWith(SWTBotJunit4ClassRunner.class)
public class SettingEspressifEnvTest
{
	private Fixture fixture;

	@Before
	public void beforeEachTest() throws Exception
	{
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.SCREENSHOTS_DIR = "screenshots/SettingEspressifEnvTest/";
		fixture = new Fixture();
	}

	@After
	public void afterEachTest()
	{
		fixture.cleanTestEnv();
	}

	@Test
	public void givenEspIdfFromExistingDirecotryWhenEspIdfIsConfiguredThenEspEnvIsConfigured() throws Exception
	{
		fixture.givenEspIdfPathsAreLoadedFromConfigs();
		fixture.whenInstallToolsIsSelected();
		fixture.whenSettingsAreConfiguredAndOkIsPressed();
		fixture.thenConsoleShowsToolsAreInstalled();
		fixture.thenConsoleHasNoErrorsAndFailures();
		fixture.thenIdfPathIsFound();
	}

	@Test
	public void givenEspIdfIsDownloadedAndConfiguredThroughTheDownloadAndConfigureOptionThenEspEnvIsConfigured()
			throws Exception
	{
		fixture.givenEspIdfPathsAreLoadedFromConfigs();
		fixture.whenDownloadAndConfigureEspIdfIsSelected();
		fixture.whenDownloadPathIsGivenAndFinishIsPressed();
		fixture.thenIdfPathIsFound();
	}
	
	@Test
	public void givenEspIdfIsConfiguredThroughAlreadyDownloadedToolsThenEnvIsConfigured() throws Exception
	{
		fixture.givenEspIdfPathsAreLoadedFromConfigs();
		fixture.whenDownloadAndConfigureEspIdfIsSelected();
		fixture.whenLocalPathIsGivenAndFinishIsPressed();
		fixture.thenIdfPathIsFound();
	}

	private class Fixture
	{
		private static final String ESPRESSIF_MENU = "Espressif";
		private static final String ESP_IDF_PATH_PROPERTY = "default.env.esp.idf.path";
		private static final String GIT_PATH_PROPERTY = "default.env.esp.git.path";
		private static final String PYTHON_VERSION_PROPERTY = "default.env.esp.python.version";
		private static final String ESP_IDF_DOWNLOAD_PATH_PROPERTY = "default.env.esp.idf.download.path";
		private SWTWorkbenchBot bot;
		private String espIdfPath;
		private String gitPath;
		private String pythonVersion;
		private String espIdfDownloadPath;

		private Fixture()
		{
			bot = new SWTWorkbenchBot();
			for (SWTBotView view : bot.views(withPartName("Welcome")))
			{
				view.close();
			}
			bot.menu("Window").menu("Perspective").menu("Open Perspective").menu("Other...").click();
			bot.table().select("C/C++");
			bot.button("Open").click();
		}

		private void cleanTestEnv()
		{
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private void givenEspIdfPathsAreLoadedFromConfigs() throws Exception
		{
			espIdfPath = DefaultPropertyFetcher.getStringPropertyValue(ESP_IDF_PATH_PROPERTY, "");
			gitPath = DefaultPropertyFetcher.getStringPropertyValue(GIT_PATH_PROPERTY, "");
			pythonVersion = DefaultPropertyFetcher.getStringPropertyValue(PYTHON_VERSION_PROPERTY, "");
			espIdfDownloadPath = DefaultPropertyFetcher.getStringPropertyValue(ESP_IDF_DOWNLOAD_PATH_PROPERTY, "");
			if (StringUtils.isEmpty(espIdfPath) || StringUtils.isEmpty(gitPath) || StringUtils.isEmpty(pythonVersion)
					|| StringUtils.isEmpty(espIdfDownloadPath))
			{
				throw new Exception("Missing Required Properties");
			}
		}

		private void whenInstallToolsIsSelected()
		{
			bot.menu(ESPRESSIF_MENU).menu("ESP-IDF Tools Manager").menu("Install Tools").click();
		}

		private void whenDownloadAndConfigureEspIdfIsSelected()
		{
			bot.menu(ESPRESSIF_MENU).menu("Download and Configure ESP-IDF").click();
		}

		private void whenSettingsAreConfiguredAndOkIsPressed()
		{
			bot.textWithLabel("ESP-IDF Directory:").setText(espIdfPath);
			bot.textWithLabel("Git Executable Location:").setText(gitPath);
			bot.comboBox().setSelection(pythonVersion);
			bot.button("Install Tools").click();
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			TestWidgetWaitUtility.waitUntilViewContains(bot, "Install tools completed", consoleView, 60000);
		}

		private void whenDownloadPathIsGivenAndFinishIsPressed() throws IOException
		{
			FileUtils.deleteDirectory(new File(espIdfDownloadPath));
			bot.textWithLabel("Choose a directory to download ESP-IDF to:").setText(espIdfDownloadPath);
			bot.comboBox().setSelection("master");
			bot.button("Finish").click();
			// need to wait here more as this is being downloaded
			TestWidgetWaitUtility.waitUntilDialogIsNotVisible(bot, "Message", 9000000);
			bot.button("Yes").click();
			bot.textWithLabel("Git Executable Location:").setText(gitPath);
			bot.button("Install Tools").click();
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			TestWidgetWaitUtility.waitUntilViewContains(bot, "Install tools completed", consoleView, 600000);
		}
		
		private void whenLocalPathIsGivenAndFinishIsPressed()
		{
			bot.checkBox("Use an existing ESP-IDF directory from file system").click();
			bot.textWithLabel("Choose existing ESP-IDF directory:").setText(espIdfPath);
			bot.button("Finish").click();
			bot.button("Yes").click();
			bot.textWithLabel("Git Executable Location:").setText(gitPath);
			bot.button("Install Tools").click();
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			TestWidgetWaitUtility.waitUntilViewContains(bot, "Install tools completed", consoleView, 60000);
		}

		private void thenConsoleShowsToolsAreInstalled()
		{
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			assertTrue(consoleView.bot().styledText().getText().contains("Install tools completed"));
		}

		private void thenConsoleHasNoErrorsAndFailures()
		{
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			assertFalse(consoleView.bot().styledText().getText().toLowerCase().contains("error"));
			assertFalse(consoleView.bot().styledText().getText().toLowerCase().contains("fail"));
		}

		private void thenIdfPathIsFound()
		{
			bot.menu(ESPRESSIF_MENU).menu("Product Information").click();
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			assertFalse(consoleView.bot().styledText().getText().contains("IDF_PATH: <NOT FOUND>"));
		}
	}
}
