package com.espressif.idf.test.executable.cases.environment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.test.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.test.operations.ProjectTestOperations;

@RunWith(SWTBotJunit4ClassRunner.class)
public class SettingEspressifEnvTest
{
	private Fixture fixture;

	@Before
	public void beforeEachTest() throws Exception
	{
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

	private class Fixture
	{
		private static final String ESP_IDF_PATH_PROPERTY = "default.env.esp.idf.path";
		private static final String GIT_PATH_PROPERTY = "default.env.esp.git.path";
		private static final String PYTHON_VERSION_PROPERTY = "default.env.esp.python.version";

		private SWTWorkbenchBot bot;
		private String espIdfPath;
		private String gitPath;
		private String pythonVersion;

		private Fixture()
		{
			bot = new SWTWorkbenchBot();
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
			if (StringUtils.isEmpty(espIdfPath) || StringUtils.isEmpty(gitPath) || StringUtils.isEmpty(pythonVersion))
			{
				throw new Exception("Missing Properties Found");
			}
		}

		private void whenInstallToolsIsSelected()
		{
			bot.menu("Help").menu("ESP-IDF Tools Manager").menu("Install Tools").click();
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
			bot.menu("Help").menu("Product Information").click();
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			assertFalse(consoleView.bot().styledText().getText().contains("IDF_PATH: <NOT FOUND>"));
		}
	}
}
